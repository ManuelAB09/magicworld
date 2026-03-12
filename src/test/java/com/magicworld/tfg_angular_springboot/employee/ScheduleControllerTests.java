package com.magicworld.tfg_angular_springboot.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
import com.magicworld.tfg_angular_springboot.employee.controller.ScheduleController;
import com.magicworld.tfg_angular_springboot.employee.dto.CoverageValidationResult;
import com.magicworld.tfg_angular_springboot.employee.dto.CreateScheduleRequest;
import com.magicworld.tfg_angular_springboot.employee.dto.WeeklyScheduleDTO;
import com.magicworld.tfg_angular_springboot.employee.service.ScheduleService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ScheduleController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Gestión de Horarios")
@Feature("API REST de Horarios")
public class ScheduleControllerTests {

    private static final String API_BASE = "/api/v1/schedules";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScheduleService scheduleService;

    @Test
    @Story("Obtener Horario Semanal")
    @Description("Verifica que obtener horario semanal retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener horario semanal retorna 200 OK")
    void testGetWeekScheduleReturnsOk() throws Exception {
        when(scheduleService.getWeekSchedule(any(LocalDate.class))).thenReturn(List.of());
        var result = mockMvc.perform(get(API_BASE + "/week")
                .param("weekStart", "2026-06-01"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Obtener Horario de Empleado")
    @Description("Verifica que obtener horario de empleado retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener horario de empleado retorna 200 OK")
    void testGetEmployeeScheduleReturnsOk() throws Exception {
        when(scheduleService.getEmployeeSchedule(1L, LocalDate.of(2026, 6, 1))).thenReturn(List.of());
        var result = mockMvc.perform(get(API_BASE + "/employee/1")
                .param("weekStart", "2026-06-01"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Crear Entrada de Horario")
    @Description("Verifica que crear entrada retorna 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear entrada retorna 201")
    void testCreateReturnsCreated() throws Exception {
        WeeklyScheduleDTO dto = WeeklyScheduleDTO.builder()
                .id(1L).employeeId(1L).employeeName("John Doe")
                .weekStartDate(LocalDate.of(2026, 6, 1))
                .dayOfWeek(DayOfWeek.MONDAY).shift(WorkShift.FULL_DAY)
                .breakGroup(BreakGroup.A).isOvertime(false).build();
        when(scheduleService.createScheduleEntry(any(CreateScheduleRequest.class))).thenReturn(dto);

        CreateScheduleRequest req = CreateScheduleRequest.builder()
                .employeeId(1L).weekStartDate(LocalDate.of(2026, 6, 1))
                .dayOfWeek(DayOfWeek.MONDAY).shift(WorkShift.FULL_DAY)
                .breakGroup(BreakGroup.A).build();

        var result = mockMvc.perform(post(API_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    @Story("Eliminar Entrada")
    @Description("Verifica que eliminar entrada retorna 204")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar entrada retorna 204")
    void testDeleteReturnsNoContent() throws Exception {
        doNothing().when(scheduleService).deleteScheduleEntry(1L);
        mockMvc.perform(delete(API_BASE + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @Story("Copiar Semana")
    @Description("Verifica que copiar semana anterior retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Copiar semana anterior retorna 200 OK")
    void testCopyPreviousWeekReturnsOk() throws Exception {
        doNothing().when(scheduleService).copyPreviousWeek(any(LocalDate.class));
        var result = mockMvc.perform(post(API_BASE + "/copy-week")
                .param("targetWeekStart", "2026-06-08"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Validar Cobertura")
    @Description("Verifica que validar cobertura retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Validar cobertura retorna 200 OK")
    void testValidateCoverageReturnsOk() throws Exception {
        CoverageValidationResult validResult = CoverageValidationResult.builder()
                .valid(true).weekStartDate(LocalDate.of(2026, 6, 1))
                .issues(Collections.emptyList()).build();
        when(scheduleService.validateWeekCoverage(any(LocalDate.class))).thenReturn(validResult);

        var result = mockMvc.perform(get(API_BASE + "/validate")
                .param("weekStart", "2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Auto-asignar")
    @Description("Verifica que auto-asignar retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Auto-asignar retorna 200 OK")
    void testAutoAssignReturnsOk() throws Exception {
        doNothing().when(scheduleService).autoAssignWeek(any(LocalDate.class));
        var result = mockMvc.perform(post(API_BASE + "/auto-assign")
                .param("weekStart", "2026-06-01"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @TestConfiguration
    static class Config {
        @Bean
        public ScheduleService scheduleService() {
            return Mockito.mock(ScheduleService.class);
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
