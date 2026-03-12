package com.magicworld.tfg_angular_springboot.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.AttractionCategory;
import com.magicworld.tfg_angular_springboot.attraction.AttractionRepository;
import com.magicworld.tfg_angular_springboot.attraction.Intensity;
import com.magicworld.tfg_angular_springboot.attraction.MaintenanceStatus;
import com.magicworld.tfg_angular_springboot.monitoring.alert.AlertSeverity;
import com.magicworld.tfg_angular_springboot.monitoring.alert.AlertType;
import com.magicworld.tfg_angular_springboot.monitoring.alert.ParkAlert;
import com.magicworld.tfg_angular_springboot.monitoring.alert.ParkAlertRepository;
import com.magicworld.tfg_angular_springboot.monitoring.dto.EventRequest;
import com.magicworld.tfg_angular_springboot.monitoring.dto.ResolveAlertRequest;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventRepository;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventType;
import com.magicworld.tfg_angular_springboot.monitoring.service.MonitoringWebSocketService;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Epic("Monitorización del Parque")
@Feature("E2E de Monitorización")
public class MonitoringE2ETests {

    private static final String API_MONITORING = "/api/v1/monitoring";
    private static final String API_PARK_STATUS = "/api/v1/park-status";
    private static final String API_SIMULATOR = "/api/v1/monitoring/simulator";
    private static final String API_ALERTS = "/api/v1/monitoring/alerts";
    private static final String TEST_ATTRACTION_NAME = "E2E Coaster";
    private static final String TEST_DESCRIPTION = "Test attraction";
    private static final String TEST_PHOTO = "http://example.com/img.jpg";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private ParkAlertRepository alertRepository;

    @Autowired
    private ParkEventRepository eventRepository;

    @MockitoBean
    private MonitoringWebSocketService webSocketService;

    private Attraction testAttraction;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        alertRepository.deleteAll();

        testAttraction = attractionRepository.save(Attraction.builder()
                .name(TEST_ATTRACTION_NAME)
                .intensity(Intensity.MEDIUM)
                .category(AttractionCategory.ROLLER_COASTER)
                .minimumHeight(100).minimumAge(8).minimumWeight(25)
                .description(TEST_DESCRIPTION).photoUrl(TEST_PHOTO)
                .isActive(true).maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .mapPositionX(25.0).mapPositionY(75.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(21, 0))
                .build());
    }

    @Test
    @Story("Park Status Público")
    @Description("Verifica que el estado del simulador es accesible sin autenticación")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /park-status/simulator público retorna 200")
    void testParkStatusSimulatorPublicAccess() throws Exception {
        mockMvc.perform(get(API_PARK_STATUS + "/simulator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").isBoolean());
    }

    @Test
    @Story("Park Status Público")
    @Description("Verifica que las atracciones son accesibles sin autenticación")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /park-status/attractions público retorna 200")
    void testParkStatusAttractionsPublicAccess() throws Exception {
        mockMvc.perform(get(API_PARK_STATUS + "/attractions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Dashboard")
    @Description("Verifica que el dashboard retorna datos correctos con atracciones")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /dashboard retorna snapshot con atracciones")
    void testGetDashboardReturnsSnapshotWithAttractions() throws Exception {
        mockMvc.perform(get(API_MONITORING + "/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAttractions").exists())
                .andExpect(jsonPath("$.attractionStatuses").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Eventos")
    @Description("Verifica que se puede registrar un evento de parque")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("POST /events registra evento correctamente")
    void testRecordEventReturnsOk() throws Exception {
        EventRequest request = new EventRequest();
        request.setEventType(ParkEventType.ATTRACTION_QUEUE_JOIN);
        request.setAttractionId(testAttraction.getId());
        request.setQueueSize(30);

        mockMvc.perform(post(API_MONITORING + "/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventType").value("ATTRACTION_QUEUE_JOIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Eventos")
    @Description("Verifica que obtener eventos recientes sin parámetro retorna 200")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("GET /events sin parámetro minutes retorna 200")
    void testGetRecentEventsDefaultMinutes() throws Exception {
        mockMvc.perform(get(API_MONITORING + "/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Alertas")
    @Description("Verifica que obtener alertas activas como admin retorna 200")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /alerts como admin retorna 200")
    void testGetActiveAlertsAsAdmin() throws Exception {
        mockMvc.perform(get(API_ALERTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Alertas")
    @Description("Verifica que resolver alerta acknowledge funciona E2E")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("POST /alerts/{id}/resolve acknowledge E2E")
    void testResolveAlertAcknowledgeE2E() throws Exception {
        ParkAlert alert = alertRepository.save(ParkAlert.builder()
                .alertType(AlertType.HIGH_QUEUE)
                .severity(AlertSeverity.WARNING)
                .message("alerts.messages.high_queue")
                .suggestion("alerts.suggestions.high_queue")
                .attractionId(testAttraction.getId())
                .timestamp(LocalDateTime.now())
                .isActive(true)
                .build());

        ResolveAlertRequest request = ResolveAlertRequest.builder()
                .resolutionOptionId("acknowledge")
                .build();

        mockMvc.perform(post(API_ALERTS + "/" + alert.getId() + "/resolve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Simulador")
    @Description("Verifica que iniciar simulador funciona E2E")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("POST /simulator/start E2E retorna 200")
    void testSimulatorStartE2E() throws Exception {
        mockMvc.perform(post(API_SIMULATOR + "/start").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(true));

        mockMvc.perform(post(API_SIMULATOR + "/stop").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Simulador")
    @Description("Verifica que broadcast forzado funciona E2E")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("POST /simulator/broadcast E2E retorna 200")
    void testSimulatorBroadcastE2E() throws Exception {
        mockMvc.perform(post(API_SIMULATOR + "/broadcast").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Seguridad")
    @Description("Verifica que usuarios normales no acceden al dashboard")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /dashboard como USER retorna 403")
    void testDashboardAsUserForbidden() throws Exception {
        mockMvc.perform(get(API_MONITORING + "/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Alertas")
    @Description("Verifica que resolver alerta inexistente retorna resultado fallido")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("POST /alerts/999/resolve retorna fallo")
    void testResolveNonExistentAlert() throws Exception {
        ResolveAlertRequest request = ResolveAlertRequest.builder()
                .resolutionOptionId("acknowledge")
                .build();

        mockMvc.perform(post(API_ALERTS + "/999/resolve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
}

