package com.magicworld.tfg_angular_springboot.seasonal_pricing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SeasonalPricingController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Gestión de Precios Estacionales")
@Feature("API REST de Precios Estacionales")
public class SeasonalPricingControllerTests {

    private static final String API_BASE = "/api/v1/seasonal-pricing";
    private static final String RULE_NAME = "Summer Season";
    private static final BigDecimal MULTIPLIER = new BigDecimal("1.50");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SeasonalPricingService service;

    private SeasonalPricing sample() {
        return SeasonalPricing.builder()
                .name(RULE_NAME)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .multiplier(MULTIPLIER)
                .applyOnWeekdays(true)
                .applyOnWeekends(true)
                .build();
    }

    private SeasonalPricingRequest sampleRequest() {
        SeasonalPricingRequest req = new SeasonalPricingRequest();
        req.setName(RULE_NAME);
        req.setStartDate(LocalDate.of(2026, 6, 1));
        req.setEndDate(LocalDate.of(2026, 8, 31));
        req.setMultiplier(MULTIPLIER);
        req.setApplyOnWeekdays(true);
        req.setApplyOnWeekends(true);
        return req;
    }

    @Test
    @Story("Listar Reglas de Precio")
    @Description("Verifica que obtener todas las reglas retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener todas las reglas retorna 200 OK")
    void testGetAllReturnsOk() throws Exception {
        SeasonalPricing sp = sample();
        sp.setId(1L);
        when(service.findAll()).thenReturn(List.of(sp));

        var result = mockMvc.perform(get(API_BASE).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Obtener Regla por ID")
    @Description("Verifica que obtener regla por ID existente retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener regla por ID existente retorna 200 OK")
    void testGetByIdFoundReturnsOk() throws Exception {
        SeasonalPricing sp = sample();
        sp.setId(1L);
        when(service.findById(1L)).thenReturn(sp);

        var result = mockMvc.perform(get(API_BASE + "/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Obtener Regla por ID")
    @Description("Verifica que obtener regla por ID inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener regla por ID inexistente retorna 404")
    void testGetByIdNotFound() throws Exception {
        when(service.findById(999L)).thenThrow(new ResourceNotFoundException("error.seasonal_pricing.notfound"));

        var result = mockMvc.perform(get(API_BASE + "/999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }

    @Test
    @Story("Obtener Multiplicador")
    @Description("Verifica que obtener multiplicador por fecha retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener multiplicador por fecha retorna 200 OK")
    void testGetMultiplierReturnsOk() throws Exception {
        when(service.getMultiplier(LocalDate.of(2026, 7, 15))).thenReturn(MULTIPLIER);

        var result = mockMvc.perform(get(API_BASE + "/multiplier")
                .param("date", "2026-07-15")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.multiplier").value(1.50))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Crear Regla de Precio")
    @Description("Verifica que crear regla retorna 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear regla retorna 201 Created")
    void testCreateReturnsCreated() throws Exception {
        SeasonalPricing saved = sample();
        saved.setId(1L);
        when(service.save(any(SeasonalPricing.class))).thenReturn(saved);

        var result = mockMvc.perform(post(API_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    @Story("Actualizar Regla de Precio")
    @Description("Verifica que actualizar regla retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar regla retorna 200 OK")
    void testUpdateReturnsOk() throws Exception {
        SeasonalPricing updated = sample();
        updated.setId(1L);
        when(service.update(eq(1L), any(SeasonalPricing.class))).thenReturn(updated);

        var result = mockMvc.perform(put(API_BASE + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Eliminar Regla de Precio")
    @Description("Verifica que eliminar regla retorna 204 No Content")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar regla retorna 204 No Content")
    void testDeleteReturnsNoContent() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete(API_BASE + "/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @Story("Validación de Entrada")
    @Description("Verifica que crear regla con datos inválidos retorna 400")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear regla con datos inválidos retorna 400")
    void testCreateBadRequest() throws Exception {
        SeasonalPricingRequest invalid = new SeasonalPricingRequest();

        var result = mockMvc.perform(post(API_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    @TestConfiguration
    static class Config {
        @Bean
        public SeasonalPricingService seasonalPricingService() {
            return Mockito.mock(SeasonalPricingService.class);
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
