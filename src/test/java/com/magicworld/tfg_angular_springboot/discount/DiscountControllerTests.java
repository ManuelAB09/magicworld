package com.magicworld.tfg_angular_springboot.discount;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.discount.DiscountController.DiscountRequest;
import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketTypeService;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

@WebMvcTest(controllers = DiscountController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class DiscountControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private DiscountTicketTypeService discountTicketTypeService;


    private Discount sampleDiscount() {
        return Discount.builder()
                .discountCode("WELCOME10")
                .discountPercentage(10)
                .expiryDate(LocalDate.now().plusDays(30))
                .build();
    }

    private DiscountRequest requestWith(Discount discount, List<String> names) {
        DiscountRequest req = new DiscountRequest();
        req.setDiscount(discount);
        req.setApplicableTicketTypesNames(names);
        return req;
    }

    @Test
    public void testCreateDiscount() throws Exception {
        Discount toSave = sampleDiscount();
        Discount saved = sampleDiscount();
        saved.setId(1L);

        when(discountService.save(any(Discount.class), anyList())).thenReturn(saved);

        DiscountRequest request = requestWith(toSave, List.of("ADULT", "CHILD"));

        mockMvc.perform(post("/api/v1/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/discounts/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.discountCode").value("WELCOME10"));
    }

    @Test
    public void testFindAllDiscounts() throws Exception {
        Discount one = sampleDiscount();
        one.setId(10L);
        when(discountService.findAll()).thenReturn(List.of(one));

        mockMvc.perform(get("/api/v1/discounts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    public void testFindById_found() throws Exception {
        Discount one = sampleDiscount();
        one.setId(2L);
        when(discountService.findById(2L)).thenReturn(one);

        mockMvc.perform(get("/api/v1/discounts/2").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.discountCode").value("WELCOME10"));
    }

    @Test
    public void testFindById_notFound() throws Exception {
        when(discountService.findById(999L)).thenThrow(new ResourceNotFoundException("error.discount.not_found"));

        mockMvc.perform(get("/api/v1/discounts/999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateDiscount() throws Exception {
        Discount in = sampleDiscount();
        in.setId(3L); // el controller no usa el path id, por lo que el id debe venir en el body
        Discount returned = Discount.builder()
                .discountCode("UPDATED20")
                .discountPercentage(20)
                .expiryDate(LocalDate.now().plusDays(60))
                .build();
        returned.setId(3L);

        when(discountService.update(any(Discount.class), anyList())).thenReturn(returned);

        DiscountRequest request = requestWith(in, List.of("VIP"));

        mockMvc.perform(put("/api/v1/discounts/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.discountCode").value("UPDATED20"))
                .andExpect(jsonPath("$.discountPercentage").value(20));
    }

    @Test
    public void testDeleteDiscount() throws Exception {
        doNothing().when(discountService).deleteById(4L);

        mockMvc.perform(delete("/api/v1/discounts/4").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(discountService).deleteById(4L);
    }

    @Test
    public void testGetTicketTypesByDiscount_found() throws Exception {
        Discount d = sampleDiscount();
        d.setId(5L);
        when(discountService.findById(5L)).thenReturn(d);

        TicketType tt = TicketType.builder()
                .cost(new BigDecimal("50.00"))
                .currency("EUR")
                .typeName("ADULT")
                .description("Adult ticket")
                .maxPerDay(10)
                .build();
        tt.setId(100L);

        when(discountTicketTypeService.findTicketsTypesByDiscountId(5L)).thenReturn(List.of(tt));

        mockMvc.perform(get("/api/v1/discounts/5/ticket-types").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].typeName").value("ADULT"));
    }

    @Test
    public void testGetTicketTypesByDiscount_notFound() throws Exception {
        when(discountService.findById(888L)).thenThrow(new ResourceNotFoundException("error.discount.not_found"));

        mockMvc.perform(get("/api/v1/discounts/888/ticket-types").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateDiscount_badRequest_whenEmptyTypes() throws Exception {
        Discount invalid = sampleDiscount();
        DiscountRequest request = requestWith(invalid, List.of()); // NotEmpty -> 400

        mockMvc.perform(post("/api/v1/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateDiscount_badRequest_whenInvalidDiscount() throws Exception {
        Discount invalid = Discount.builder()
                .discountCode("") // NotBlank -> 400
                .discountPercentage(0) // Min 1 -> 400
                .expiryDate(null) // NotNull -> 400
                .build();
        DiscountRequest request = requestWith(invalid, List.of("ADULT"));

        mockMvc.perform(post("/api/v1/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class DiscountControllerTestConfig {
        @Bean
        public DiscountService discountService() {
            return Mockito.mock(DiscountService.class);
        }

        @Bean
        public DiscountTicketTypeService discountTicketTypeService() {
            return Mockito.mock(DiscountTicketTypeService.class);
        }

        @Bean
        public JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return Mockito.mock(JwtAuthenticationFilter.class);
        }
    }
}
