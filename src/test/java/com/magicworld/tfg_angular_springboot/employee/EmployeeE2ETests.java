package com.magicworld.tfg_angular_springboot.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.employee.dto.CreateEmployeeRequest;
import io.qameta.allure.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@Epic("Gestión de Empleados")
@Feature("API REST de Empleados E2E")
public class EmployeeE2ETests {

        private static final String API_BASE = "/api/v1/employees";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private EmployeeRepository employeeRepository;

        @BeforeEach
        void setUp() {
                employeeRepository.deleteAll();
        }

        @AfterEach
        void tearDown() {
                employeeRepository.deleteAll();
        }

        private CreateEmployeeRequest sampleRequest(String email) {
                return CreateEmployeeRequest.builder()
                                .firstName("John").lastName("Doe").email(email)
                                .phone("+123456789").role(EmployeeRole.OPERATOR).build();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @Story("Crear Empleado")
        @Description("Verifica que crear empleado retorna 201")
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Crear empleado retorna 201")
        void testCreateReturnsCreated() throws Exception {
                var result = mockMvc.perform(post(API_BASE)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sampleRequest("john@test.com"))))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").exists())
                                .andReturn();
                assertEquals(201, result.getResponse().getStatus());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @Story("Listar Empleados")
        @Description("Verifica que obtener todos retorna 200 OK")
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Obtener todos retorna 200 OK")
        void testGetAllReturnsOk() throws Exception {
                var result = mockMvc.perform(get(API_BASE))
                                .andExpect(status().isOk())
                                .andReturn();
                assertEquals(200, result.getResponse().getStatus());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @Story("Listar Empleados")
        @Description("Verifica que obtener todos retorna datos correctos")
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("Obtener todos retorna datos")
        void testGetAllWithDataReturnsEmployees() throws Exception {
                mockMvc.perform(post(API_BASE)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sampleRequest("emp1@test.com"))))
                                .andExpect(status().isCreated());

                var result = mockMvc.perform(get(API_BASE))
                                .andExpect(jsonPath("$.length()").value(1))
                                .andReturn();
                assertTrue(result.getResponse().getContentAsString().contains("John"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @Story("Listar Empleados Activos")
        @Description("Verifica que obtener activos retorna 200 OK")
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("Obtener activos retorna 200 OK")
        void testGetActiveReturnsOk() throws Exception {
                var result = mockMvc.perform(get(API_BASE + "/active"))
                                .andExpect(status().isOk())
                                .andReturn();
                assertEquals(200, result.getResponse().getStatus());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @Story("Filtrar por Rol")
        @Description("Verifica que obtener por rol retorna 200 OK")
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("Obtener por rol retorna 200 OK")
        void testGetByRoleReturnsOk() throws Exception {
                var result = mockMvc.perform(get(API_BASE + "/role/OPERATOR"))
                                .andExpect(status().isOk())
                                .andReturn();
                assertEquals(200, result.getResponse().getStatus());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @Story("Actualizar Empleado")
        @Description("Verifica que actualizar empleado retorna 200 OK")
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Actualizar empleado retorna 200")
        void testUpdateReturnsOk() throws Exception {
                var createResult = mockMvc.perform(post(API_BASE)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sampleRequest("upd@test.com"))))
                                .andExpect(status().isCreated())
                                .andReturn();
                String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

                CreateEmployeeRequest update = CreateEmployeeRequest.builder()
                                .firstName("Jane").lastName("Smith").email("upd@test.com")
                                .phone("+987654321").role(EmployeeRole.SECURITY).build();

                var result = mockMvc.perform(put(API_BASE + "/" + id)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.firstName").value("Jane"))
                                .andReturn();
                assertEquals(200, result.getResponse().getStatus());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @Story("Eliminar Empleado")
        @Description("Verifica que eliminar empleado retorna 204")
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Eliminar empleado retorna 204")
        void testTerminateReturns204() throws Exception {
                var createResult = mockMvc.perform(post(API_BASE)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sampleRequest("del@test.com"))))
                                .andExpect(status().isCreated())
                                .andReturn();
                String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

                var result = mockMvc.perform(post(API_BASE + "/" + id + "/terminate").with(csrf()))
                                .andExpect(status().isNoContent())
                                .andReturn();
                assertEquals(204, result.getResponse().getStatus());
        }

        @Test
        @Story("Seguridad API")
        @Description("Verifica que crear empleado sin autenticación retorna 401")
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Crear sin autenticación retorna 401")
        void testCreateUnauthorized() throws Exception {
                var result = mockMvc.perform(post(API_BASE)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sampleRequest("unauth@test.com"))))
                                .andExpect(status().isUnauthorized())
                                .andReturn();
                assertEquals(401, result.getResponse().getStatus());
        }

        @Test
        @WithMockUser(roles = "USER")
        @Story("Seguridad API")
        @Description("Verifica que crear empleado con rol USER retorna 403")
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Crear con rol USER retorna 403")
        void testCreateForbidden() throws Exception {
                var result = mockMvc.perform(post(API_BASE)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sampleRequest("forbid@test.com"))))
                                .andExpect(status().isForbidden())
                                .andReturn();
                assertEquals(403, result.getResponse().getStatus());
        }
}
