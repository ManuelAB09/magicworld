package com.magicworld.tfg_angular_springboot.statistics;

import com.magicworld.tfg_angular_springboot.attraction.*;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEvent;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventRepository;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventType;
import com.magicworld.tfg_angular_springboot.statistics.dto.AttractionPerformanceDTO;
import com.magicworld.tfg_angular_springboot.statistics.dto.MonthlySalesDTO;
import com.magicworld.tfg_angular_springboot.statistics.dto.TicketSalesDTO;
import com.magicworld.tfg_angular_springboot.statistics.service.ParkStatsService;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Estadísticas")
@Feature("Servicio de Estadísticas del Parque - Integración")
public class ParkStatsServiceIntegrationTests {

    @Autowired private ParkStatsService parkStatsService;
    @Autowired private ParkEventRepository parkEventRepository;
    @Autowired private AttractionRepository attractionRepository;

    private Attraction attraction;

    @BeforeEach
    void setUp() {
        attraction = attractionRepository.save(Attraction.builder()
                .name("Stats Attraction").description("Test").photoUrl("http://example.com/stats.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.HIGH)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(12).minimumHeight(140).minimumWeight(0)
                .mapPositionX(5.0).mapPositionY(5.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(17, 0))
                .build());
    }

    @Test
    @Story("Ventas de Entradas")
    @Description("Verifica que getTicketSales con rango sin datos retorna cero")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getTicketSalesConRangoSinDatosRetornaCero")
    void getTicketSalesConRangoSinDatosRetornaCero() {
        LocalDate from = LocalDate.now().plusYears(2);
        LocalDate to = from.plusDays(30);

        TicketSalesDTO result = parkStatsService.getTicketSales(from, to, "es");

        assertEquals(0, result.getTotalTicketsSold());
        assertEquals(0, result.getTotalRevenue().compareTo(BigDecimal.ZERO));
    }

    @Test
    @Story("Ventas de Entradas")
    @Description("Verifica que getTicketSales en USD convierte moneda")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getTicketSalesEnUsdConvierteMoneda")
    void getTicketSalesEnUsdConvierteMoneda() {
        LocalDate from = LocalDate.now().minusMonths(1);
        LocalDate to = LocalDate.now();

        TicketSalesDTO resultEs = parkStatsService.getTicketSales(from, to, "es");
        TicketSalesDTO resultEn = parkStatsService.getTicketSales(from, to, "en");

        assertEquals("EUR", resultEs.getCurrency());
        assertEquals("USD", resultEn.getCurrency());
    }

    @Test
    @Story("Desglose Mensual")
    @Description("Verifica que getSeasonalBreakdown retorna nombres de meses en español")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getSeasonalBreakdownRetornaNombresMesesEnEspanol")
    void getSeasonalBreakdownRetornaNombresMesesEnEspanol() {
        List<MonthlySalesDTO> result = parkStatsService.getSeasonalBreakdown(2026, "es");

        assertEquals(12, result.size());
        assertEquals(1, result.get(0).getMonth());
        assertTrue(result.get(0).getMonthName().toLowerCase().contains("enero"));
    }

    @Test
    @Story("Desglose Mensual")
    @Description("Verifica que getSeasonalBreakdown con null locale usa español")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getSeasonalBreakdownConNullLocaleUsaEspanol")
    void getSeasonalBreakdownConNullLocaleUsaEspanol() {
        List<MonthlySalesDTO> result = parkStatsService.getSeasonalBreakdown(2026, null);

        assertEquals(12, result.size());
    }

    @Test
    @Story("Rendimiento de Atracciones")
    @Description("Verifica que getAttractionPerformance con eventos retorna datos")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getAttractionPerformanceConEventosRetornaDatos")
    void getAttractionPerformanceConEventosRetornaDatos() {
        parkEventRepository.save(ParkEvent.builder()
                .eventType(ParkEventType.ATTRACTION_QUEUE_JOIN)
                .attractionId(attraction.getId())
                .queueSize(50)
                .timestamp(LocalDateTime.now())
                .build());

        parkEventRepository.save(ParkEvent.builder()
                .eventType(ParkEventType.ATTRACTION_QUEUE_JOIN)
                .attractionId(attraction.getId())
                .queueSize(80)
                .timestamp(LocalDateTime.now())
                .build());

        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now().plusDays(1);

        List<AttractionPerformanceDTO> result = parkStatsService.getAttractionPerformance(from, to);

        assertFalse(result.isEmpty());
        assertEquals(attraction.getId(), result.get(0).getAttractionId());
        assertTrue(result.get(0).getMaxQueueSize() >= 50);
    }

    @Test
    @Story("Rendimiento de Atracciones")
    @Description("Verifica que getAttractionPerformance sin eventos retorna lista vacía")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getAttractionPerformanceSinEventosRetornaListaVacia")
    void getAttractionPerformanceSinEventosRetornaListaVacia() {
        LocalDate from = LocalDate.now().plusYears(2);
        LocalDate to = from.plusDays(1);

        List<AttractionPerformanceDTO> result = parkStatsService.getAttractionPerformance(from, to);
        assertTrue(result.isEmpty());
    }
}

