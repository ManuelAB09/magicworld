package com.magicworld.tfg_angular_springboot.statistics;

import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
import com.magicworld.tfg_angular_springboot.statistics.controller.StatisticsController;
import com.magicworld.tfg_angular_springboot.statistics.dto.*;
import com.magicworld.tfg_angular_springboot.statistics.service.EmployeeStatsService;
import com.magicworld.tfg_angular_springboot.statistics.service.ParkStatsService;
import com.magicworld.tfg_angular_springboot.storage.ImageStorageService;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StatisticsController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Estadísticas")
@Feature("API REST de Estadísticas")
public class StatisticsControllerTests {

    private static final String API_BASE = "/api/v1/statistics";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeStatsService employeeStatsService;

    @Autowired
    private ParkStatsService parkStatsService;

    @Test
    @Story("Ventas de Entradas")
    @Description("Verifica que obtener ventas de entradas retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Ventas de entradas retorna 200 OK")
    void testGetTicketSalesReturnsOk() throws Exception {
        TicketSalesDTO dto = TicketSalesDTO.builder()
                .totalTicketsSold(100).totalRevenue(BigDecimal.valueOf(500.00))
                .currency("EUR").build();
        when(parkStatsService.getTicketSales(any(LocalDate.class), any(LocalDate.class), anyString())).thenReturn(dto);

        var result = mockMvc.perform(get(API_BASE + "/park/ticket-sales")
                .param("from", "2026-06-01").param("to", "2026-06-30").param("locale", "es"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTicketsSold").value(100))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Ranking de Horas")
    @Description("Verifica que obtener ranking de horas retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Ranking de horas retorna 200 OK")
    void testGetHoursRankingReturnsOk() throws Exception {
        when(employeeStatsService.getHoursRanking(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());

        var result = mockMvc.perform(get(API_BASE + "/employees/hours-ranking")
                .param("from", "2026-06-01").param("to", "2026-06-07"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Ranking de Ausencias")
    @Description("Verifica que obtener ranking de ausencias retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ranking de ausencias retorna 200 OK")
    void testGetAbsenceRankingReturnsOk() throws Exception {
        when(employeeStatsService.getAbsenceRanking(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());

        var result = mockMvc.perform(get(API_BASE + "/employees/absence-ranking")
                .param("from", "2026-06-01").param("to", "2026-06-07"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Frecuencia de Posición")
    @Description("Verifica que obtener frecuencia de posición retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Frecuencia de posición retorna 200 OK")
    void testGetPositionFrequencyReturnsOk() throws Exception {
        when(employeeStatsService.getPositionFrequency(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        var result = mockMvc.perform(get(API_BASE + "/employees/position-frequency/1")
                .param("from", "2026-06-01").param("to", "2026-06-07"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Informe de Salarios")
    @Description("Verifica que obtener informe de salarios retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Informe de salarios retorna 200 OK")
    void testGetSalaryReportReturnsOk() throws Exception {
        when(employeeStatsService.getSalaryReport(any(LocalDate.class), any(LocalDate.class), anyString()))
                .thenReturn(List.of());

        var result = mockMvc.perform(get(API_BASE + "/employees/salary")
                .param("from", "2026-06-01").param("to", "2026-06-07")
                .param("locale", "es"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Desglose Estacional")
    @Description("Verifica que obtener desglose estacional retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Desglose estacional retorna 200 OK")
    void testGetSeasonalBreakdownReturnsOk() throws Exception {
        when(parkStatsService.getSeasonalBreakdown(anyInt(), anyString())).thenReturn(List.of());

        var result = mockMvc.perform(get(API_BASE + "/park/seasonality")
                .param("year", "2026").param("locale", "es"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Rendimiento de Atracciones")
    @Description("Verifica que obtener rendimiento de atracciones retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Rendimiento de atracciones retorna 200 OK")
    void testGetAttractionPerformanceReturnsOk() throws Exception {
        when(parkStatsService.getAttractionPerformance(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        var result = mockMvc.perform(get(API_BASE + "/park/attraction-performance")
                .param("from", "2026-06-01").param("to", "2026-06-30"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @TestConfiguration
    static class Config {
        @Bean
        public EmployeeStatsService employeeStatsService() {
            return Mockito.mock(EmployeeStatsService.class);
        }

        @Bean
        public ParkStatsService parkStatsService() {
            return Mockito.mock(ParkStatsService.class);
        }

        @Bean
        public JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return Mockito.mock(JwtAuthenticationFilter.class);
        }

        @Bean
        public ImageStorageService imageStorageService() {
            return Mockito.mock(ImageStorageService.class);
        }

        @Bean
        public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
            return Mockito.mock(OAuth2AuthenticationSuccessHandler.class);
        }

        @Bean
        public OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
            return Mockito.mock(OAuth2AuthenticationFailureHandler.class);
        }

        @Bean
        public SimpMessagingTemplate simpMessagingTemplate() {
            return Mockito.mock(SimpMessagingTemplate.class);
        }
    }
}
