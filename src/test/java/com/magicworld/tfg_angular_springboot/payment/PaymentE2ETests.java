package com.magicworld.tfg_angular_springboot.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.discount.Discount;
import com.magicworld.tfg_angular_springboot.discount.DiscountRepository;
import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketType;
import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketTypeRepository;
import com.magicworld.tfg_angular_springboot.purchase.PurchaseRepository;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLineRepository;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketTypeRepository;
import io.qameta.allure.*;
import jakarta.persistence.EntityManager;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Epic("Pasarela de Pago")
@Feature("API REST de Pago E2E")
public class PaymentE2ETests {

    private static final String API_PAYMENT = "/api/v1/payment";
    private static final String TYPE_NAME_ADULT = "ADULT";
    private static final String TYPE_NAME_CHILD = "CHILD";
    private static final BigDecimal COST_50 = new BigDecimal("50.00");
    private static final BigDecimal COST_25 = new BigDecimal("25.00");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private DiscountTicketTypeRepository discountTicketTypeRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseLineRepository purchaseLineRepository;

    @Autowired
    private EntityManager entityManager;


    @BeforeEach
    void setUp() {
        purchaseLineRepository.deleteAll();
        purchaseRepository.deleteAll();
        discountTicketTypeRepository.deleteAll();
        discountRepository.deleteAll();
        ticketTypeRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        ticketTypeRepository.save(TicketType.builder()
                .cost(COST_50)
                .typeName(TYPE_NAME_ADULT)
                .description("Adult ticket")
                .maxPerDay(100)
                .photoUrl("https://example.com/adult.jpg")
                .build());

        ticketTypeRepository.save(TicketType.builder()
                .cost(COST_25)
                .typeName(TYPE_NAME_CHILD)
                .description("Child ticket")
                .maxPerDay(100)
                .photoUrl("https://example.com/child.jpg")
                .build());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("GET /api/v1/payment/stripe-key retorna 200 OK")
    @Story("Obtener clave Stripe")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se obtiene la clave pública de Stripe correctamente")
    void getStripePublicKeyReturnsOk() throws Exception {
        var result = mockMvc.perform(get(API_PAYMENT + "/stripe-key"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("GET /api/v1/payment/stripe-key retorna campo publicKey")
    @Story("Obtener clave Stripe")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que la respuesta contiene el campo publicKey")
    void getStripePublicKeyReturnsPublicKeyField() throws Exception {
        var result = mockMvc.perform(get(API_PAYMENT + "/stripe-key"))
                .andExpect(jsonPath("$.publicKey").exists())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("publicKey"));
    }

    @Test
    @DisplayName("GET /api/v1/payment/availability retorna 200 OK")
    @Story("Consultar disponibilidad")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se obtiene la disponibilidad de entradas correctamente")
    void getAvailabilityReturnsOk() throws Exception {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        var result = mockMvc.perform(get(API_PAYMENT + "/availability")
                        .param("date", tomorrow))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("GET /api/v1/payment/availability retorna tipos de entrada")
    @Story("Consultar disponibilidad")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que la disponibilidad incluye los tipos de entrada")
    void getAvailabilityReturnsTicketTypes() throws Exception {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        var result = mockMvc.perform(get(API_PAYMENT + "/availability")
                        .param("date", tomorrow))
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(TYPE_NAME_ADULT));
    }

    @Test
    @DisplayName("POST /api/v1/payment/calculate retorna 200 OK")
    @Story("Calcular precio")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se calcula el precio correctamente")
    void calculatePriceReturnsOk() throws Exception {
        PaymentController.PriceCalculationRequest request = new PaymentController.PriceCalculationRequest();
        request.setItems(List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(2)
                        .build()
        ));
        request.setDiscountCodes(List.of());

        var result = mockMvc.perform(post(API_PAYMENT + "/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("POST /api/v1/payment/calculate retorna subtotal correcto")
    @Story("Calcular precio")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que el subtotal calculado es correcto")
    void calculatePriceReturnsCorrectSubtotal() throws Exception {
        PaymentController.PriceCalculationRequest request = new PaymentController.PriceCalculationRequest();
        request.setItems(List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(2)
                        .build()
        ));
        request.setDiscountCodes(List.of());

        var result = mockMvc.perform(post(API_PAYMENT + "/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.subtotal").value(100.00))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("100.00"));
    }

    @Test
    @DisplayName("POST /api/v1/payment/calculate con descuento válido aplica descuento")
    @Story("Calcular precio con descuento")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que el descuento se aplica correctamente")
    void calculatePriceWithDiscountAppliesDiscount() throws Exception {
        TicketType adult = ticketTypeRepository.findByTypeName(TYPE_NAME_ADULT).orElseThrow();

        Discount discount = discountRepository.save(Discount.builder()
                .discountCode("SAVE10")
                .discountPercentage(10)
                .expiryDate(LocalDate.now().plusDays(30))
                .build());

        discountTicketTypeRepository.save(DiscountTicketType.builder()
                .discount(discount)
                .ticketType(adult)
                .build());

        entityManager.flush();
        entityManager.clear();

        PaymentController.PriceCalculationRequest request = new PaymentController.PriceCalculationRequest();
        request.setItems(List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(2)
                        .build()
        ));
        request.setDiscountCodes(List.of("SAVE10"));

        var result = mockMvc.perform(post(API_PAYMENT + "/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.discountAmount").value(10.00))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("90.00"));
    }

    @Test
    @DisplayName("POST /api/v1/payment/calculate con descuento inválido no aplica descuento")
    @Story("Calcular precio con descuento inválido")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que los códigos de descuento inválidos no se aplican")
    void calculatePriceWithInvalidDiscountDoesNotApply() throws Exception {
        PaymentController.PriceCalculationRequest request = new PaymentController.PriceCalculationRequest();
        request.setItems(List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(2)
                        .build()
        ));
        request.setDiscountCodes(List.of("INVALID_CODE"));

        var result = mockMvc.perform(post(API_PAYMENT + "/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.invalidDiscountCodes[0]").value("INVALID_CODE"))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("INVALID_CODE"));
    }

    @Test
    @DisplayName("POST /api/v1/payment/process sin items retorna 400")
    @Story("Procesar pago")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que se validan los datos requeridos para el pago")
    void processPaymentWithEmptyItemsReturnsBadRequest() throws Exception {
        PaymentRequest request = PaymentRequest.builder()
                .visitDate(LocalDate.now().plusDays(1))
                .items(List.of())
                .email("")
                .firstName("")
                .lastName("")
                .build();

        var result = mockMvc.perform(post(API_PAYMENT + "/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("POST /api/v1/payment/process con fecha pasada retorna error")
    @Story("Procesar pago")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que no se puede procesar un pago para fecha pasada")
    void processPaymentWithPastDateReturnsError() throws Exception {
        PaymentRequest request = PaymentRequest.builder()
                .visitDate(LocalDate.now().minusDays(1))
                .items(List.of(PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(1)
                        .build()))
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .stripePaymentMethodId("pm_test")
                .build();

        var result = mockMvc.perform(post(API_PAYMENT + "/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("GET /api/v1/payment/availability retorna disponibilidad máxima inicial")
    @Story("Consultar disponibilidad")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que la disponibilidad inicial es igual al máximo por día")
    void getAvailabilityReturnsMaxAvailability() throws Exception {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        var result = mockMvc.perform(get(API_PAYMENT + "/availability")
                        .param("date", tomorrow))
                .andExpect(jsonPath("$[0].available").value(100))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("\"available\":100"));
    }

    @Test
    @DisplayName("POST /api/v1/payment/calculate con múltiples tipos de entrada")
    @Story("Calcular precio")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica el cálculo de precio con múltiples tipos de entrada")
    void calculatePriceWithMultipleTicketTypes() throws Exception {
        PaymentController.PriceCalculationRequest request = new PaymentController.PriceCalculationRequest();
        request.setItems(List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(2)
                        .build(),
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_CHILD)
                        .quantity(1)
                        .build()
        ));
        request.setDiscountCodes(List.of());

        var result = mockMvc.perform(post(API_PAYMENT + "/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.total").value(125.00))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("125.00"));
    }
}
