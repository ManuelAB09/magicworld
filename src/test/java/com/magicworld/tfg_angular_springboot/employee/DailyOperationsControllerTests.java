package com.magicworld.tfg_angular_springboot.employee;

import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
import com.magicworld.tfg_angular_springboot.employee.controller.DailyOperationsController;
import com.magicworld.tfg_angular_springboot.employee.dto.AvailableEmployeesResponse;
import com.magicworld.tfg_angular_springboot.employee.dto.DailyAssignmentDTO;
import com.magicworld.tfg_angular_springboot.employee.service.DailyOperationsService;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DailyOperationsController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Gestión de Operaciones Diarias")
@Feature("API REST de Operaciones Diarias")
public class DailyOperationsControllerTests {

    private static final String API_BASE = "/api/v1/daily-operations";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DailyOperationsService dailyOperationsService;

    @Test
    @Story("Inicializar Día")
    @Description("Verifica que inicializar día retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Inicializar día retorna 200 OK")
    void testInitializeDayReturnsOk() throws Exception {
        doNothing().when(dailyOperationsService).initializeDay(any(LocalDate.class));
        var result = mockMvc.perform(post(API_BASE + "/initialize")
                .param("date", "2026-06-01"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Obtener Asignaciones de Hoy")
    @Description("Verifica que obtener asignaciones de hoy retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener asignaciones de hoy retorna 200 OK")
    void testGetTodayAssignmentsReturnsOk() throws Exception {
        when(dailyOperationsService.getTodayAssignments()).thenReturn(List.of());
        var result = mockMvc.perform(get(API_BASE + "/today"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Obtener Asignaciones por Fecha")
    @Description("Verifica que obtener asignaciones por fecha retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener por fecha retorna 200 OK")
    void testGetByDateReturnsOk() throws Exception {
        when(dailyOperationsService.getAssignmentsForDate(any(LocalDate.class))).thenReturn(List.of());
        var result = mockMvc.perform(get(API_BASE + "/date")
                .param("date", "2026-06-01"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Empleados Disponibles")
    @Description("Verifica que obtener disponibles retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener disponibles retorna 200 OK")
    void testGetAvailableReturnsOk() throws Exception {
        AvailableEmployeesResponse response = AvailableEmployeesResponse.builder()
                .employees(Collections.emptyList()).reinforcements(Collections.emptyList())
                .hasAvailable(false).hasReinforcements(false).build();
        when(dailyOperationsService.getAvailableEmployees(EmployeeRole.OPERATOR)).thenReturn(response);

        var result = mockMvc.perform(get(API_BASE + "/available")
                .param("role", "OPERATOR"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Asignar a Alerta")
    @Description("Verifica que asignar a alerta retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Asignar a alerta retorna 200 OK")
    void testAssignToAlertReturnsOk() throws Exception {
        DailyAssignmentDTO dto = DailyAssignmentDTO.builder()
                .id(1L).employeeId(1L).employeeName("John Doe").employeeRole(EmployeeRole.OPERATOR)
                .assignmentDate(LocalDate.now()).currentStatus(DailyStatus.ASSIGNED_TO_ALERT)
                .build();
        when(dailyOperationsService.assignEmployeeToAlert(1L, 1L)).thenReturn(dto);

        var result = mockMvc.perform(post(API_BASE + "/assign-to-alert")
                .param("employeeId", "1").param("alertId", "1"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Liberar de Alerta")
    @Description("Verifica que liberar de alerta retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Liberar de alerta retorna 200 OK")
    void testReleaseFromAlertReturnsOk() throws Exception {
        DailyAssignmentDTO dto = DailyAssignmentDTO.builder()
                .id(1L).employeeId(1L).employeeName("John Doe").employeeRole(EmployeeRole.OPERATOR)
                .assignmentDate(LocalDate.now()).currentStatus(DailyStatus.WORKING)
                .build();
        when(dailyOperationsService.releaseEmployeeFromAlert(1L)).thenReturn(dto);

        var result = mockMvc.perform(post(API_BASE + "/release-from-alert")
                .param("employeeId", "1"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Llamar Refuerzo")
    @Description("Verifica que llamar refuerzo retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Llamar refuerzo retorna 200 OK")
    void testCallReinforcementReturnsOk() throws Exception {
        ReinforcementCall call = ReinforcementCall.builder()
                .status(ReinforcementStatus.PENDING).build();
        call.setId(1L);
        when(dailyOperationsService.callReinforcement(1L, null)).thenReturn(call);

        var result = mockMvc.perform(post(API_BASE + "/call-reinforcement")
                .param("employeeId", "1"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Llamar Refuerzo")
    @Description("Verifica que llamar refuerzo con alerta retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Llamar refuerzo con alerta retorna 200 OK")
    void testCallReinforcementWithAlertReturnsOk() throws Exception {
        ReinforcementCall call = ReinforcementCall.builder()
                .status(ReinforcementStatus.PENDING).build();
        call.setId(1L);
        when(dailyOperationsService.callReinforcement(1L, 5L)).thenReturn(call);

        var result = mockMvc.perform(post(API_BASE + "/call-reinforcement")
                .param("employeeId", "1").param("alertId", "5"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Actualizar Estado de Refuerzo")
    @Description("Verifica que actualizar estado de refuerzo retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar estado de refuerzo retorna 200 OK")
    void testUpdateReinforcementStatusReturnsOk() throws Exception {
        ReinforcementCall call = ReinforcementCall.builder()
                .status(ReinforcementStatus.ACCEPTED).build();
        call.setId(1L);
        when(dailyOperationsService.updateReinforcementStatus(1L, ReinforcementStatus.ACCEPTED)).thenReturn(call);

        var result = mockMvc.perform(post(API_BASE + "/reinforcement/1/status")
                .param("status", "ACCEPTED"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @TestConfiguration
    static class Config {
        @Bean
        public DailyOperationsService dailyOperationsService() {
            return Mockito.mock(DailyOperationsService.class);
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
