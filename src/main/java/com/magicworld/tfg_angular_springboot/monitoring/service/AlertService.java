package com.magicworld.tfg_angular_springboot.monitoring.service;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.AttractionRepository;
import com.magicworld.tfg_angular_springboot.monitoring.alert.*;
import com.magicworld.tfg_angular_springboot.monitoring.dto.*;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEvent;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AlertService {

    private static final int HIGH_QUEUE_THRESHOLD = 80;
    private static final int CRITICAL_QUEUE_THRESHOLD = 120;

    private final ParkAlertRepository alertRepository;
    private final AttractionRepository attractionRepository;
    private final MonitoringWebSocketService webSocketService;
    private final Random random = new Random();

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

        String message = getMessageForType(type);
        AlertSeverity severity = getSeverityForType(type);
        String suggestion = getSuggestionForType(type);

        ParkAlert alert = buildAlert(type, severity, message, suggestion,
            type == AlertType.LOST_CHILD || type == AlertType.MEDICAL_EMERGENCY ? null : attractionId);
        saveAndBroadcast(alert);
    }

    private String getMessageForType(AlertType type) {
        return switch (type) {
            case TECHNICAL_ISSUE -> "Fallo técnico detectado en el sistema de seguridad";
            case MEDICAL_EMERGENCY -> "Emergencia médica reportada - Visitante requiere atención";
            case GUEST_COMPLAINT -> "Queja de visitante: Tiempo de espera excesivo";
            case MAINTENANCE_REQUIRED -> "Mantenimiento preventivo requerido";
            case SAFETY_CONCERN -> "Incidente de seguridad menor reportado";
            case LOST_CHILD -> "Niño perdido reportado en la zona";
            case WEATHER_WARNING -> "Alerta meteorológica: Posible tormenta";
            default -> "Alerta del sistema";
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
            case TECHNICAL_ISSUE -> "Evaluar si se requiere cierre temporal";
            case MEDICAL_EMERGENCY -> "Enviar equipo médico inmediatamente";
            case GUEST_COMPLAINT -> "Ofrecer compensación al visitante";
            case MAINTENANCE_REQUIRED -> "Programar mantenimiento en próximo cierre";
            case SAFETY_CONCERN -> "Enviar seguridad para evaluar";
            case LOST_CHILD -> "Activar protocolo de búsqueda";
            default -> "Evaluar situación";
        };
    }

    private void checkQueueAlert(ParkEvent event) {
        if (event.getQueueSize() >= CRITICAL_QUEUE_THRESHOLD) {
            createQueueAlert(event, AlertSeverity.CRITICAL, "Cola crítica: más de 100 personas esperando");
        } else if (event.getQueueSize() >= HIGH_QUEUE_THRESHOLD) {
            createQueueAlert(event, AlertSeverity.WARNING, "Cola alta: más de 50 personas esperando");
        }
    }

    private void createQueueAlert(ParkEvent event, AlertSeverity severity, String message) {
        List<ParkAlert> existing = alertRepository.findByAttractionIdAndIsActiveTrue(event.getAttractionId());
        boolean alreadyAlerted = existing.stream()
                .anyMatch(a -> a.getAlertType() == AlertType.HIGH_QUEUE && a.getSeverity() == severity);
        if (alreadyAlerted) return;

        ParkAlert alert = buildAlert(AlertType.HIGH_QUEUE, severity, message,
                "Considerar abrir más puertas o redirigir personal", event.getAttractionId());
        saveAndBroadcast(alert);
    }

    private void createAttractionDownAlert(ParkEvent event) {
        ParkAlert alert = buildAlert(AlertType.ATTRACTION_DOWN, AlertSeverity.WARNING,
                "Atracción cerrada", "Revisar estado técnico", event.getAttractionId());
        saveAndBroadcast(alert);
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

    @Transactional
    public ResolutionResult resolveAlert(Long alertId, ResolveAlertRequest request) {
        Optional<ParkAlert> alertOpt = alertRepository.findById(alertId);
        if (alertOpt.isEmpty()) {
            return ResolutionResult.builder()
                    .success(false)
                    .message("Alerta no encontrada")
                    .build();
        }

        ParkAlert alert = alertOpt.get();
        alert.setIsActive(false);
        alert.setResolvedAt(LocalDateTime.now());
        alertRepository.save(alert);

        return calculateResolutionImpact(alert, request.getResolutionOptionId());
    }

    private ResolutionResult calculateResolutionImpact(ParkAlert alert, String optionId) {
        int satisfaction = 0;
        int waitChange = 0;
        int cost = 0;
        String impact;
        String message;

        switch (optionId) {
            case "add_staff" -> {
                satisfaction = 15;
                waitChange = -10;
                cost = 150;
                impact = "positive";
                message = "Personal adicional asignado. Tiempo de espera reducido.";
            }
            case "open_extra_line" -> {
                satisfaction = 20;
                waitChange = -15;
                cost = 50;
                impact = "positive";
                message = "Línea adicional abierta. Flujo mejorado significativamente.";
            }
            case "offer_fastpass" -> {
                satisfaction = 10;
                waitChange = -5;
                cost = 0;
                impact = "neutral";
                message = "FastPass ofrecidos a visitantes afectados.";
            }
            case "temporary_close" -> {
                satisfaction = -20;
                waitChange = 0;
                cost = 0;
                impact = "negative";
                message = "Atracción cerrada temporalmente. Visitantes decepcionados.";
            }
            case "send_medical" -> {
                satisfaction = 25;
                waitChange = 0;
                cost = 200;
                impact = "positive";
                message = "Equipo médico enviado. Situación bajo control.";
            }
            case "call_ambulance" -> {
                satisfaction = 30;
                waitChange = 0;
                cost = 500;
                impact = "positive";
                message = "Ambulancia en camino. Protocolo de emergencia activado.";
            }
            case "security_patrol" -> {
                satisfaction = 10;
                waitChange = 0;
                cost = 50;
                impact = "positive";
                message = "Patrulla de seguridad enviada a la zona.";
            }
            case "announce_pa" -> {
                satisfaction = 5;
                waitChange = 0;
                cost = 0;
                impact = "neutral";
                message = "Anuncio realizado por megafonía.";
            }
            case "offer_compensation" -> {
                satisfaction = 15;
                waitChange = 0;
                cost = 25;
                impact = "positive";
                message = "Compensación ofrecida al visitante.";
            }
            case "ignore" -> {
                satisfaction = -10;
                waitChange = 5;
                cost = 0;
                impact = "negative";
                message = "Situación ignorada. Posible escalada del problema.";
            }
            case "schedule_maintenance" -> {
                satisfaction = 5;
                waitChange = 0;
                cost = 100;
                impact = "neutral";
                message = "Mantenimiento programado para el cierre.";
            }
            case "activate_search" -> {
                satisfaction = 30;
                waitChange = 0;
                cost = 100;
                impact = "positive";
                message = "Protocolo de búsqueda activado. Todo el personal alertado.";
            }
            default -> {
                satisfaction = 0;
                waitChange = 0;
                cost = 0;
                impact = "neutral";
                message = "Alerta resuelta.";
            }
        }

        return ResolutionResult.builder()
                .success(true)
                .message(message)
                .impact(impact)
                .satisfactionChange(satisfaction)
                .waitTimeChange(waitChange)
                .costIncurred(cost)
                .build();
    }

    public List<ResolutionOption> getResolutionOptions(AlertType type) {
        return switch (type) {
            case HIGH_QUEUE -> List.of(
                ResolutionOption.builder()
                    .id("add_staff").label("Añadir personal").description("Asignar 2 empleados adicionales")
                    .impact("positive").effectivenessScore(80).costScore(60).timeToResolveMinutes(5).build(),
                ResolutionOption.builder()
                    .id("open_extra_line").label("Abrir línea extra").description("Abrir una fila adicional")
                    .impact("positive").effectivenessScore(90).costScore(20).timeToResolveMinutes(2).build(),
                ResolutionOption.builder()
                    .id("offer_fastpass").label("Ofrecer FastPass").description("Dar FastPass a los que esperan")
                    .impact("neutral").effectivenessScore(50).costScore(0).timeToResolveMinutes(10).build(),
                ResolutionOption.builder()
                    .id("ignore").label("Ignorar").description("No tomar acción")
                    .impact("negative").effectivenessScore(0).costScore(0).timeToResolveMinutes(0).build()
            );
            case MEDICAL_EMERGENCY -> List.of(
                ResolutionOption.builder()
                    .id("send_medical").label("Enviar equipo médico").description("Enviar enfermeros del parque")
                    .impact("positive").effectivenessScore(85).costScore(40).timeToResolveMinutes(3).build(),
                ResolutionOption.builder()
                    .id("call_ambulance").label("Llamar ambulancia").description("Contactar servicios de emergencia")
                    .impact("positive").effectivenessScore(100).costScore(100).timeToResolveMinutes(10).build(),
                ResolutionOption.builder()
                    .id("ignore").label("Solo observar").description("Evaluar sin intervenir")
                    .impact("negative").effectivenessScore(10).costScore(0).timeToResolveMinutes(0).build()
            );
            case GUEST_COMPLAINT -> List.of(
                ResolutionOption.builder()
                    .id("offer_compensation").label("Ofrecer compensación").description("Dar vale de comida o descuento")
                    .impact("positive").effectivenessScore(75).costScore(30).timeToResolveMinutes(5).build(),
                ResolutionOption.builder()
                    .id("offer_fastpass").label("Ofrecer FastPass").description("Acceso prioritario a otra atracción")
                    .impact("positive").effectivenessScore(80).costScore(0).timeToResolveMinutes(2).build(),
                ResolutionOption.builder()
                    .id("ignore").label("Disculparse").description("Solo ofrecer disculpas verbales")
                    .impact("neutral").effectivenessScore(30).costScore(0).timeToResolveMinutes(2).build()
            );
            case SAFETY_CONCERN, TECHNICAL_ISSUE -> List.of(
                ResolutionOption.builder()
                    .id("security_patrol").label("Enviar seguridad").description("Patrulla para evaluar")
                    .impact("positive").effectivenessScore(70).costScore(20).timeToResolveMinutes(5).build(),
                ResolutionOption.builder()
                    .id("temporary_close").label("Cierre temporal").description("Cerrar área para inspección")
                    .impact("negative").effectivenessScore(90).costScore(80).timeToResolveMinutes(30).build(),
                ResolutionOption.builder()
                    .id("announce_pa").label("Anuncio PA").description("Informar por megafonía")
                    .impact("neutral").effectivenessScore(40).costScore(0).timeToResolveMinutes(1).build()
            );
            case MAINTENANCE_REQUIRED -> List.of(
                ResolutionOption.builder()
                    .id("schedule_maintenance").label("Programar mantenimiento").description("Para el próximo cierre")
                    .impact("neutral").effectivenessScore(60).costScore(50).timeToResolveMinutes(0).build(),
                ResolutionOption.builder()
                    .id("temporary_close").label("Cerrar ahora").description("Mantenimiento inmediato")
                    .impact("negative").effectivenessScore(95).costScore(90).timeToResolveMinutes(60).build()
            );
            case LOST_CHILD -> List.of(
                ResolutionOption.builder()
                    .id("activate_search").label("Activar búsqueda").description("Protocolo completo de búsqueda")
                    .impact("positive").effectivenessScore(95).costScore(50).timeToResolveMinutes(15).build(),
                ResolutionOption.builder()
                    .id("announce_pa").label("Anuncio PA").description("Descripción por megafonía")
                    .impact("positive").effectivenessScore(70).costScore(0).timeToResolveMinutes(2).build(),
                ResolutionOption.builder()
                    .id("security_patrol").label("Alertar seguridad").description("Notificar a guardias")
                    .impact("positive").effectivenessScore(80).costScore(20).timeToResolveMinutes(5).build()
            );
            default -> List.of(
                ResolutionOption.builder()
                    .id("acknowledge").label("Reconocer").description("Marcar como visto")
                    .impact("neutral").effectivenessScore(50).costScore(0).timeToResolveMinutes(0).build()
            );
        };
    }

    @Transactional(readOnly = true)
    public List<AlertDTO> getActiveAlerts() {
        return alertRepository.findByIsActiveTrueOrderByTimestampDesc().stream()
                .map(this::toDTO)
                .toList();
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
                .resolutionOptions(getResolutionOptions(alert.getAlertType()))
                .build();
    }
}
