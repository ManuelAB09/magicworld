package com.magicworld.tfg_angular_springboot.monitoring.service;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.AttractionRepository;
import com.magicworld.tfg_angular_springboot.monitoring.alert.*;
import com.magicworld.tfg_angular_springboot.monitoring.dto.*;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEvent;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventType;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AlertService {

    private static final int HIGH_QUEUE_THRESHOLD = 80;
    private static final int CRITICAL_QUEUE_THRESHOLD = 120;

    private final ParkAlertRepository alertRepository;
    private final AttractionRepository attractionRepository;
    private final MonitoringWebSocketService webSocketService;
    private final ResolutionOptionsService resolutionOptionsService;
    private final AlertResolutionService alertResolutionService;
    private final Random random = new Random();

    public AlertService(
            ParkAlertRepository alertRepository,
            AttractionRepository attractionRepository,
            MonitoringWebSocketService webSocketService,
            ResolutionOptionsService resolutionOptionsService,
            @Lazy AlertResolutionService alertResolutionService) {
        this.alertRepository = alertRepository;
        this.attractionRepository = attractionRepository;
        this.webSocketService = webSocketService;
        this.resolutionOptionsService = resolutionOptionsService;
        this.alertResolutionService = alertResolutionService;
    }

    @Transactional
    public void checkAndTriggerAlerts(ParkEvent event) {
        if (event.getEventType() == ParkEventType.ATTRACTION_QUEUE_JOIN && event.getQueueSize() != null) {
            checkQueueAlert(event);
        }
        if (event.getEventType() == ParkEventType.ATTRACTION_CLOSE) {
            createAttractionDownAlert(event);
        }
    }

    @Transactional
    public void generateRandomAlert(Long attractionId) {
        AlertType[] types = {
            AlertType.TECHNICAL_ISSUE, AlertType.MEDICAL_EMERGENCY,
            AlertType.GUEST_COMPLAINT, AlertType.MAINTENANCE_REQUIRED,
            AlertType.SAFETY_CONCERN, AlertType.LOST_CHILD
        };
        AlertType type = types[random.nextInt(types.length)];

        Long targetAttractionId = shouldHaveAttraction(type) ? attractionId : null;
        ParkAlert alert = buildAlert(type, getSeverityForType(type),
            getMessageForType(type), getSuggestionForType(type), targetAttractionId);
        saveAndBroadcast(alert);
    }

    @Transactional
    public ResolutionResult resolveAlert(Long alertId, ResolveAlertRequest request) {
        return alertResolutionService.resolveAlert(
            alertId,
            request.getResolutionOptionId(),
            request.getEmployeeId()
        );
    }

    @Transactional(readOnly = true)
    public List<AlertDTO> getActiveAlerts() {
        return alertRepository.findByIsActiveTrueOrderByTimestampDesc().stream()
                .map(this::toDTO)
                .toList();
    }

    private void checkQueueAlert(ParkEvent event) {
        if (event.getQueueSize() >= CRITICAL_QUEUE_THRESHOLD) {
            createQueueAlert(event, AlertSeverity.CRITICAL, "alerts.messages.high_queue_critical");
        } else if (event.getQueueSize() >= HIGH_QUEUE_THRESHOLD) {
            createQueueAlert(event, AlertSeverity.WARNING, "alerts.messages.high_queue");
        }
    }

    private void createQueueAlert(ParkEvent event, AlertSeverity severity, String message) {
        List<ParkAlert> existing = alertRepository.findByAttractionIdAndIsActiveTrue(event.getAttractionId());
        boolean alreadyAlerted = existing.stream()
                .anyMatch(a -> a.getAlertType() == AlertType.HIGH_QUEUE && a.getSeverity() == severity);
        if (alreadyAlerted) return;

        ParkAlert alert = buildAlert(AlertType.HIGH_QUEUE, severity, message,
                "alerts.suggestions.high_queue", event.getAttractionId());
        saveAndBroadcast(alert);
    }

    private void createAttractionDownAlert(ParkEvent event) {
        ParkAlert alert = buildAlert(AlertType.ATTRACTION_DOWN, AlertSeverity.WARNING,
                "alerts.messages.attraction_down", "alerts.suggestions.attraction_down", event.getAttractionId());
        saveAndBroadcast(alert);
    }

    private boolean shouldHaveAttraction(AlertType type) {
        return type != AlertType.LOST_CHILD && type != AlertType.MEDICAL_EMERGENCY;
    }

    private String getMessageForType(AlertType type) {
        return switch (type) {
            case TECHNICAL_ISSUE -> "alerts.messages.technical_issue";
            case MEDICAL_EMERGENCY -> "alerts.messages.medical_emergency";
            case GUEST_COMPLAINT -> "alerts.messages.guest_complaint";
            case MAINTENANCE_REQUIRED -> "alerts.messages.maintenance_required";
            case SAFETY_CONCERN -> "alerts.messages.safety_concern";
            case LOST_CHILD -> "alerts.messages.lost_child";
            default -> "alerts.messages.default";
        };
    }

    private AlertSeverity getSeverityForType(AlertType type) {
        return switch (type) {
            case MEDICAL_EMERGENCY, LOST_CHILD -> AlertSeverity.CRITICAL;
            case TECHNICAL_ISSUE, SAFETY_CONCERN -> AlertSeverity.WARNING;
            default -> AlertSeverity.INFO;
        };
    }

    private String getSuggestionForType(AlertType type) {
        return switch (type) {
            case TECHNICAL_ISSUE -> "alerts.suggestions.technical_issue";
            case MEDICAL_EMERGENCY -> "alerts.suggestions.medical_emergency";
            case GUEST_COMPLAINT -> "alerts.suggestions.guest_complaint";
            case MAINTENANCE_REQUIRED -> "alerts.suggestions.maintenance_required";
            case SAFETY_CONCERN -> "alerts.suggestions.safety_concern";
            case LOST_CHILD -> "alerts.suggestions.lost_child";
            default -> "alerts.suggestions.default";
        };
    }

    private ParkAlert buildAlert(AlertType type, AlertSeverity severity, String message,
                                  String suggestion, Long attractionId) {
        return ParkAlert.builder()
                .alertType(type)
                .severity(severity)
                .message(message)
                .suggestion(suggestion)
                .attractionId(attractionId)
                .timestamp(LocalDateTime.now())
                .isActive(true)
                .build();
    }

    private void saveAndBroadcast(ParkAlert alert) {
        ParkAlert saved = alertRepository.save(alert);
        webSocketService.broadcastAlert(toDTO(saved));
    }

    private AlertDTO toDTO(ParkAlert alert) {
        String attractionName = null;
        if (alert.getAttractionId() != null) {
            attractionName = attractionRepository.findById(alert.getAttractionId())
                    .map(Attraction::getName).orElse(null);
        }

        return AlertDTO.builder()
                .id(alert.getId())
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .message(alert.getMessage())
                .suggestion(alert.getSuggestion())
                .attractionId(alert.getAttractionId())
                .attractionName(attractionName)
                .timestamp(alert.getTimestamp())
                .active(alert.getIsActive())
                .resolutionOptions(resolutionOptionsService.getResolutionOptions(alert))
                .build();
    }
}

