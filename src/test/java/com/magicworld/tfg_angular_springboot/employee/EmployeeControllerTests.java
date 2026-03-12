package com.magicworld.tfg_angular_springboot.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
import com.magicworld.tfg_angular_springboot.employee.controller.EmployeeController;
import com.magicworld.tfg_angular_springboot.employee.dto.CreateEmployeeRequest;
import com.magicworld.tfg_angular_springboot.employee.dto.EmployeeDTO;
import com.magicworld.tfg_angular_springboot.employee.service.EmployeeService;
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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Gestión de Empleados")
@Feature("API REST de Empleados")
public class EmployeeControllerTests {

    private static final String API_BASE = "/api/v1/employees";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeService employeeService;

    private EmployeeDTO sampleDTO() {
        return EmployeeDTO.builder()
                .id(1L).firstName("John").lastName("Doe").fullName("John Doe")
                .email("john@example.com").phone("+123456789")
                .role(EmployeeRole.OPERATOR).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now())
                .build();
    }

    @Test
    @Story("Listar Empleados")
    @Description("Verifica que obtener todos retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener todos retorna 200 OK")
    void testGetAllReturnsOk() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(sampleDTO()));
        var result = mockMvc.perform(get(API_BASE).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Listar Empleados Activos")
    @Description("Verifica que obtener activos retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener activos retorna 200 OK")
    void testGetActiveReturnsOk() throws Exception {
        when(employeeService.getActiveEmployees()).thenReturn(List.of(sampleDTO()));
        var result = mockMvc.perform(get(API_BASE + "/active").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Filtrar por Rol")
    @Description("Verifica que obtener por rol retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener por rol retorna 200 OK")
    void testGetByRoleReturnsOk() throws Exception {
        when(employeeService.getEmployeesByRole(EmployeeRole.OPERATOR)).thenReturn(List.of(sampleDTO()));
        var result = mockMvc.perform(get(API_BASE + "/role/OPERATOR").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Obtener por ID")
    @Description("Verifica que obtener por ID retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener por ID retorna 200 OK")
    void testGetByIdReturnsOk() throws Exception {
        when(employeeService.getEmployee(1L)).thenReturn(sampleDTO());
        var result = mockMvc.perform(get(API_BASE + "/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Crear Empleado")
    @Description("Verifica que crear empleado retorna 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear empleado retorna 201")
    void testCreateReturnsCreated() throws Exception {
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(sampleDTO());
        CreateEmployeeRequest req = CreateEmployeeRequest.builder()
                .firstName("John").lastName("Doe").email("john@example.com")
                .phone("+123456789").role(EmployeeRole.OPERATOR).build();

        var result = mockMvc.perform(post(API_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    @Story("Actualizar Empleado")
    @Description("Verifica que actualizar empleado retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar empleado retorna 200 OK")
    void testUpdateReturnsOk() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(CreateEmployeeRequest.class))).thenReturn(sampleDTO());
        CreateEmployeeRequest req = CreateEmployeeRequest.builder()
                .firstName("John").lastName("Doe").email("john@example.com")
                .phone("+123456789").role(EmployeeRole.OPERATOR).build();

        var result = mockMvc.perform(put(API_BASE + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Eliminar Empleado")
    @Description("Verifica que eliminar empleado retorna 204 No Content")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar empleado retorna 204")
    void testTerminateReturnsNoContent() throws Exception {
        doNothing().when(employeeService).terminateEmployee(1L);
        mockMvc.perform(post(API_BASE + "/1/terminate").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @TestConfiguration
    static class Config {
        @Bean
        public EmployeeService employeeService() {
            return Mockito.mock(EmployeeService.class);
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
