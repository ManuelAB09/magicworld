package com.magicworld.tfg_angular_springboot.payment;

import com.magicworld.tfg_angular_springboot.discount.Discount;
import com.magicworld.tfg_angular_springboot.discount.DiscountRepository;
import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketType;
import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketTypeRepository;
import com.magicworld.tfg_angular_springboot.exceptions.InvalidOperationException;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Pasarela de Pago")
@Feature("Servicio de Pago")
public class PaymentServiceTests {

    private static final String TYPE_NAME_ADULT = "ADULT";
    private static final String TYPE_NAME_CHILD = "CHILD";
    private static final BigDecimal COST_50 = new BigDecimal("50.00");
    private static final BigDecimal COST_25 = new BigDecimal("25.00");

    @Autowired
    private PaymentService paymentService;

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

    private TicketType adultTicket;
    private TicketType childTicket;

    @BeforeEach
    void setUp() {
        purchaseLineRepository.deleteAll();
        purchaseRepository.deleteAll();
        discountTicketTypeRepository.deleteAll();
        discountRepository.deleteAll();
        ticketTypeRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        adultTicket = ticketTypeRepository.save(TicketType.builder()
                .cost(COST_50)
                .typeName(TYPE_NAME_ADULT)
                .description("Adult ticket")
                .maxPerDay(100)
                .photoUrl("https://example.com/adult.jpg")
                .build());

        childTicket = ticketTypeRepository.save(TicketType.builder()
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
    @DisplayName("Obtener disponibilidad de entradas para una fecha")
    @Story("Consulta de disponibilidad")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se obtiene correctamente la disponibilidad de entradas para una fecha dada")
    void getAvailabilityShouldReturnAllTicketTypes() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<TicketAvailabilityDTO> availability = paymentService.getAvailability(tomorrow);

        assertNotNull(availability);
        assertEquals(2, availability.size());

        TicketAvailabilityDTO adultAvail = availability.stream()
                .filter(a -> a.getTypeName().equals(TYPE_NAME_ADULT))
                .findFirst()
                .orElse(null);

        assertNotNull(adultAvail);
        assertEquals(100, adultAvail.getAvailable());
        assertEquals(COST_50, adultAvail.getCost());
    }

    @Test
    @DisplayName("Calcular precio sin descuentos")
    @Story("Cálculo de precios")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que el cálculo del precio sin descuentos es correcto")
    void calculatePriceWithoutDiscountsShouldReturnCorrectTotal() {
        List<PaymentRequest.PaymentLineItem> items = List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(2)
                        .build(),
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_CHILD)
                        .quantity(1)
                        .build()
        );

        PriceCalculationResponse response = paymentService.calculatePrice(items, null);

