package com.magicworld.tfg_angular_springboot.attraction;

import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
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
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ParkZoneController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Gestión de Zonas del Parque")
@Feature("API de Zonas")
public class ParkZoneControllerTests {

    private static final String API_ZONES = "/api/zones";
    private static final String TEST_ZONE_DESC = "Adventure themed zone";
    private static final String TEST_ATTRACTION_NAME = "Thunder Mountain";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParkZoneRepository zoneRepository;

    @Autowired
    private AttractionRepository attractionRepository;

    @Test
    @Story("Listar Zonas")
    @Description("Verifica que obtener todas las zonas retorna 200 OK con lista vacía")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getAllZones vacías retorna 200 OK")
    void testGetAllZonesEmptyReturnsOk() throws Exception {
        when(zoneRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(API_ZONES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Story("Listar Zonas")
    @Description("Verifica que obtener zonas con datos retorna lista")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getAllZones con datos retorna lista")
    void testGetAllZonesWithDataReturnsList() throws Exception {
        ParkZone zone = new ParkZone();
        zone.setId(1L);
        zone.setZoneName(ParkZoneName.ADVENTURE_ZONE);
        zone.setDescription(TEST_ZONE_DESC);

        Attraction attr = Attraction.builder()
                .name(TEST_ATTRACTION_NAME).isActive(true).build();
        attr.setId(1L);

        when(zoneRepository.findAll()).thenReturn(List.of(zone));
        when(attractionRepository.findByZoneId(1L)).thenReturn(List.of(attr));

        mockMvc.perform(get(API_ZONES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].zoneName").value("ADVENTURE_ZONE"));
    }

    @Test
    @Story("Obtener Zona por ID")
    @Description("Verifica que obtener zona existente retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getZone existente retorna 200 OK")
    void testGetZoneByIdFoundReturnsOk() throws Exception {
        ParkZone zone = new ParkZone();
        zone.setId(1L);
        zone.setZoneName(ParkZoneName.ADVENTURE_ZONE);
        zone.setDescription(TEST_ZONE_DESC);

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(attractionRepository.findByZoneId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(API_ZONES + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zoneName").value("ADVENTURE_ZONE"));
    }

    @Test
    @Story("Obtener Zona por ID")
    @Description("Verifica que obtener zona inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getZone inexistente retorna 404")
    void testGetZoneByIdNotFoundReturns404() throws Exception {
        when(zoneRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get(API_ZONES + "/999"))
                .andExpect(status().isNotFound());
    }

    @TestConfiguration
    static class Config {
        @Bean
        public ParkZoneRepository parkZoneRepository() { return Mockito.mock(ParkZoneRepository.class); }
        @Bean
        public AttractionRepository attractionRepository() { return Mockito.mock(AttractionRepository.class); }
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

