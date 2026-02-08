package com.magicworld.tfg_angular_springboot.monitoring.service;

import com.magicworld.tfg_angular_springboot.monitoring.dto.EventRequest;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEvent;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventRepository;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventIngestionService {

    private final ParkEventRepository eventRepository;
    private final MonitoringWebSocketService webSocketService;
    private final AlertService alertService;

    @Transactional
    public ParkEvent recordEvent(EventRequest request) {
        ParkEvent event = ParkEvent.builder()
                .eventType(request.getEventType())
                .timestamp(LocalDateTime.now())
                .attractionId(request.getAttractionId())
                .userId(request.getUserId())
                .visitorCount(request.getVisitorCount())
                .queueSize(request.getQueueSize())
                .metadata(request.getMetadata())
                .build();

        ParkEvent saved = eventRepository.save(event);
        webSocketService.broadcastEvent(saved);
        alertService.checkAndTriggerAlerts(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ParkEvent> getRecentEvents(int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        return eventRepository.findByTimestampBetweenOrderByTimestampDesc(since, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public long countEntriesSince(LocalDateTime since) {
        return eventRepository.countByEventTypeSince(ParkEventType.PARK_ENTRY, since);
    }

    @Transactional(readOnly = true)
    public long countExitsSince(LocalDateTime since) {
        return eventRepository.countByEventTypeSince(ParkEventType.PARK_EXIT, since);
    }
}
