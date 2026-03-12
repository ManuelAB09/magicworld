package com.magicworld.tfg_angular_springboot.monitoring;

import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
import com.magicworld.tfg_angular_springboot.monitoring.controller.ParkStatusController;
import com.magicworld.tfg_angular_springboot.monitoring.dto.AttractionStatus;
import com.magicworld.tfg_angular_springboot.monitoring.dto.DashboardSnapshot;
import com.magicworld.tfg_angular_springboot.monitoring.service.DashboardService;
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
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ParkStatusController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Monitorización del Parque")
@Feature("API de Estado del Parque")
public class ParkStatusControllerTests {

    private static final String API_SIMULATOR = "/api/v1/park-status/simulator";
    private static final String API_ATTRACTIONS = "/api/v1/park-status/attractions";
    private static final String FIELD_RUNNING = "running";
    private static final String FIELD_SIMULATED_VISITORS = "simulatedVisitors";
    private static final String FIELD_ATTRACTION_ID = "attractionId";
    private static final String FIELD_NAME = "name";
    private static final String TEST_ATTRACTION_NAME = "Test Coaster";
    private static final int TEST_QUEUE_SIZE = 25;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParkSimulatorService simulatorService;

    @Autowired
    private DashboardService dashboardService;

    @Test
    @Story("Estado del Simulador")
    @Description("Verifica que obtener estado del simulador retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getSimulatorStatus retorna 200 OK")
    void testGetSimulatorStatusReturnsOk() throws Exception {
        when(simulatorService.getSimulatorStatus()).thenReturn(
                Map.of(FIELD_RUNNING, false, FIELD_SIMULATED_VISITORS, 0, "activeQueues", 0, "totalInQueues", 0));

        mockMvc.perform(get(API_SIMULATOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + FIELD_RUNNING).value(false));
    }

    @Test
    @Story("Estado del Simulador")
    @Description("Verifica que el simulador running retorna visitantes simulados")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getSimulatorStatus running retorna visitantes")
    void testGetSimulatorStatusRunningReturnsVisitors() throws Exception {
        when(simulatorService.getSimulatorStatus()).thenReturn(
                Map.of(FIELD_RUNNING, true, FIELD_SIMULATED_VISITORS, 150, "activeQueues", 5, "totalInQueues", 80));

        mockMvc.perform(get(API_SIMULATOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + FIELD_RUNNING).value(true))
                .andExpect(jsonPath("$." + FIELD_SIMULATED_VISITORS).value(150));
    }

    @Test
    @Story("Estado de Atracciones")
    @Description("Verifica que obtener estados de atracciones retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getAttractionStatuses retorna 200 OK")
    void testGetAttractionStatusesReturnsOk() throws Exception {
        DashboardSnapshot snapshot = DashboardSnapshot.builder()
                .attractionStatuses(Collections.emptyList())
                .build();
        when(dashboardService.getSnapshot()).thenReturn(snapshot);

        mockMvc.perform(get(API_ATTRACTIONS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Story("Estado de Atracciones")
    @Description("Verifica que obtener estados con datos retorna lista de atracciones")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getAttractionStatuses con datos retorna atracciones")
    void testGetAttractionStatusesWithDataReturnsList() throws Exception {
        AttractionStatus status = AttractionStatus.builder()
                .attractionId(1L)
                .name(TEST_ATTRACTION_NAME)
                .open(true)
                .queueSize(TEST_QUEUE_SIZE)
                .estimatedWaitMinutes(5)
                .mapPositionX(10.0)
                .mapPositionY(20.0)
                .intensity("HIGH")
                .build();
        DashboardSnapshot snapshot = DashboardSnapshot.builder()
                .attractionStatuses(List.of(status))
                .build();
        when(dashboardService.getSnapshot()).thenReturn(snapshot);

        mockMvc.perform(get(API_ATTRACTIONS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0]." + FIELD_NAME).value(TEST_ATTRACTION_NAME))
                .andExpect(jsonPath("$[0].queueSize").value(TEST_QUEUE_SIZE));
    }

    @TestConfiguration
    static class Config {
        @Bean
        public ParkSimulatorService parkSimulatorService() { return Mockito.mock(ParkSimulatorService.class); }
        @Bean
        public DashboardService dashboardService() { return Mockito.mock(DashboardService.class); }
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

