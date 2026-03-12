package com.magicworld.tfg_angular_springboot.monitoring;

import com.magicworld.tfg_angular_springboot.monitoring.dto.EventRequest;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEvent;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventRepository;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventType;
import com.magicworld.tfg_angular_springboot.monitoring.service.EventIngestionService;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Monitorización del Parque")
@Feature("Servicio de Ingesta de Eventos")
public class EventIngestionServiceTests {

    @Autowired
    private EventIngestionService eventIngestionService;

    @Autowired
    private ParkEventRepository eventRepository;

    @MockitoBean
    private MonitoringWebSocketService webSocketService;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    @Story("Registrar Evento")
    @Description("Verifica que registrar evento de entrada al parque funciona")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Registrar evento de entrada")
    void testRecordParkEntryEvent() {
        EventRequest request = new EventRequest();
        request.setEventType(ParkEventType.PARK_ENTRY);
        request.setVisitorCount(1);

        ParkEvent result = eventIngestionService.recordEvent(request);
        assertNotNull(result.getId());
        assertEquals(ParkEventType.PARK_ENTRY, result.getEventType());
    }

    @Test
    @Story("Registrar Evento")
    @Description("Verifica que registrar evento de salida del parque funciona")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Registrar evento de salida")
    void testRecordParkExitEvent() {
        EventRequest request = new EventRequest();
        request.setEventType(ParkEventType.PARK_EXIT);
        request.setVisitorCount(1);

        ParkEvent result = eventIngestionService.recordEvent(request);
        assertNotNull(result.getId());
        assertEquals(ParkEventType.PARK_EXIT, result.getEventType());
    }

    @Test
    @Story("Eventos Recientes")
    @Description("Verifica que obtener eventos recientes funciona")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener eventos recientes")
    void testGetRecentEvents() {
        EventRequest request = new EventRequest();
        request.setEventType(ParkEventType.PARK_ENTRY);
        request.setVisitorCount(1);
        eventIngestionService.recordEvent(request);

        List<ParkEvent> recentEvents = eventIngestionService.getRecentEvents(60);
        assertFalse(recentEvents.isEmpty());
    }

    @Test
    @Story("Conteo de Entradas")
    @Description("Verifica que contar entradas funciona")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contar entradas desde")
    void testCountEntriesSince() {
        EventRequest request = new EventRequest();
        request.setEventType(ParkEventType.PARK_ENTRY);
        request.setVisitorCount(1);
        eventIngestionService.recordEvent(request);

        long count = eventIngestionService.countEntriesSince(
                java.time.LocalDateTime.now().minusMinutes(5));
        assertTrue(count >= 1);
    }

    @Test
    @Story("Conteo de Salidas")
    @Description("Verifica que contar salidas funciona")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contar salidas desde")
    void testCountExitsSince() {
        EventRequest request = new EventRequest();
        request.setEventType(ParkEventType.PARK_EXIT);
        request.setVisitorCount(1);
        eventIngestionService.recordEvent(request);

        long count = eventIngestionService.countExitsSince(
                java.time.LocalDateTime.now().minusMinutes(5));
        assertTrue(count >= 1);
    }
}
