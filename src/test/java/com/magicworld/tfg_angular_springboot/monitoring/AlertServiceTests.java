package com.magicworld.tfg_angular_springboot.monitoring;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.AttractionCategory;
import com.magicworld.tfg_angular_springboot.attraction.AttractionRepository;
import com.magicworld.tfg_angular_springboot.attraction.Intensity;
import com.magicworld.tfg_angular_springboot.attraction.MaintenanceStatus;
import com.magicworld.tfg_angular_springboot.monitoring.alert.*;
import com.magicworld.tfg_angular_springboot.monitoring.dto.AlertDTO;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEvent;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventType;
import com.magicworld.tfg_angular_springboot.monitoring.service.AlertService;
import com.magicworld.tfg_angular_springboot.monitoring.service.MonitoringWebSocketService;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Monitorización del Parque")
@Feature("Servicio de Alertas")
public class AlertServiceTests {

    @Autowired
    private AlertService alertService;

    @Autowired
    private ParkAlertRepository alertRepository;

    @Autowired
    private AttractionRepository attractionRepository;

    @MockitoBean
    private MonitoringWebSocketService webSocketService;

    private Attraction testAttraction;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();

        testAttraction = attractionRepository.save(Attraction.builder()
                .name("Alert Test Coaster").description("Ride").photoUrl("http://example.com/a.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.HIGH)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(12).minimumHeight(140).minimumWeight(0)
                .mapPositionX(5.0).mapPositionY(5.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(17, 0))
                .build());
    }

    @Test
    @Story("Obtener Alertas Activas")
    @Description("Verifica que obtener alertas activas funciona cuando no hay alertas")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener alertas activas vacías")
    void testGetActiveAlertsEmpty() {
        List<AlertDTO> result = alertService.getActiveAlerts();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @Story("Obtener Alertas Activas")
    @Description("Verifica que obtener alertas activas retorna alertas existentes")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener alertas activas con datos")
    void testGetActiveAlertsWithData() {
        alertRepository.save(ParkAlert.builder()
                .alertType(AlertType.TECHNICAL_ISSUE)
                .severity(AlertSeverity.WARNING)
                .message("Test alert")
                .suggestion("Fix it")
                .attractionId(testAttraction.getId())
                .timestamp(LocalDateTime.now())
                .isActive(true)
                .build());

        List<AlertDTO> result = alertService.getActiveAlerts();
        assertEquals(1, result.size());
        assertEquals(AlertType.TECHNICAL_ISSUE, result.get(0).getAlertType());
    }

    @Test
    @Story("Alertas por Cola Alta")
    @Description("Verifica que cola alta genera alerta WARNING")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Cola alta genera alerta WARNING")
    void testHighQueueTriggersWarningAlert() {
        ParkEvent event = ParkEvent.builder()
                .eventType(ParkEventType.ATTRACTION_QUEUE_JOIN)
                .attractionId(testAttraction.getId())
                .queueSize(85)
                .timestamp(LocalDateTime.now())
                .build();

        alertService.checkAndTriggerAlerts(event);

        List<ParkAlert> alerts = alertRepository.findByAttractionIdAndIsActiveTrue(testAttraction.getId());
        assertFalse(alerts.isEmpty());
        assertEquals(AlertSeverity.WARNING, alerts.get(0).getSeverity());
    }

    @Test
    @Story("Alertas por Cola Alta")
    @Description("Verifica que cola crítica genera alerta CRITICAL")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Cola crítica genera alerta CRITICAL")
    void testCriticalQueueTriggersCriticalAlert() {
        ParkEvent event = ParkEvent.builder()
                .eventType(ParkEventType.ATTRACTION_QUEUE_JOIN)
                .attractionId(testAttraction.getId())
                .queueSize(130)
                .timestamp(LocalDateTime.now())
                .build();

        alertService.checkAndTriggerAlerts(event);

        List<ParkAlert> alerts = alertRepository.findByAttractionIdAndIsActiveTrue(testAttraction.getId());
        assertTrue(alerts.stream().anyMatch(a -> a.getSeverity() == AlertSeverity.CRITICAL));
    }

    @Test
    @Story("Alertas por Cierre de Atracción")
    @Description("Verifica que cerrar atracción genera alerta de tipo ATTRACTION_DOWN")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Cerrar atracción genera alerta ATTRACTION_DOWN")
    void testAttractionCloseTriggersAlert() {
        ParkEvent event = ParkEvent.builder()
                .eventType(ParkEventType.ATTRACTION_CLOSE)
                .attractionId(testAttraction.getId())
                .timestamp(LocalDateTime.now())
                .build();

        alertService.checkAndTriggerAlerts(event);

        List<ParkAlert> alerts = alertRepository.findByAttractionIdAndIsActiveTrue(testAttraction.getId());
        assertTrue(alerts.stream().anyMatch(a -> a.getAlertType() == AlertType.ATTRACTION_DOWN));
    }

    @Test
    @Story("Alertas por Cola Alta")
    @Description("Verifica que no se duplican alertas de cola para la misma atracción y severidad")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("No duplica alertas de cola")
    void testNoDuplicateQueueAlerts() {
        ParkEvent event = ParkEvent.builder()
                .eventType(ParkEventType.ATTRACTION_QUEUE_JOIN)
                .attractionId(testAttraction.getId())
                .queueSize(90)
                .timestamp(LocalDateTime.now())
                .build();

        alertService.checkAndTriggerAlerts(event);
        alertService.checkAndTriggerAlerts(event);

        List<ParkAlert> alerts = alertRepository.findByAttractionIdAndIsActiveTrue(testAttraction.getId());
        long warningCount = alerts.stream()
                .filter(a -> a.getAlertType() == AlertType.HIGH_QUEUE && a.getSeverity() == AlertSeverity.WARNING)
                .count();
        assertEquals(1, warningCount);
    }

    @Test
    @Story("Generar Alerta Aleatoria")
    @Description("Verifica que generar alerta aleatoria crea una alerta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Generar alerta aleatoria crea alerta")
    void testGenerateRandomAlertCreatesAlert() {
        alertService.generateRandomAlert(testAttraction.getId());

        List<ParkAlert> allAlerts = alertRepository.findAll();
        assertFalse(allAlerts.isEmpty());
    }
}
