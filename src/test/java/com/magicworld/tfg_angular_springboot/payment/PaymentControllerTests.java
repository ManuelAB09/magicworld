package com.magicworld.tfg_angular_springboot.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketTypeRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Epic("Pasarela de Pago")
@Feature("API de Pago")
public class PaymentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @BeforeEach
    void setUp() {
        ticketTypeRepository.deleteAll();

        ticketTypeRepository.save(TicketType.builder()
                .cost(new BigDecimal("50.00"))
                .currency("EUR")
                .typeName("ADULT")
                .description("Adult ticket")
                .maxPerDay(100)
                .photoUrl("https://example.com/adult.jpg")
                .build());

        ticketTypeRepository.save(TicketType.builder()
                .cost(new BigDecimal("25.00"))
                .currency("EUR")
                .typeName("CHILD")
                .description("Child ticket")
                .maxPerDay(100)
                .photoUrl("https://example.com/child.jpg")
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/payment/stripe-key - Obtener clave pública de Stripe")
    @Story("Configuración de Stripe")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se obtiene la clave pública de Stripe")
    void getStripePublicKey_shouldReturnKey() throws Exception {
        mockMvc.perform(get("/api/v1/payment/stripe-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicKey").exists());
    }

    @Test
    @DisplayName("GET /api/v1/payment/availability - Obtener disponibilidad")
    @Story("Consulta de disponibilidad")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se obtiene la disponibilidad de entradas para una fecha")
    void getAvailability_shouldReturnTicketTypes() throws Exception {
        String tomorrow = LocalDate.now().plusDays(1).toString();

        mockMvc.perform(get("/api/v1/payment/availability")
                        .param("date", tomorrow))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/v1/payment/calculate - Calcular precio")
    @Story("Cálculo de precios")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se calcula correctamente el precio de las entradas")
    void calculatePrice_shouldReturnCorrectTotal() throws Exception {
        PaymentController.PriceCalculationRequest request = new PaymentController.PriceCalculationRequest();
        request.setItems(List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName("ADULT")
                        .quantity(2)
                        .build()
        ));
        request.setDiscountCodes(List.of());

        mockMvc.perform(post("/api/v1/payment/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(100.00))
                .andExpect(jsonPath("$.total").value(100.00));
    }

    @Test
    @DisplayName("POST /api/v1/payment/process - Validar datos requeridos")
    @Story("Procesamiento de pago")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que se validan los datos requeridos para el pago")
    void processPayment_withMissingData_shouldReturnBadRequest() throws Exception {
        PaymentRequest request = PaymentRequest.builder()
                .visitDate(LocalDate.now().plusDays(1))
                .items(List.of())
                .email("")
                .firstName("")
                .lastName("")
                .build();

        mockMvc.perform(post("/api/v1/payment/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

