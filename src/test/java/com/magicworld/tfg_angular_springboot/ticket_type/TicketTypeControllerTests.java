package com.magicworld.tfg_angular_springboot.ticket_type;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
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
    public void testCreateTicketType() throws Exception {
        TicketType req = sample();
        TicketType saved = sample();
        saved.setId(1L);

        when(ticketTypeService.save(any(TicketType.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/ticket-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/ticket-types/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.typeName").value("STANDARD"));
    }

    @Test
    public void testGetAllTicketTypes() throws Exception {
        TicketType one = sample();
        one.setId(2L);
        when(ticketTypeService.findAll()).thenReturn(List.of(one));

        mockMvc.perform(get("/api/v1/ticket-types").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    public void testGetTicketTypeById_found() throws Exception {
        TicketType tt = sample();
        tt.setId(3L);
        when(ticketTypeService.findById(3L)).thenReturn(tt);

        mockMvc.perform(get("/api/v1/ticket-types/3").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.typeName").value("STANDARD"));
    }

    @Test
    public void testGetTicketTypeById_notFound() throws Exception {
        when(ticketTypeService.findById(999L)).thenThrow(new ResourceNotFoundException("error.ticket_type.notfound"));

        mockMvc.perform(get("/api/v1/ticket-types/999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateTicketType() throws Exception {
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
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.typeName").value("PREMIUM"));
    }

    @Test
    public void testDeleteTicketType() throws Exception {
        doNothing().when(ticketTypeService).delete(5L);

        mockMvc.perform(delete("/api/v1/ticket-types/5").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(ticketTypeService).delete(5L);
    }

    @Test
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
