package com.magicworld.tfg_angular_springboot.park_closure;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@Epic("Gestión de Cierres del Parque")
@Feature("API REST de Cierres del Parque E2E")
public class ParkClosureDayE2ETests {

    private static final String API_BASE = "/api/v1/park-closures";
    private static final String CLOSURE_REASON = "Holiday Closure";
    private static final String CLOSURE_REASON_2 = "Storm Warning";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParkClosureDayRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    private ParkClosureDayRequest buildRequest(LocalDate date, String reason) {
        ParkClosureDayRequest req = new ParkClosureDayRequest();
        req.setClosureDate(date);
        req.setReason(reason);
        return req;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Cierre")
    @Description("Verifica que crear cierre retorna 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear cierre retorna 201")
    void testCreateReturnsCreated() throws Exception {
        ParkClosureDayRequest request = buildRequest(LocalDate.now().plusMonths(2), CLOSURE_REASON);

        var result = mockMvc.perform(post(API_BASE)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Cierre")
    @Description("Verifica que crear cierre retorna header Location")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear cierre retorna header Location")
    void testCreateReturnsLocationHeader() throws Exception {
        ParkClosureDayRequest request = buildRequest(LocalDate.now().plusMonths(2), CLOSURE_REASON);

        var result = mockMvc.perform(post(API_BASE)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(header().exists("Location"))
                .andReturn();
        assertNotNull(result.getResponse().getHeader("Location"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Listar Cierres")
    @Description("Verifica que obtener todos los cierres retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener todos los cierres retorna 200 OK")
    void testGetAllReturnsOk() throws Exception {
        var result = mockMvc.perform(get(API_BASE))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Listar Cierres")
    @Description("Verifica que obtener cierres con datos retorna datos correctos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener cierres con datos")
    void testGetAllWithDataReturnsClosure() throws Exception {
        repository.save(ParkClosureDay.builder()
                .closureDate(LocalDate.of(2026, 12, 25)).reason(CLOSURE_REASON).build());

        var result = mockMvc.perform(get(API_BASE))
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(CLOSURE_REASON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Listar Cierres por Rango")
    @Description("Verifica que filtrar por rango de fechas funciona")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Filtrar por rango de fechas")
    void testGetAllByRange() throws Exception {
        repository.save(ParkClosureDay.builder()
                .closureDate(LocalDate.of(2026, 12, 25)).reason(CLOSURE_REASON).build());
        repository.save(ParkClosureDay.builder()
                .closureDate(LocalDate.of(2027, 1, 1)).reason(CLOSURE_REASON_2).build());

        var result = mockMvc.perform(get(API_BASE)
                        .param("from", "2026-12-01")
                        .param("to", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(CLOSURE_REASON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Verificar Cierre")
    @Description("Verifica que comprobar día cerrado retorna true")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Comprobar día cerrado retorna true")
    void testIsClosedDayReturnsTrue() throws Exception {
        repository.save(ParkClosureDay.builder()
                .closureDate(LocalDate.of(2026, 12, 25)).reason(CLOSURE_REASON).build());

        var result = mockMvc.perform(get(API_BASE + "/check")
                        .param("date", "2026-12-25"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Verificar Cierre")
    @Description("Verifica que comprobar día abierto retorna false")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Comprobar día abierto retorna false")
    void testIsClosedDayReturnsFalse() throws Exception {
        var result = mockMvc.perform(get(API_BASE + "/check")
                        .param("date", "2026-12-26"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Eliminar Cierre")
    @Description("Verifica que eliminar cierre retorna 204 No Content")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar cierre retorna 204")
    void testDeleteReturns204() throws Exception {
        ParkClosureDay saved = repository.save(ParkClosureDay.builder()
                .closureDate(LocalDate.now().plusMonths(3)).reason(CLOSURE_REASON).build());

        var result = mockMvc.perform(delete(API_BASE + "/" + saved.getId()).with(csrf()))
                .andExpect(status().isNoContent())
                .andReturn();
        assertEquals(204, result.getResponse().getStatus());
    }

    @Test
    @Story("Seguridad API")
    @Description("Verifica que crear cierre sin autenticación retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear cierre sin autenticación retorna 401")
    void testCreateUnauthorized() throws Exception {
        ParkClosureDayRequest request = buildRequest(LocalDate.now().plusMonths(2), CLOSURE_REASON);

        var result = mockMvc.perform(post(API_BASE)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        assertEquals(401, result.getResponse().getStatus());
    }
}
