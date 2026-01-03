package com.magicworld.tfg_angular_springboot.ticket_type;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.storage.ImageStorageService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    private static final String API_TICKET_TYPES = "/api/v1/ticket-types";
    private static final String API_TICKET_TYPES_ID = "/api/v1/ticket-types/";
    private static final String TYPE_NAME_STANDARD = "STANDARD";
    private static final String TYPE_NAME_PREMIUM = "PREMIUM";
    private static final String STANDARD_TICKET_DESC = "Standard ticket";
    private static final String PREMIUM_TICKET_DESC = "Premium ticket";
    private static final String CURRENCY_EUR = "EUR";
    private static final String CURRENCY_USD = "USD";
    private static final String PHOTO_URL_STANDARD = "http://example.com/standard.jpg";
    private static final String PHOTO_URL_PREMIUM = "http://example.com/premium.jpg";
    private static final String PHOTO_URL_INVALID = "http://example.com/invalid.jpg";
    private static final String ERROR_TICKET_TYPE_NOT_FOUND = "error.ticket_type.notfound";
    private static final BigDecimal COST_50 = new BigDecimal("50.00");
    private static final BigDecimal COST_75_50 = new BigDecimal("75.50");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketTypeService ticketTypeService;

    private TicketType sample() {
        return TicketType.builder()
                .cost(COST_50)
                .currency(CURRENCY_EUR)
                .typeName(TYPE_NAME_STANDARD)
                .description(STANDARD_TICKET_DESC)
                .maxPerDay(10)
                .photoUrl(PHOTO_URL_STANDARD)
                .build();
    }

    @Test
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que crear tipo de entrada retorna 201 Created con header Location")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear tipo de entrada retorna 201 Created")
    public void testCreateTicketTypeReturnsCreated() throws Exception {
        TicketType req = sample();
        TicketType saved = sample();
        saved.setId(1L);

        when(ticketTypeService.save(any(TicketType.class))).thenReturn(saved);

        var result = mockMvc.perform(post(API_TICKET_TYPES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", API_TICKET_TYPES_ID + "1"))
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que crear tipo de entrada retorna el cuerpo correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear tipo de entrada retorna cuerpo correcto")
    public void testCreateTicketTypeReturnsBody() throws Exception {
        TicketType req = sample();
        TicketType saved = sample();
        saved.setId(1L);

        when(ticketTypeService.save(any(TicketType.class))).thenReturn(saved);

        var result = mockMvc.perform(post(API_TICKET_TYPES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.typeName").value(TYPE_NAME_STANDARD))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(TYPE_NAME_STANDARD));
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que obtener todos los tipos retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener todos los tipos retorna 200 OK")
    public void testGetAllTicketTypesReturnsOk() throws Exception {
        TicketType one = sample();
        one.setId(2L);
        when(ticketTypeService.findAll()).thenReturn(List.of(one));

        var result = mockMvc.perform(get(API_TICKET_TYPES).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Obtener Tipo de Entrada por ID")
    @Description("Verifica que obtener tipo por ID existente retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener tipo por ID existente retorna 200 OK")
    public void testGetTicketTypeByIdFoundReturnsOk() throws Exception {
        TicketType tt = sample();
        tt.setId(3L);
        when(ticketTypeService.findById(3L)).thenReturn(tt);

        var result = mockMvc.perform(get(API_TICKET_TYPES_ID + "3").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Obtener Tipo de Entrada por ID")
    @Description("Verifica que obtener tipo por ID inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener tipo por ID inexistente retorna 404")
    public void testGetTicketTypeByIdNotFound() throws Exception {
        when(ticketTypeService.findById(999L)).thenThrow(new ResourceNotFoundException(ERROR_TICKET_TYPE_NOT_FOUND));

        var result = mockMvc.perform(get(API_TICKET_TYPES_ID + "999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que actualizar tipo retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar tipo retorna 200 OK")
    public void testUpdateTicketTypeReturnsOk() throws Exception {
        TicketType update = TicketType.builder()
                .cost(COST_75_50)
                .currency(CURRENCY_USD)
                .typeName(TYPE_NAME_PREMIUM)
                .description(PREMIUM_TICKET_DESC)
                .maxPerDay(20)
                .photoUrl(PHOTO_URL_PREMIUM)
                .build();

        TicketType returned = TicketType.builder()
                .cost(COST_75_50)
                .currency(CURRENCY_USD)
                .typeName(TYPE_NAME_PREMIUM)
                .description(PREMIUM_TICKET_DESC)
                .maxPerDay(20)
                .photoUrl(PHOTO_URL_PREMIUM)
                .build();
        returned.setId(4L);

        when(ticketTypeService.update(4L, update)).thenReturn(returned);

        var result = mockMvc.perform(put(API_TICKET_TYPES_ID + "4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Eliminar Tipo de Entrada")
    @Description("Verifica que eliminar tipo retorna 204 No Content")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar tipo retorna 204 No Content")
    public void testDeleteTicketType() throws Exception {
        doNothing().when(ticketTypeService).delete(5L);

        mockMvc.perform(delete(API_TICKET_TYPES_ID + "5").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(ticketTypeService).delete(5L);
    }

    @Test
    @Story("Validación de Entrada")
    @Description("Verifica que crear tipo con datos inválidos retorna 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear tipo con datos inválidos retorna 400")
    public void testCreateTicketTypeBadRequestWhenInvalid() throws Exception {
        TicketType invalid = TicketType.builder()
                .cost(null)
                .currency("")
                .typeName("")
                .description("")
                .maxPerDay(null)
                .photoUrl(PHOTO_URL_INVALID)
                .build();

        var result = mockMvc.perform(post(API_TICKET_TYPES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
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
        public ImageStorageService imageStorageService() {
            return Mockito.mock(ImageStorageService.class);
        }
    }
}
