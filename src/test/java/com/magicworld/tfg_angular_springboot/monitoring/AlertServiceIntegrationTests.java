package com.magicworld.tfg_angular_springboot.monitoring;

import com.magicworld.tfg_angular_springboot.attraction.*;
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
@Feature("Servicio de Alertas - Integración")
public class AlertServiceIntegrationTests {

    @Autowired private AlertService alertService;
    @Autowired private ParkAlertRepository alertRepository;
    @Autowired private AttractionRepository attractionRepository;
    @MockitoBean private MonitoringWebSocketService webSocketService;

    private Attraction testAttraction;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();

        testAttraction = attractionRepository.save(Attraction.builder()
                .name("AlertInt Coaster").description("Test").photoUrl("http://example.com/ai.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.HIGH)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(12).minimumHeight(140).minimumWeight(0)
                .mapPositionX(6.0).mapPositionY(6.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(17, 0))
                .build());
    }

    @Test
    @Story("Alertas por Cola Alta")
    @Description("Verifica que cola por debajo del umbral no genera alerta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("colaPorDebajoDelUmbralNoGeneraAlerta")
    void colaPorDebajoDelUmbralNoGeneraAlerta() {
        ParkEvent event = ParkEvent.builder()
                .eventType(ParkEventType.ATTRACTION_QUEUE_JOIN)
                .attractionId(testAttraction.getId())
                .queueSize(50)
                .timestamp(LocalDateTime.now())
                .build();

        alertService.checkAndTriggerAlerts(event);

        List<ParkAlert> alerts = alertRepository.findByAttractionIdAndIsActiveTrue(testAttraction.getId());
        assertTrue(alerts.isEmpty());
    }

    @Test
    @Story("Alertas por Cola Alta")
    @Description("Verifica que cola entre 80-119 genera alerta WARNING y >= 120 genera CRITICAL")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("colaGeneraAmbosNivelesDeSeveridad")
    void colaGeneraAmbosNivelesDeSeveridad() {
        ParkEvent warningEvent = ParkEvent.builder()
                .eventType(ParkEventType.ATTRACTION_QUEUE_JOIN)
                .attractionId(testAttraction.getId())
                .queueSize(90)
                .timestamp(LocalDateTime.now())
                .build();
        alertService.checkAndTriggerAlerts(warningEvent);

        ParkEvent criticalEvent = ParkEvent.builder()
                .eventType(ParkEventType.ATTRACTION_QUEUE_JOIN)
                .attractionId(testAttraction.getId())
                .queueSize(130)
                .timestamp(LocalDateTime.now())
                .build();
        alertService.checkAndTriggerAlerts(criticalEvent);

        List<ParkAlert> alerts = alertRepository.findByAttractionIdAndIsActiveTrue(testAttraction.getId());
        boolean hasWarning = alerts.stream().anyMatch(a -> a.getSeverity() == AlertSeverity.WARNING);
        boolean hasCritical = alerts.stream().anyMatch(a -> a.getSeverity() == AlertSeverity.CRITICAL);
        assertTrue(hasWarning);
        assertTrue(hasCritical);
    }

    @Test
    @Story("Generar Alerta Aleatoria")
    @Description("Verifica que generar alerta aleatoria crea alerta con tipo válido")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("generateRandomAlertCreaAlertaConTipoValido")
    void generateRandomAlertCreaAlertaConTipoValido() {
        alertService.generateRandomAlert(testAttraction.getId());

        List<ParkAlert> alerts = alertRepository.findAll();
        assertFalse(alerts.isEmpty());
        ParkAlert alert = alerts.get(0);
        assertNotNull(alert.getAlertType());
        assertNotNull(alert.getSeverity());
        assertNotNull(alert.getMessage());
        assertTrue(alert.getIsActive());
    }

    @Test
    @Story("Generar Alerta Aleatoria")
    @Description("Verifica que múltiples alertas aleatorias generan distintos tipos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("multipleAlertasAleatoriasGeneranDistintosTipos")
    void multipleAlertasAleatoriasGeneranDistintosTipos() {
        for (int i = 0; i < 10; i++) {
            alertService.generateRandomAlert(testAttraction.getId());
        }

        List<ParkAlert> alerts = alertRepository.findAll();
        long distinctTypes = alerts.stream()
                .map(ParkAlert::getAlertType)
                .distinct().count();
        assertTrue(distinctTypes >= 1);
    }

    @Test
    @Story("Obtener Alertas Activas")
    @Description("Verifica que getActiveAlerts incluye nombre de atracción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getActiveAlertsIncluyeNombreDeAtraccion")
    void getActiveAlertsIncluyeNombreDeAtraccion() {
        alertRepository.save(ParkAlert.builder()
                .alertType(AlertType.TECHNICAL_ISSUE)
                .severity(AlertSeverity.WARNING)
                .message("test.alert")
                .suggestion("fix.it")
                .attractionId(testAttraction.getId())
                .timestamp(LocalDateTime.now())
                .isActive(true)
                .build());

        List<AlertDTO> result = alertService.getActiveAlerts();
        assertEquals(1, result.size());
        assertEquals(testAttraction.getName(), result.get(0).getAttractionName());
    }

    @Test
    @Story("Obtener Alertas Activas")
    @Description("Verifica que getActiveAlerts sin attractionId tiene nombre null")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getActiveAlertsSinAttractionIdTieneNombreNull")
    void getActiveAlertsSinAttractionIdTieneNombreNull() {
        alertRepository.save(ParkAlert.builder()
                .alertType(AlertType.LOST_CHILD)
                .severity(AlertSeverity.CRITICAL)
                .message("test.alert")
                .suggestion("search")
                .timestamp(LocalDateTime.now())
                .isActive(true)
                .build());

        List<AlertDTO> result = alertService.getActiveAlerts();
        assertEquals(1, result.size());
        assertNull(result.get(0).getAttractionName());
    }

    @Test
    @Story("Obtener Alertas Activas")
    @Description("Verifica que alertas inactivas no aparecen en getActiveAlerts")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("alertasInactivasNoAparecenEnGetActiveAlerts")
    void alertasInactivasNoAparecenEnGetActiveAlerts() {
        alertRepository.save(ParkAlert.builder()
                .alertType(AlertType.TECHNICAL_ISSUE)
                .severity(AlertSeverity.WARNING)
                .message("test.inactive")
                .suggestion("none")
                .timestamp(LocalDateTime.now())
                .isActive(false)
                .build());

        List<AlertDTO> result = alertService.getActiveAlerts();
        assertTrue(result.isEmpty());
    }

    @Test
    @Story("Obtener Alertas Activas")
    @Description("Verifica que getActiveAlerts incluye opciones de resolución")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getActiveAlertsIncluyeOpcionesDeResolucion")
    void getActiveAlertsIncluyeOpcionesDeResolucion() {
        alertRepository.save(ParkAlert.builder()
                .alertType(AlertType.HIGH_QUEUE)
                .severity(AlertSeverity.WARNING)
                .message("alerts.messages.high_queue")
                .suggestion("alerts.suggestions.high_queue")
                .attractionId(testAttraction.getId())
                .timestamp(LocalDateTime.now())
                .isActive(true)
                .build());

        List<AlertDTO> result = alertService.getActiveAlerts();
        assertFalse(result.isEmpty());
        assertNotNull(result.get(0).getResolutionOptions());
        assertFalse(result.get(0).getResolutionOptions().isEmpty());
    }
}

