package com.magicworld.tfg_angular_springboot.discount;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.discount.DiscountController.DiscountRequest;
import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketTypeService;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
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
@Epic("Gestión de Descuentos")
@Feature("Controlador de Descuentos")
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
    @Story("Crear Descuento")
    @Description("Verifica que crear descuento retorna estado 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear descuento retorna 201 Created")
    public void testCreateDiscount_returnsCreated() throws Exception {
        Discount toSave = sampleDiscount();
        Discount saved = sampleDiscount();
        saved.setId(1L);

        when(discountService.save(any(Discount.class), anyList())).thenReturn(saved);

        DiscountRequest request = requestWith(toSave, List.of("ADULT", "CHILD"));

        mockMvc.perform(post("/api/v1/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/discounts/1"));
    }

    @Test
    @Story("Crear Descuento")
    @Description("Verifica que crear descuento retorna el cuerpo de respuesta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear descuento retorna body con datos")
    public void testCreateDiscount_returnsBody() throws Exception {
        Discount toSave = sampleDiscount();
        Discount saved = sampleDiscount();
        saved.setId(1L);

        when(discountService.save(any(Discount.class), anyList())).thenReturn(saved);

        DiscountRequest request = requestWith(toSave, List.of("ADULT", "CHILD"));

        mockMvc.perform(post("/api/v1/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.discountCode").value("WELCOME10"));
    }

    @Test
    @Story("Listar Descuentos")
    @Description("Verifica que listar descuentos retorna estado 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar descuentos retorna 200 OK")
    public void testFindAllDiscounts_returnsOk() throws Exception {
        Discount one = sampleDiscount();
        one.setId(10L);
        when(discountService.findAll()).thenReturn(List.of(one));

        mockMvc.perform(get("/api/v1/discounts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Story("Buscar Descuento por ID")
    @Description("Verifica que buscar descuento existente retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Buscar descuento existente retorna 200 OK")
    public void testFindById_found_returnsOk() throws Exception {
        Discount one = sampleDiscount();
        one.setId(2L);
        when(discountService.findById(2L)).thenReturn(one);

        mockMvc.perform(get("/api/v1/discounts/2").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @Story("Buscar Descuento por ID")
    @Description("Verifica que buscar descuento inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar descuento inexistente retorna 404")
    public void testFindById_notFound() throws Exception {
        when(discountService.findById(999L)).thenThrow(new ResourceNotFoundException("error.discount.not_found"));

        mockMvc.perform(get("/api/v1/discounts/999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Story("Actualizar Descuento")
    @Description("Verifica que actualizar descuento retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar descuento retorna 200 OK")
    public void testUpdateDiscount_returnsOk() throws Exception {
        Discount in = sampleDiscount();
        in.setId(3L);
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
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    @Story("Eliminar Descuento")
    @Description("Verifica que eliminar descuento retorna 204 No Content")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar descuento retorna 204 No Content")
    public void testDeleteDiscount() throws Exception {
        doNothing().when(discountService).deleteById(4L);

        mockMvc.perform(delete("/api/v1/discounts/4").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(discountService).deleteById(4L);
    }

    @Test
    @Story("Obtener Tipos de Entrada por Descuento")
    @Description("Verifica que obtener tipos de entrada por descuento retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener tipos de entrada por descuento retorna 200 OK")
    public void testGetTicketTypesByDiscount_found_returnsOk() throws Exception {
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
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Story("Obtener Tipos de Entrada por Descuento")
    @Description("Verifica que obtener tipos de entrada de descuento inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener tipos de entrada de descuento inexistente retorna 404")
    public void testGetTicketTypesByDiscount_notFound() throws Exception {
        when(discountService.findById(888L)).thenThrow(new ResourceNotFoundException("error.discount.not_found"));

        mockMvc.perform(get("/api/v1/discounts/888/ticket-types").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Story("Validación de Descuentos")
    @Description("Verifica que crear descuento con tipos vacíos retorna 400")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear descuento con tipos vacíos retorna 400")
    public void testCreateDiscount_badRequest_whenEmptyTypes() throws Exception {
        Discount invalid = sampleDiscount();
        DiscountRequest request = requestWith(invalid, List.of()); // NotEmpty -> 400

        mockMvc.perform(post("/api/v1/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Story("Validación de Descuentos")
    @Description("Verifica que crear descuento con datos inválidos retorna 400")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear descuento con datos inválidos retorna 400")
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