        assertNotNull(response);
        assertEquals(new BigDecimal("125.00"), response.getSubtotal());
        assertEquals(BigDecimal.ZERO.setScale(2), response.getDiscountAmount());
        assertEquals(new BigDecimal("125.00"), response.getTotal());
    }

    @Test
    @DisplayName("Calcular precio con descuento válido")
    @Story("Cálculo de precios")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que el cálculo del precio con descuento válido aplica correctamente")
    void calculatePriceWithValidDiscountShouldApplyDiscount() {
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

        List<PaymentRequest.PaymentLineItem> items = List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(2)
                        .build()
        );

        PriceCalculationResponse response = paymentService.calculatePrice(items, List.of("SAVE10"));

        assertNotNull(response);
        assertEquals(new BigDecimal("100.00"), response.getSubtotal());
        assertEquals(new BigDecimal("10.00"), response.getDiscountAmount());
        assertEquals(new BigDecimal("90.00"), response.getTotal());
        assertTrue(response.getValidDiscountCodes().contains("SAVE10"));
    }

    @Test
    @DisplayName("Calcular precio con descuento inválido")
    @Story("Cálculo de precios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que los códigos de descuento inválidos no se aplican")
    void calculatePriceWithInvalidDiscountShouldNotApplyDiscount() {
        List<PaymentRequest.PaymentLineItem> items = List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(2)
                        .build()
        );

        PriceCalculationResponse response = paymentService.calculatePrice(items, List.of("INVALID_CODE"));

        assertNotNull(response);
        assertEquals(new BigDecimal("100.00"), response.getSubtotal());
        assertEquals(BigDecimal.ZERO.setScale(2), response.getDiscountAmount());
        assertEquals(new BigDecimal("100.00"), response.getTotal());
        assertTrue(response.getInvalidDiscountCodes().contains("INVALID_CODE"));
    }

    @Test
    @DisplayName("Calcular precio con múltiples códigos de descuento")
    @Story("Cálculo de precios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que se pueden aplicar múltiples códigos de descuento separados")
    void calculatePriceWithMultipleDiscountsShouldApplyBest() {
        TicketType adult = ticketTypeRepository.findByTypeName(TYPE_NAME_ADULT).orElseThrow();

        Discount discount1 = discountRepository.save(Discount.builder()
                .discountCode("SAVE10")
                .discountPercentage(10)
                .expiryDate(LocalDate.now().plusDays(30))
                .build());

        Discount discount2 = discountRepository.save(Discount.builder()
                .discountCode("SAVE20")
                .discountPercentage(20)
                .expiryDate(LocalDate.now().plusDays(30))
                .build());

        discountTicketTypeRepository.save(DiscountTicketType.builder()
                .discount(discount1)
                .ticketType(adult)
                .build());

        discountTicketTypeRepository.save(DiscountTicketType.builder()
                .discount(discount2)
                .ticketType(adult)
                .build());

        entityManager.flush();
        entityManager.clear();

        List<PaymentRequest.PaymentLineItem> items = List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(2)
                        .build()
        );

        PriceCalculationResponse response = paymentService.calculatePrice(items, List.of("SAVE10", "SAVE20"));

        assertNotNull(response);
        assertEquals(new BigDecimal("100.00"), response.getSubtotal());
        assertEquals(new BigDecimal("20.00"), response.getDiscountAmount());
        assertEquals(new BigDecimal("80.00"), response.getTotal());
    }

    @Test
    @DisplayName("No permitir compra en fecha pasada")
    @Story("Validación de compra")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que no se puede procesar un pago para una fecha pasada")
    void processPaymentWithPastDateShouldThrowException() {
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

        assertThrows(InvalidOperationException.class, () ->
                paymentService.processPayment(request, "es")
        );
    }

    @Test
    @DisplayName("No permitir compra cuando no hay disponibilidad")
    @Story("Validación de compra")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que no se puede comprar más de la disponibilidad")
    void processPaymentWithInsufficientAvailabilityShouldThrowException() {
        PaymentRequest request = PaymentRequest.builder()
                .visitDate(LocalDate.now().plusDays(1))
                .items(List.of(PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(101)
                        .build()))
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .stripePaymentMethodId("pm_test")
                .build();

        assertThrows(InvalidOperationException.class, () ->
                paymentService.processPayment(request, "es")
        );
    }

    @Test
    @DisplayName("Calcular precio con lista de descuentos vacía")
    @Story("Cálculo de precios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que el cálculo funciona con lista vacía de descuentos")
    void calculatePriceWithEmptyDiscountListShouldWork() {
        List<PaymentRequest.PaymentLineItem> items = List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(1)
                        .build()
        );

        PriceCalculationResponse response = paymentService.calculatePrice(items, List.of());

        assertNotNull(response);
        assertEquals(COST_50, response.getTotal());
    }

    @Test
    @DisplayName("Calcular precio con código de descuento vacío se ignora")
    @Story("Cálculo de precios")
    @Severity(SeverityLevel.MINOR)
    @Description("Verifica que códigos vacíos se ignoran en el cálculo")
    void calculatePriceWithEmptyCodeShouldIgnore() {
        List<PaymentRequest.PaymentLineItem> items = List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(1)
                        .build()
        );

        PriceCalculationResponse response = paymentService.calculatePrice(items, List.of("   ", ""));

        assertNotNull(response);
        assertTrue(response.getInvalidDiscountCodes().isEmpty());
    }

    @Test
    @DisplayName("Calcular precio con descuento retorna porcentajes")
    @Story("Cálculo de precios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que la respuesta incluye porcentajes de descuento")
    void calculatePriceReturnsDiscountPercentages() {
        TicketType adult = ticketTypeRepository.findByTypeName(TYPE_NAME_ADULT).orElseThrow();

        Discount discount = discountRepository.save(Discount.builder()
                .discountCode("PERCENT15")
                .discountPercentage(15)
                .expiryDate(LocalDate.now().plusDays(30))
                .build());

        discountTicketTypeRepository.save(DiscountTicketType.builder()
                .discount(discount)
                .ticketType(adult)
                .build());

        entityManager.flush();
        entityManager.clear();

        List<PaymentRequest.PaymentLineItem> items = List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(1)
                        .build()
        );

        PriceCalculationResponse response = paymentService.calculatePrice(items, List.of("PERCENT15"));

        assertNotNull(response.getDiscountPercentages());
        assertEquals(15, response.getDiscountPercentages().get("PERCENT15"));
    }

    @Test
    @DisplayName("Calcular precio con descuento retorna tipos aplicables")
    @Story("Cálculo de precios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que la respuesta indica a qué tipos aplica el descuento")
    void calculatePriceReturnsDiscountAppliesTo() {
        TicketType adult = ticketTypeRepository.findByTypeName(TYPE_NAME_ADULT).orElseThrow();

        Discount discount = discountRepository.save(Discount.builder()
                .discountCode("ADULTONLY")
                .discountPercentage(10)
                .expiryDate(LocalDate.now().plusDays(30))
                .build());

        discountTicketTypeRepository.save(DiscountTicketType.builder()
                .discount(discount)
                .ticketType(adult)
                .build());

        entityManager.flush();
        entityManager.clear();

        List<PaymentRequest.PaymentLineItem> items = List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(1)
                        .build()
        );

        PriceCalculationResponse response = paymentService.calculatePrice(items, List.of("ADULTONLY"));

        assertNotNull(response.getDiscountAppliesTo());
        assertTrue(response.getDiscountAppliesTo().get("ADULTONLY").contains(TYPE_NAME_ADULT));
    }

    @Test
    @DisplayName("Obtener disponibilidad para fecha específica")
    @Story("Consulta de disponibilidad")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que se puede obtener disponibilidad para cualquier fecha futura")
    void getAvailabilityForSpecificDateShouldWork() {
        LocalDate futureDate = LocalDate.now().plusDays(30);

        List<TicketAvailabilityDTO> availability = paymentService.getAvailability(futureDate);

        assertNotNull(availability);
        assertFalse(availability.isEmpty());
    }

    @Test
    @DisplayName("Disponibilidad incluye información de precio")
    @Story("Consulta de disponibilidad")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que la disponibilidad incluye el costo de cada tipo")
    void getAvailabilityIncludesCostInfo() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<TicketAvailabilityDTO> availability = paymentService.getAvailability(tomorrow);

        TicketAvailabilityDTO adultAvail = availability.stream()
                .filter(a -> a.getTypeName().equals(TYPE_NAME_ADULT))
                .findFirst()
                .orElseThrow();

        assertEquals(COST_50, adultAvail.getCost());
        assertNotNull(adultAvail.getDescription());
    }
}

