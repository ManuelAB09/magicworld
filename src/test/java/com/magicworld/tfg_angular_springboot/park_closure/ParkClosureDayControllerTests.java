package com.magicworld.tfg_angular_springboot.park_closure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ParkClosureDayController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Gestión de Cierres del Parque")
@Feature("API REST de Cierres del Parque")
public class ParkClosureDayControllerTests {

    private static final String API_BASE = "/api/v1/park-closures";
    private static final String CLOSURE_REASON = "Maintenance Day";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParkClosureDayService service;

    private ParkClosureDay sample(LocalDate date) {
        return ParkClosureDay.builder()
                .closureDate(date)
                .reason(CLOSURE_REASON)
                .build();
    }

    @Test
    @Story("Listar Cierres")
    @Description("Verifica que obtener todos los cierres retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener todos los cierres retorna 200 OK")
    void testGetAllReturnsOk() throws Exception {
        ParkClosureDay pcd = sample(LocalDate.of(2026, 12, 25));
        pcd.setId(1L);
        when(service.findAll()).thenReturn(List.of(pcd));

        var result = mockMvc.perform(get(API_BASE).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Listar Cierres por Rango")
    @Description("Verifica que obtener cierres por rango retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener cierres por rango retorna 200 OK")
    void testGetAllByRangeReturnsOk() throws Exception {
        LocalDate from = LocalDate.of(2026, 12, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        when(service.findByRange(from, to)).thenReturn(List.of());

        var result = mockMvc.perform(get(API_BASE)
                        .param("from", "2026-12-01")
                        .param("to", "2026-12-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Verificar Cierre")
    @Description("Verifica que comprobar si un día está cerrado retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Comprobar día cerrado retorna 200 OK")
    void testIsClosedDayReturnsOk() throws Exception {
        when(service.isClosedDay(LocalDate.of(2026, 12, 25))).thenReturn(true);

        var result = mockMvc.perform(get(API_BASE + "/check")
                        .param("date", "2026-12-25")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Crear Cierre")
    @Description("Verifica que crear cierre retorna 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear cierre retorna 201 Created")
    void testCreateReturnsCreated() throws Exception {
        ParkClosureDay saved = sample(LocalDate.of(2026, 12, 25));
        saved.setId(1L);
        when(service.save(any(ParkClosureDay.class))).thenReturn(saved);

        ParkClosureDayRequest request = new ParkClosureDayRequest();
        request.setClosureDate(LocalDate.of(2026, 12, 25));
        request.setReason(CLOSURE_REASON);

        var result = mockMvc.perform(post(API_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    @Story("Eliminar Cierre")
    @Description("Verifica que eliminar cierre retorna 204 No Content")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar cierre retorna 204 No Content")
    void testDeleteReturnsNoContent() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete(API_BASE + "/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @Story("Validación de Entrada")
    @Description("Verifica que crear cierre con datos inválidos retorna 400")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear cierre con datos inválidos retorna 400")
    void testCreateBadRequest() throws Exception {
        ParkClosureDayRequest invalid = new ParkClosureDayRequest();

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
        public ParkClosureDayService parkClosureDayService() {
            return Mockito.mock(ParkClosureDayService.class);
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
