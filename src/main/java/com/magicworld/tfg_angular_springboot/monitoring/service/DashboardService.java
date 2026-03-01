package com.magicworld.tfg_angular_springboot.monitoring.service;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.AttractionRepository;
import com.magicworld.tfg_angular_springboot.monitoring.dto.*;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventType;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EventIngestionService eventService;
    private final AlertService alertService;
    private final AttractionRepository attractionRepository;
    private final PurchaseLineService purchaseLineService;

    @Value("${park.max-capacity:500}")
    private int parkMaxCapacity;

    private final Map<Long, AttractionState> attractionStates = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public DashboardSnapshot getSnapshot() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long entries = eventService.countEntriesSince(todayStart);
        long exits = eventService.countExitsSince(todayStart);
        int currentVisitors = (int) (entries - exits);

        List<Attraction> allAttractions = attractionRepository.findAll();
        List<AttractionStatus> statuses = allAttractions.stream()
                .map(this::buildAttractionStatus)
                .toList();

        long activeCount = statuses.stream().filter(AttractionStatus::isOpen).count();
        double avgWait = statuses.stream()
                .filter(AttractionStatus::isOpen)
                .mapToInt(AttractionStatus::getEstimatedWaitMinutes)
                .average().orElse(0);

        int ticketsSoldToday = purchaseLineService.getTotalSoldForDate(LocalDate.now());

        return DashboardSnapshot.builder()
                .currentVisitors(Math.max(0, currentVisitors))
                .totalEntriesToday((int) entries)
                .totalSalesToday(0)
                .activeAttractions((int) activeCount)
                .totalAttractions(allAttractions.size())
                .avgParkWaitTime(avgWait)
                .ticketsSoldToday(ticketsSoldToday)
                .parkMaxCapacity(parkMaxCapacity)
                .attractionStatuses(statuses)
                .activeAlerts(alertService.getActiveAlerts())
                .build();
    }

    private AttractionStatus buildAttractionStatus(Attraction attraction) {
        AttractionState state = attractionStates.computeIfAbsent(
                attraction.getId(),
                k -> new AttractionState(attraction.getIsActive()));

        // Always sync open/closed state from the database
        state.isOpen = attraction.getIsActive();

        int waitTime = state.isOpen ? calculateWaitTime(state.queueSize) : 0;

        return AttractionStatus.builder()
                .attractionId(attraction.getId())
                .name(attraction.getName())
                .open(state.isOpen)
                .queueSize(state.isOpen ? state.queueSize : 0)
                .estimatedWaitMinutes(waitTime)
                .mapPositionX(attraction.getMapPositionX())
                .mapPositionY(attraction.getMapPositionY())
                .intensity(attraction.getIntensity().name())
                .build();
    }

    private int calculateWaitTime(int queueSize) {
        if (queueSize <= 0)
            return 0;
        int cyclesNeeded = (int) Math.ceil((double) queueSize / 20);
        return cyclesNeeded * 3;
    }

    public void updateAttractionState(Long attractionId, ParkEventType eventType, Integer queueSize) {
        AttractionState state = attractionStates.computeIfAbsent(attractionId, k -> new AttractionState(true));

        switch (eventType) {
            case ATTRACTION_OPEN -> state.isOpen = true;
            case ATTRACTION_CLOSE -> {
                state.isOpen = false;
                state.queueSize = 0;
            }
            case ATTRACTION_QUEUE_JOIN -> {
                if (state.isOpen) {
                    if (queueSize != null)
                        state.queueSize = queueSize;
                    else
                        state.queueSize++;
                }
            }
            case ATTRACTION_QUEUE_LEAVE -> {
                if (state.isOpen) {
                    state.queueSize = Math.max(0, state.queueSize - 1);
                }
            }
            default -> {
            }
        }

    }

    public void initializeAttractionStates() {
        attractionRepository.findAll().forEach(a -> {
            AttractionState state = new AttractionState(a.getIsActive());
            state.isOpen = a.getIsActive();
            state.queueSize = 0;
            attractionStates.put(a.getId(), state);
        });
    }

    private static class AttractionState {
        boolean isOpen;
        int queueSize = 0;

        AttractionState(boolean isActive) {
            this.isOpen = isActive;
        }
    }
}
