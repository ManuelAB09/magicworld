package com.magicworld.tfg_angular_springboot.monitoring;

import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
import com.magicworld.tfg_angular_springboot.monitoring.controller.SimulatorController;
import com.magicworld.tfg_angular_springboot.monitoring.dto.DashboardSnapshot;
import com.magicworld.tfg_angular_springboot.monitoring.service.DashboardService;
import com.magicworld.tfg_angular_springboot.monitoring.service.MonitoringWebSocketService;
import com.magicworld.tfg_angular_springboot.monitoring.simulator.ParkSimulatorService;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SimulatorController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Monitorización del Parque")
@Feature("API del Simulador")
public class SimulatorControllerTests {

    private static final String API_BASE = "/api/v1/monitoring/simulator";
    private static final String FIELD_RUNNING = "running";
    private static final String FIELD_SIMULATED_VISITORS = "simulatedVisitors";
    private static final int VISITORS_COUNT = 100;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParkSimulatorService simulatorService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private MonitoringWebSocketService webSocketService;

    @Test
    @Story("Iniciar Simulador")
    @Description("Verifica que iniciar simulador retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("POST /start retorna 200 OK")
    void testStartReturnsOk() throws Exception {
        doNothing().when(simulatorService).start();
        when(simulatorService.getSimulatorStatus()).thenReturn(
                Map.of(FIELD_RUNNING, true, FIELD_SIMULATED_VISITORS, VISITORS_COUNT, "activeQueues", 3, "totalInQueues", 50));

        mockMvc.perform(post(API_BASE + "/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + FIELD_RUNNING).value(true))
                .andExpect(jsonPath("$." + FIELD_SIMULATED_VISITORS).value(VISITORS_COUNT));
    }

    @Test
    @Story("Detener Simulador")
    @Description("Verifica que detener simulador retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("POST /stop retorna 200 OK")
    void testStopReturnsOk() throws Exception {
        doNothing().when(simulatorService).stop();
        when(simulatorService.getSimulatorStatus()).thenReturn(
                Map.of(FIELD_RUNNING, false, FIELD_SIMULATED_VISITORS, 0, "activeQueues", 0, "totalInQueues", 0));

        mockMvc.perform(post(API_BASE + "/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + FIELD_RUNNING).value(false));
    }

    @Test
    @Story("Estado del Simulador")
    @Description("Verifica que obtener estado retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /status retorna 200 OK")
    void testGetStatusReturnsOk() throws Exception {
        when(simulatorService.getSimulatorStatus()).thenReturn(
                Map.of(FIELD_RUNNING, false, FIELD_SIMULATED_VISITORS, 0, "activeQueues", 0, "totalInQueues", 0));

        mockMvc.perform(get(API_BASE + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + FIELD_RUNNING).isBoolean());
    }

    @Test
    @Story("Forzar Broadcast")
    @Description("Verifica que forzar broadcast retorna 200 OK y llama a webSocket")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("POST /broadcast retorna 200 OK")
    void testForceBroadcastReturnsOk() throws Exception {
        DashboardSnapshot snapshot = DashboardSnapshot.builder()
                .attractionStatuses(Collections.emptyList())
                .activeAlerts(Collections.emptyList())
                .build();
        when(dashboardService.getSnapshot()).thenReturn(snapshot);
        doNothing().when(webSocketService).broadcastDashboard(any());

        mockMvc.perform(post(API_BASE + "/broadcast"))
                .andExpect(status().isOk());

        verify(webSocketService).broadcastDashboard(any(DashboardSnapshot.class));
    }

    @TestConfiguration
    static class Config {
        @Bean
        public ParkSimulatorService parkSimulatorService() { return Mockito.mock(ParkSimulatorService.class); }
        @Bean
        public DashboardService dashboardService() { return Mockito.mock(DashboardService.class); }
        @Bean
        public MonitoringWebSocketService monitoringWebSocketService() { return Mockito.mock(MonitoringWebSocketService.class); }
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

