package com.magicworld.tfg_angular_springboot.payment;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Pasarela de Pago")
@Feature("Servicio de Pago - Integración")
public class PaymentServiceIntegrationTests {

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
                .maxPerDay(50)
                .photoUrl("https://example.com/child.jpg")
                .build());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("GetAvailability retorna todos los tipos de entrada")
    @Story("Disponibilidad de Entradas")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se obtienen todos los tipos de entrada con disponibilidad")
    void getAvailabilityReturnsAllTicketTypes() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<TicketAvailabilityDTO> availability = paymentService.getAvailability(tomorrow);

        assertEquals(2, availability.size());
    }

    @Test
    @DisplayName("GetAvailability retorna disponibilidad máxima sin ventas")
    @Story("Disponibilidad de Entradas")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que la disponibilidad sin ventas es el máximo por día")
    void getAvailabilityReturnsMaxWithNoSales() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<TicketAvailabilityDTO> availability = paymentService.getAvailability(tomorrow);

        TicketAvailabilityDTO adultAvail = availability.stream()
                .filter(a -> a.getTypeName().equals(TYPE_NAME_ADULT))
                .findFirst()
                .orElseThrow();

        assertEquals(100, adultAvail.getAvailable());
        assertEquals(COST_50, adultAvail.getCost());
    }

    @Test
    @DisplayName("CalculatePrice sin descuentos calcula correctamente")
    @Story("Cálculo de Precios")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica el cálculo del precio sin descuentos")
    void calculatePriceWithoutDiscountsCalculatesCorrectly() {
        List<PaymentRequest.PaymentLineItem> items = List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(2)
                        .build()
        );

        PriceCalculationResponse response = paymentService.calculatePrice(items, null);

        assertEquals(new BigDecimal("100.00"), response.getSubtotal());
        assertEquals(new BigDecimal("100.00"), response.getTotal());
    }

    @Test
    @DisplayName("CalculatePrice con descuento válido aplica correctamente")
    @Story("Cálculo de Precios")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que el descuento se aplica correctamente")
    void calculatePriceWithValidDiscountAppliesCorrectly() {
        TicketType adult = ticketTypeRepository.findByTypeName(TYPE_NAME_ADULT).orElseThrow();

        Discount discount = discountRepository.save(Discount.builder()
                .discountCode("PROMO20")
                .discountPercentage(20)
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

        PriceCalculationResponse response = paymentService.calculatePrice(items, List.of("PROMO20"));

        assertEquals(new BigDecimal("100.00"), response.getSubtotal());
        assertEquals(new BigDecimal("20.00"), response.getDiscountAmount());
    }


    @Test
    @DisplayName("CalculatePrice con múltiples tipos suma correctamente")
    @Story("Cálculo de Precios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica el cálculo con múltiples tipos de entrada")
    void calculatePriceWithMultipleTypesCalculatesCorrectly() {
        List<PaymentRequest.PaymentLineItem> items = List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(2)
                        .build(),
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_CHILD)
                        .quantity(3)
                        .build()
        );

        PriceCalculationResponse response = paymentService.calculatePrice(items, null);

        assertEquals(new BigDecimal("175.00"), response.getSubtotal());
        assertEquals(new BigDecimal("175.00"), response.getTotal());
    }

    @Test
    @DisplayName("CalculatePrice con código inexistente lo marca como inválido")
    @Story("Cálculo de Precios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que códigos inexistentes se marcan como inválidos")
    void calculatePriceWithNonExistentCodeMarksAsInvalid() {
        List<PaymentRequest.PaymentLineItem> items = List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(1)
                        .build()
        );

        PriceCalculationResponse response = paymentService.calculatePrice(items, List.of("NONEXISTENT"));

        assertTrue(response.getInvalidDiscountCodes().contains("NONEXISTENT"));
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), response.getDiscountAmount());
    }

    @Test
    @DisplayName("CalculatePrice aplica mejor descuento cuando hay múltiples")
    @Story("Cálculo de Precios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que se aplica el mejor descuento disponible")
    void calculatePriceAppliesBestDiscount() {
        TicketType adult = ticketTypeRepository.findByTypeName(TYPE_NAME_ADULT).orElseThrow();

        Discount discount10 = discountRepository.save(Discount.builder()
                .discountCode("SAVE10")
                .discountPercentage(10)
                .expiryDate(LocalDate.now().plusDays(30))
                .build());

        Discount discount30 = discountRepository.save(Discount.builder()
                .discountCode("SAVE30")
                .discountPercentage(30)
                .expiryDate(LocalDate.now().plusDays(30))
                .build());

        discountTicketTypeRepository.save(DiscountTicketType.builder()
                .discount(discount10)
                .ticketType(adult)
                .build());

        discountTicketTypeRepository.save(DiscountTicketType.builder()
                .discount(discount30)
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

        PriceCalculationResponse response = paymentService.calculatePrice(items, List.of("SAVE10", "SAVE30"));

        assertEquals(new BigDecimal("15.00"), response.getDiscountAmount());
    }

    @Test
    @DisplayName("CalculatePrice con descuento no aplicable a tipo lo marca")
    @Story("Cálculo de Precios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que descuentos no aplicables se marcan correctamente")
    void calculatePriceWithDiscountNotApplicableToTypeMarksIt() {
        TicketType child = ticketTypeRepository.findByTypeName(TYPE_NAME_CHILD).orElseThrow();

        Discount discount = discountRepository.save(Discount.builder()
                .discountCode("CHILDONLY")
                .discountPercentage(15)
                .expiryDate(LocalDate.now().plusDays(30))
                .build());

        discountTicketTypeRepository.save(DiscountTicketType.builder()
                .discount(discount)
                .ticketType(child)
                .build());

        entityManager.flush();
        entityManager.clear();

        List<PaymentRequest.PaymentLineItem> items = List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .quantity(1)
                        .build()
        );

        PriceCalculationResponse response = paymentService.calculatePrice(items, List.of("CHILDONLY"));

        assertTrue(response.getValidButNotApplicableCodes().contains("CHILDONLY"));
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), response.getDiscountAmount());
    }
}
