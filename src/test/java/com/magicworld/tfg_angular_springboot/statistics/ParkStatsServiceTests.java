package com.magicworld.tfg_angular_springboot.statistics;

import com.magicworld.tfg_angular_springboot.statistics.dto.MonthlySalesDTO;
import com.magicworld.tfg_angular_springboot.statistics.dto.TicketSalesDTO;
import com.magicworld.tfg_angular_springboot.statistics.service.ParkStatsService;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Estadísticas")
@Feature("Servicio de Estadísticas del Parque")
public class ParkStatsServiceTests {

    @Autowired
    private ParkStatsService parkStatsService;

    @Test
    @Story("Ventas de Entradas")
    @Description("Verifica que obtener ventas de entradas en español retorna EUR")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Ventas de entradas en EUR")
    void testGetTicketSalesEUR() {
        LocalDate from = LocalDate.now().minusMonths(1);
        LocalDate to = LocalDate.now();
        TicketSalesDTO result = parkStatsService.getTicketSales(from, to, "es");
        assertNotNull(result);
        assertEquals("EUR", result.getCurrency());
        assertTrue(result.getTotalTicketsSold() >= 0);
    }

    @Test
    @Story("Ventas de Entradas")
    @Description("Verifica que obtener ventas de entradas en inglés retorna USD")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ventas de entradas en USD")
    void testGetTicketSalesUSD() {
        LocalDate from = LocalDate.now().minusMonths(1);
        LocalDate to = LocalDate.now();
        TicketSalesDTO result = parkStatsService.getTicketSales(from, to, "en");
        assertNotNull(result);
        assertEquals("USD", result.getCurrency());
    }

    @Test
    @Story("Desglose Mensual")
    @Description("Verifica que obtener desglose estacional retorna 12 meses")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Desglose estacional retorna 12 meses")
    void testGetSeasonalBreakdown() {
        List<MonthlySalesDTO> result = parkStatsService.getSeasonalBreakdown(2026, "es");
        assertNotNull(result);
        assertEquals(12, result.size());
    }

    @Test
    @Story("Desglose Mensual")
    @Description("Verifica que desglose estacional en inglés retorna meses en inglés")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Desglose estacional en inglés")
    void testGetSeasonalBreakdownEnglish() {
        List<MonthlySalesDTO> result = parkStatsService.getSeasonalBreakdown(2026, "en");
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getMonthName().toLowerCase().contains("jan"));
    }

    @Test
    @Story("Rendimiento de Atracciones")
    @Description("Verifica que obtener rendimiento de atracciones retorna datos")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Rendimiento de atracciones")
    void testGetAttractionPerformance() {
        LocalDate from = LocalDate.now().minusMonths(1);
        LocalDate to = LocalDate.now();
        var result = parkStatsService.getAttractionPerformance(from, to);
        assertNotNull(result);
    }
}
