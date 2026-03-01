package com.magicworld.tfg_angular_springboot.monitoring;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.AttractionCategory;
import com.magicworld.tfg_angular_springboot.attraction.AttractionRepository;
import com.magicworld.tfg_angular_springboot.attraction.Intensity;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Epic("Sistema de Monitorización")
@Feature("API de Monitorización")
public class MonitoringControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private ParkEventRepository eventRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        attractionRepository.deleteAll();

        attractionRepository.save(Attraction.builder()
                .name("Test Ride")
                .intensity(Intensity.MEDIUM)
                .category(AttractionCategory.ROLLER_COASTER)
                .minimumHeight(100)
                .minimumAge(8)
                .minimumWeight(25)
                .description("Test")
                .photoUrl("http://test.com/img.jpg")
                .isActive(true)
                .mapPositionX(25.0)
                .mapPositionY(75.0)
                .build());
    }

    @Test
    @Story("Dashboard API")
    @Description("Verifica que admins pueden acceder al dashboard")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /dashboard como admin retorna 200")
    @WithMockUser(roles = "ADMIN")
    void testGetDashboardAsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/monitoring/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAttractions").value(1))
                .andExpect(jsonPath("$.attractionStatuses").isArray());
    }

    @Test
    @Story("Dashboard API")
    @Description("Verifica que usuarios normales no pueden acceder")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /dashboard como user retorna 403")
    @WithMockUser(roles = "USER")
    void testGetDashboardAsUserForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/monitoring/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Story("Eventos API")
    @Description("Verifica que el endpoint de eventos está protegido para admins")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /events retorna eventos recientes")
    @WithMockUser(roles = "ADMIN")
    void testGetRecentEvents() throws Exception {
        mockMvc.perform(get("/api/v1/monitoring/events")
                .param("minutes", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Story("Simulador API")
    @Description("Verifica control del simulador")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("GET /simulator/status retorna estado")
    @WithMockUser(roles = "ADMIN")
    void testSimulatorStatus() throws Exception {
        mockMvc.perform(get("/api/v1/monitoring/simulator/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").isBoolean());
    }
}
