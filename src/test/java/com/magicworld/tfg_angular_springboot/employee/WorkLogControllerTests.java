package com.magicworld.tfg_angular_springboot.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
import com.magicworld.tfg_angular_springboot.employee.controller.WorkLogController;
import com.magicworld.tfg_angular_springboot.employee.dto.EmployeeHoursSummaryDTO;
import com.magicworld.tfg_angular_springboot.employee.dto.WorkLogEntryDTO;
import com.magicworld.tfg_angular_springboot.employee.dto.WorkLogEntryRequest;
import com.magicworld.tfg_angular_springboot.employee.service.WorkLogService;
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
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WorkLogController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Gestión de Horas de Trabajo")
@Feature("API REST de Work Log")
public class WorkLogControllerTests {

    private static final String API_BASE = "/api/v1/worklog";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkLogService workLogService;

    @Test
    @Story("Obtener Resumen")
    @Description("Verifica que obtener resumen retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener resumen retorna 200 OK")
    void testGetSummaryReturnsOk() throws Exception {
        EmployeeHoursSummaryDTO dto = EmployeeHoursSummaryDTO.builder()
                .employeeId(1L).employeeName("John Doe").role("OPERATOR")
                .scheduledHours(BigDecimal.valueOf(40)).normalHoursWorked(BigDecimal.valueOf(40))
                .overtimeHours(BigDecimal.ZERO).totalHoursWorked(BigDecimal.valueOf(40))
                .absences(0).scheduledDays(5).workedDays(5)
                .adjustments(Collections.emptyList()).build();
        when(workLogService.getEmployeeSummary(eq(1L), any(), any())).thenReturn(dto);

        var result = mockMvc.perform(get(API_BASE + "/summary/1")
                .param("from", "2026-06-01").param("to", "2026-06-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(1))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Obtener Historial")
    @Description("Verifica que obtener historial retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener historial retorna 200 OK")
    void testGetHistoryReturnsOk() throws Exception {
        when(workLogService.getWorkLogHistory(eq(1L), any(), any())).thenReturn(List.of());
        var result = mockMvc.perform(get(API_BASE + "/history/1")
                .param("from", "2026-06-01").param("to", "2026-06-07"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Añadir Entrada")
    @Description("Verifica que añadir entrada retorna 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Añadir entrada retorna 201")
    void testAddEntryReturnsCreated() throws Exception {
        WorkLogEntryDTO dto = WorkLogEntryDTO.builder()
                .id(1L).employeeId(1L).employeeName("John Doe")
                .targetDate(LocalDate.of(2026, 6, 2))
                .action(WorkLogAction.ADD_OVERTIME_HOURS)
                .hoursAffected(new BigDecimal("4.00"))
                .isOvertime(true).reason("Extra").performedBy("admin")
                .createdAt(LocalDateTime.now()).build();
        when(workLogService.addWorkLogEntry(any(WorkLogEntryRequest.class), anyString())).thenReturn(dto);

        WorkLogEntryRequest req = WorkLogEntryRequest.builder()
                .employeeId(1L).targetDate(LocalDate.of(2026, 6, 2))
                .action(WorkLogAction.ADD_OVERTIME_HOURS)
                .hoursAffected(new BigDecimal("4.00"))
                .isOvertime(true).reason("Extra coverage").build();

        var result = mockMvc.perform(post(API_BASE + "/entry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .with(request -> {
                    request.setUserPrincipal(
                            new UsernamePasswordAuthenticationToken(
                                    "admin", "password", java.util.Collections.emptyList()));
                    return request;
                }))
                .andExpect(status().isCreated())
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    @TestConfiguration
    static class Config {
        @Bean
        public WorkLogService workLogService() {
            return Mockito.mock(WorkLogService.class);
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
