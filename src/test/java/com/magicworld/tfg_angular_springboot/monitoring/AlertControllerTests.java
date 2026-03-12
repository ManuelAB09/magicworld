package com.magicworld.tfg_angular_springboot.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
import com.magicworld.tfg_angular_springboot.monitoring.alert.AlertSeverity;
import com.magicworld.tfg_angular_springboot.monitoring.alert.AlertType;
import com.magicworld.tfg_angular_springboot.monitoring.controller.AlertController;
import com.magicworld.tfg_angular_springboot.monitoring.dto.AlertDTO;
import com.magicworld.tfg_angular_springboot.monitoring.dto.ResolutionResult;
import com.magicworld.tfg_angular_springboot.monitoring.dto.ResolveAlertRequest;
import com.magicworld.tfg_angular_springboot.monitoring.service.AlertService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AlertController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Monitorización del Parque")
@Feature("API de Alertas")
public class AlertControllerTests {

    private static final String API_ALERTS = "/api/v1/monitoring/alerts";
    private static final String TEST_ALERT_MESSAGE = "alerts.messages.high_queue";
    private static final String TEST_SUGGESTION = "alerts.suggestions.high_queue";
    private static final String RESOLUTION_ACKNOWLEDGE = "acknowledge";
    private static final String RESOLUTION_SUCCESS_MSG = "Alert acknowledged and marked as handled";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AlertService alertService;

    @Test
    @Story("Obtener Alertas Activas")
    @Description("Verifica que obtener alertas activas retorna 200 OK con lista vacía")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getActiveAlerts vacías retorna 200 OK")
    void testGetActiveAlertsEmptyReturnsOk() throws Exception {
        when(alertService.getActiveAlerts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(API_ALERTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Story("Obtener Alertas Activas")
    @Description("Verifica que obtener alertas activas con datos retorna lista")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getActiveAlerts con datos retorna lista")
    void testGetActiveAlertsWithDataReturnsList() throws Exception {
        AlertDTO alertDTO = AlertDTO.builder()
                .id(1L)
                .alertType(AlertType.HIGH_QUEUE)
                .severity(AlertSeverity.WARNING)
                .message(TEST_ALERT_MESSAGE)
                .suggestion(TEST_SUGGESTION)
                .attractionId(1L)
                .attractionName("Test Coaster")
                .timestamp(LocalDateTime.now())
                .active(true)
                .build();
        when(alertService.getActiveAlerts()).thenReturn(List.of(alertDTO));

        mockMvc.perform(get(API_ALERTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].alertType").value("HIGH_QUEUE"));
    }

    @Test
    @Story("Resolver Alerta")
    @Description("Verifica que resolver alerta retorna 200 OK con resultado exitoso")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("resolveAlert exitoso retorna 200 OK")
    void testResolveAlertSuccessReturnsOk() throws Exception {
        ResolutionResult result = ResolutionResult.builder()
                .success(true)
                .message(RESOLUTION_SUCCESS_MSG)
                .actionTaken("ACKNOWLEDGED")
                .resourcesUsed(Map.of("acknowledgedAt", "2026-03-12T10:00:00"))
                .build();
        when(alertService.resolveAlert(eq(1L), any(ResolveAlertRequest.class))).thenReturn(result);

        ResolveAlertRequest request = ResolveAlertRequest.builder()
                .resolutionOptionId(RESOLUTION_ACKNOWLEDGE)
                .build();

        mockMvc.perform(post(API_ALERTS + "/1/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.actionTaken").value("ACKNOWLEDGED"));
    }

    @Test
    @Story("Resolver Alerta")
    @Description("Verifica que resolver alerta no encontrada retorna resultado fallido")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("resolveAlert no encontrada retorna fallo")
    void testResolveAlertNotFoundReturnsFailed() throws Exception {
        ResolutionResult result = ResolutionResult.builder()
                .success(false)
                .failureReason("Alert not found")
                .code("alerts.resolution.alert_not_found")
                .build();
        when(alertService.resolveAlert(eq(999L), any(ResolveAlertRequest.class))).thenReturn(result);

        ResolveAlertRequest request = ResolveAlertRequest.builder()
                .resolutionOptionId(RESOLUTION_ACKNOWLEDGE)
                .build();

        mockMvc.perform(post(API_ALERTS + "/999/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @TestConfiguration
    static class Config {
        @Bean
        public AlertService alertService() { return Mockito.mock(AlertService.class); }
        @Bean
        public JwtService jwtService() { return Mockito.mock(JwtService.class); }
        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() { return Mockito.mock(JwtAuthenticationFilter.class); }
        @Bean
        public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() { return Mockito.mock(OAuth2AuthenticationSuccessHandler.class); }
        @Bean
        public OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() { return Mockito.mock(OAuth2AuthenticationFailureHandler.class); }
    }
}

