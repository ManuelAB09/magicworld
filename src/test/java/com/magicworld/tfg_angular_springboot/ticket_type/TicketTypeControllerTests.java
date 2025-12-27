package com.magicworld.tfg_angular_springboot.ticket_type;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TicketTypeController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@Epic("Gestión de Tipos de Entrada")
@Feature("API REST de Tipos de Entrada")
public class TicketTypeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketTypeService ticketTypeService;

    private TicketType sample() {
        return TicketType.builder()
                .cost(new BigDecimal("50.00"))
                .currency("EUR")
                .typeName("STANDARD")
                .description("Standard ticket")
                .maxPerDay(10)
                .photoUrl("http://example.com/standard.jpg")
                .build();
    }

    @Test
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que crear tipo de entrada retorna 201 Created con header Location")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear tipo de entrada retorna 201 Created")
    public void testCreateTicketType_returnsCreated() throws Exception {
        TicketType req = sample();
        TicketType saved = sample();
        saved.setId(1L);

        when(ticketTypeService.save(any(TicketType.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/ticket-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/ticket-types/1"));
    }

    @Test
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que crear tipo de entrada retorna el cuerpo correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear tipo de entrada retorna cuerpo correcto")
    public void testCreateTicketType_returnsBody() throws Exception {
        TicketType req = sample();
        TicketType saved = sample();
        saved.setId(1L);

        when(ticketTypeService.save(any(TicketType.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/ticket-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.typeName").value("STANDARD"));
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que obtener todos los tipos retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener todos los tipos retorna 200 OK")
    public void testGetAllTicketTypes_returnsOk() throws Exception {
        TicketType one = sample();
        one.setId(2L);
        when(ticketTypeService.findAll()).thenReturn(List.of(one));

        mockMvc.perform(get("/api/v1/ticket-types").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Story("Obtener Tipo de Entrada por ID")
    @Description("Verifica que obtener tipo por ID existente retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener tipo por ID existente retorna 200 OK")
    public void testGetTicketTypeById_found_returnsOk() throws Exception {
        TicketType tt = sample();
        tt.setId(3L);
        when(ticketTypeService.findById(3L)).thenReturn(tt);

        mockMvc.perform(get("/api/v1/ticket-types/3").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    @Story("Obtener Tipo de Entrada por ID")
    @Description("Verifica que obtener tipo por ID inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener tipo por ID inexistente retorna 404")
    public void testGetTicketTypeById_notFound() throws Exception {
        when(ticketTypeService.findById(999L)).thenThrow(new ResourceNotFoundException("error.ticket_type.notfound"));

        mockMvc.perform(get("/api/v1/ticket-types/999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que actualizar tipo retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar tipo retorna 200 OK")
    public void testUpdateTicketType_returnsOk() throws Exception {
        TicketType update = TicketType.builder()
                .cost(new BigDecimal("75.50"))
                .currency("USD")
                .typeName("PREMIUM")
                .description("Premium ticket")
                .maxPerDay(20)
                .photoUrl("http://example.com/premium.jpg")
                .build();

        TicketType returned = TicketType.builder()
                .cost(new BigDecimal("75.50"))
                .currency("USD")
                .typeName("PREMIUM")
                .description("Premium ticket")
                .maxPerDay(20)
                .photoUrl("http://example.com/premium.jpg")
                .build();
        returned.setId(4L);

        when(ticketTypeService.update(4L, update)).thenReturn(returned);

        mockMvc.perform(put("/api/v1/ticket-types/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    @Story("Eliminar Tipo de Entrada")
    @Description("Verifica que eliminar tipo retorna 204 No Content")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar tipo retorna 204 No Content")
    public void testDeleteTicketType() throws Exception {
        doNothing().when(ticketTypeService).delete(5L);

        mockMvc.perform(delete("/api/v1/ticket-types/5").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(ticketTypeService).delete(5L);
    }

    @Test
    @Story("Validación de Entrada")
    @Description("Verifica que crear tipo con datos inválidos retorna 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear tipo con datos inválidos retorna 400")
    public void testCreateTicketType_badRequest_whenInvalid() throws Exception {
        TicketType invalid = TicketType.builder()
                .cost(null)
                .currency("")
                .typeName("")
                .description("")
                .maxPerDay(null)
                .photoUrl("http://example.com/invalid.jpg")
                .build();

        mockMvc.perform(post("/api/v1/ticket-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class TicketTypeControllerTestConfig {
        @Bean
        public TicketTypeService ticketTypeService() {
            return Mockito.mock(TicketTypeService.class);
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
        public com.magicworld.tfg_angular_springboot.storage.ImageStorageService imageStorageService() {
            return Mockito.mock(com.magicworld.tfg_angular_springboot.storage.ImageStorageService.class);
        }
    }
}
