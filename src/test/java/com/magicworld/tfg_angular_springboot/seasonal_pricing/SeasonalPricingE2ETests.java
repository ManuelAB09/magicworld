package com.magicworld.tfg_angular_springboot.seasonal_pricing;

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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@Epic("Gestión de Precios Estacionales")
@Feature("API REST de Precios Estacionales E2E")
public class SeasonalPricingE2ETests {

    private static final String API_BASE = "/api/v1/seasonal-pricing";
    private static final String RULE_NAME = "Summer Season";
    private static final String RULE_NAME_WINTER = "Winter Season";
    private static final BigDecimal MULTIPLIER = new BigDecimal("1.50");
    private static final BigDecimal MULTIPLIER_WINTER = new BigDecimal("2.00");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SeasonalPricingRepository repository;

    private SeasonalPricingRequest sampleRequest;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        sampleRequest = new SeasonalPricingRequest();
        sampleRequest.setName(RULE_NAME);
        sampleRequest.setStartDate(LocalDate.of(2026, 6, 1));
        sampleRequest.setEndDate(LocalDate.of(2026, 8, 31));
        sampleRequest.setMultiplier(MULTIPLIER);
        sampleRequest.setApplyOnWeekdays(true);
        sampleRequest.setApplyOnWeekends(true);
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Regla de Precio")
    @Description("Verifica que crear regla retorna 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear regla retorna 201")
    void testCreateReturnsCreated() throws Exception {
        var result = mockMvc.perform(post(API_BASE)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Regla de Precio")
    @Description("Verifica que crear regla retorna header Location")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear regla retorna header Location")
    void testCreateReturnsLocationHeader() throws Exception {
        var result = mockMvc.perform(post(API_BASE)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(header().exists("Location"))
                .andReturn();
        assertNotNull(result.getResponse().getHeader("Location"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Regla de Precio")
    @Description("Verifica que crear regla retorna ID generado")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear regla retorna ID")
    void testCreateReturnsId() throws Exception {
        var result = mockMvc.perform(post(API_BASE)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("id"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Listar Reglas")
    @Description("Verifica que obtener todas retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener todas retorna 200 OK")
    void testGetAllReturnsOk() throws Exception {
        var result = mockMvc.perform(get(API_BASE))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Listar Reglas")
    @Description("Verifica que obtener todas retorna datos correctos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener todas retorna datos correctos")
    void testGetAllWithDataReturnsRules() throws Exception {
        repository.save(SeasonalPricing.builder()
                .name(RULE_NAME).startDate(LocalDate.of(2026, 6, 1)).endDate(LocalDate.of(2026, 8, 31))
                .multiplier(MULTIPLIER).applyOnWeekdays(true).applyOnWeekends(true).build());

        var result = mockMvc.perform(get(API_BASE))
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(RULE_NAME));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Obtener Regla por ID")
    @Description("Verifica que obtener regla por ID retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener por ID retorna 200 OK")
    void testGetByIdReturnsOk() throws Exception {
        SeasonalPricing saved = repository.save(SeasonalPricing.builder()
                .name(RULE_NAME).startDate(LocalDate.of(2026, 6, 1)).endDate(LocalDate.of(2026, 8, 31))
                .multiplier(MULTIPLIER).applyOnWeekdays(true).applyOnWeekends(true).build());

        var result = mockMvc.perform(get(API_BASE + "/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(RULE_NAME))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Obtener Regla por ID")
    @Description("Verifica que obtener regla inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener por ID inexistente retorna 404")
    void testGetByIdNotFound() throws Exception {
        var result = mockMvc.perform(get(API_BASE + "/999999"))
                .andExpect(status().isNotFound())
                .andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Actualizar Regla")
    @Description("Verifica que actualizar regla retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar regla retorna 200 OK")
    void testUpdateReturnsOk() throws Exception {
        SeasonalPricing saved = repository.save(SeasonalPricing.builder()
                .name(RULE_NAME).startDate(LocalDate.of(2026, 6, 1)).endDate(LocalDate.of(2026, 8, 31))
                .multiplier(MULTIPLIER).applyOnWeekdays(true).applyOnWeekends(true).build());

        sampleRequest.setName(RULE_NAME_WINTER);
        sampleRequest.setMultiplier(MULTIPLIER_WINTER);

        var result = mockMvc.perform(put(API_BASE + "/" + saved.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(RULE_NAME_WINTER))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Eliminar Regla")
    @Description("Verifica que eliminar regla retorna 204 No Content")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar regla retorna 204")
    void testDeleteReturns204() throws Exception {
        SeasonalPricing saved = repository.save(SeasonalPricing.builder()
                .name(RULE_NAME).startDate(LocalDate.of(2026, 6, 1)).endDate(LocalDate.of(2026, 8, 31))
                .multiplier(MULTIPLIER).applyOnWeekdays(true).applyOnWeekends(true).build());

        var result = mockMvc.perform(delete(API_BASE + "/" + saved.getId()).with(csrf()))
                .andExpect(status().isNoContent())
                .andReturn();
        assertEquals(204, result.getResponse().getStatus());
    }

    @Test
    @Story("Seguridad API")
    @Description("Verifica que crear regla sin autenticación retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear regla sin autenticación retorna 401")
    void testCreateUnauthorized() throws Exception {
        var result = mockMvc.perform(post(API_BASE)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        assertEquals(401, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Seguridad API")
    @Description("Verifica que crear regla con rol USER retorna 403")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear regla con rol USER retorna 403")
    void testCreateForbidden() throws Exception {
        var result = mockMvc.perform(post(API_BASE)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isForbidden())
                .andReturn();
        assertEquals(403, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Obtener Multiplicador")
    @Description("Verifica que obtener multiplicador retorna 200 OK con valor correcto")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener multiplicador retorna 200 OK")
    void testGetMultiplierReturnsOk() throws Exception {
        repository.save(SeasonalPricing.builder()
                .name(RULE_NAME).startDate(LocalDate.of(2026, 6, 1)).endDate(LocalDate.of(2026, 8, 31))
                .multiplier(MULTIPLIER).applyOnWeekdays(true).applyOnWeekends(true).build());

        var result = mockMvc.perform(get(API_BASE + "/multiplier")
                        .param("date", "2026-07-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.multiplier").exists())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }
}
