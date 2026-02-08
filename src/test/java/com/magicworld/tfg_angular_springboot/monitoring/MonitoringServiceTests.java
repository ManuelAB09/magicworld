package com.magicworld.tfg_angular_springboot.monitoring;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.AttractionCategory;
import com.magicworld.tfg_angular_springboot.attraction.AttractionRepository;
import com.magicworld.tfg_angular_springboot.attraction.Intensity;
import com.magicworld.tfg_angular_springboot.monitoring.dto.DashboardSnapshot;
import com.magicworld.tfg_angular_springboot.monitoring.dto.EventRequest;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventRepository;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventType;
import com.magicworld.tfg_angular_springboot.monitoring.service.DashboardService;
import com.magicworld.tfg_angular_springboot.monitoring.service.EventIngestionService;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Sistema de Monitorización")
@Feature("Dashboard y Eventos")
public class MonitoringServiceTests {

    @Autowired
    private EventIngestionService eventService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private ParkEventRepository eventRepository;

    private Attraction testAttraction;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        attractionRepository.deleteAll();

        testAttraction = attractionRepository.save(Attraction.builder()
                .name("Test Coaster")
                .intensity(Intensity.HIGH)
                .category(AttractionCategory.ROLLER_COASTER)
                .minimumHeight(120)
                .minimumAge(10)
                .minimumWeight(30)
                .description("Test attraction")
                .photoUrl("http://test.com/photo.jpg")
                .isActive(true)
                .mapPositionX(50.0)
                .mapPositionY(50.0)
                .build());

        dashboardService.initializeAttractionStates();
    }

    @Test
    @Story("Registro de eventos")
    @Description("Verifica que se pueden registrar eventos de entrada al parque")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Registrar evento de entrada")
    void testRecordParkEntry() {
        EventRequest request = new EventRequest();
        request.setEventType(ParkEventType.PARK_ENTRY);
        request.setVisitorCount(1);

        var event = eventService.recordEvent(request);

        assertNotNull(event.getId());
        assertEquals(ParkEventType.PARK_ENTRY, event.getEventType());
    }

    @Test
    @Story("Dashboard")
    @Description("Verifica que el snapshot del dashboard incluye datos correctos")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener snapshot del dashboard")
    void testGetDashboardSnapshot() {
        EventRequest entry = new EventRequest();
        entry.setEventType(ParkEventType.PARK_ENTRY);
        eventService.recordEvent(entry);

        DashboardSnapshot snapshot = dashboardService.getSnapshot();

        assertNotNull(snapshot);
        assertTrue(snapshot.getCurrentVisitors() >= 0);
        assertEquals(1, snapshot.getTotalAttractions());
    }

    @Test
    @Story("Colas de atracciones")
    @Description("Verifica el registro de eventos de cola")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Registrar evento de cola")
    void testRecordQueueEvent() {
        EventRequest request = new EventRequest();
        request.setEventType(ParkEventType.ATTRACTION_QUEUE_JOIN);
        request.setAttractionId(testAttraction.getId());
        request.setQueueSize(10);

        var event = eventService.recordEvent(request);

        assertNotNull(event.getId());
        assertEquals(testAttraction.getId(), event.getAttractionId());
        assertEquals(10, event.getQueueSize());
    }

    @Test
    @Story("Eventos recientes")
    @Description("Verifica la consulta de eventos recientes")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener eventos recientes")
    void testGetRecentEvents() {
        EventRequest request = new EventRequest();
        request.setEventType(ParkEventType.PARK_ENTRY);
        eventService.recordEvent(request);

        var events = eventService.getRecentEvents(30);

        assertFalse(events.isEmpty());
    }
}
