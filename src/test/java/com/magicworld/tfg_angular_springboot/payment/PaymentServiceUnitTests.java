package com.magicworld.tfg_angular_springboot.payment;

import com.magicworld.tfg_angular_springboot.discount.DiscountService;
import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketTypeService;
import com.magicworld.tfg_angular_springboot.email.EmailService;
import com.magicworld.tfg_angular_springboot.exceptions.InvalidOperationException;
import com.magicworld.tfg_angular_springboot.purchase.Purchase;
import com.magicworld.tfg_angular_springboot.purchase.PurchaseService;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLineService;
import com.magicworld.tfg_angular_springboot.qr.QrCodeService;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketTypeService;
import com.magicworld.tfg_angular_springboot.user.Role;
import com.magicworld.tfg_angular_springboot.user.User;
import com.magicworld.tfg_angular_springboot.user.UserRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("Pasarela de Pago")
@Feature("Servicio de Pago - Unit Tests")
public class PaymentServiceUnitTests {

    @Mock
    private TicketTypeService ticketTypeService;
    @Mock
    private PurchaseLineService purchaseLineService;
    @Mock
    private PurchaseService purchaseService;
    @Mock
    private DiscountService discountService;
    @Mock
    private DiscountTicketTypeService discountTicketTypeService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private QrCodeService qrCodeService;
    @Mock
    private EmailService emailService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentService = new PaymentService(
                ticketTypeService,
                purchaseLineService,
                purchaseService,
                discountService,
                discountTicketTypeService,
                userRepository,
                qrCodeService,
                emailService,
                messagingTemplate
        );
    }

    @Test
    @DisplayName("ValidatePurchase lanza excepción con fecha pasada")
    @Story("Validación de Compra")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que no se permite fecha de visita pasada")
    void validatePurchaseThrowsExceptionForPastDate() {
        PaymentRequest request = PaymentRequest.builder()
                .visitDate(LocalDate.now().minusDays(1))
                .items(List.of(PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName("ADULT")
                        .quantity(1)
                        .build()))
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        assertThrows(InvalidOperationException.class, () -> paymentService.processPayment(request, "es"));
    }

    @Test
    @DisplayName("ValidatePurchase lanza excepción si no hay disponibilidad")
    @Story("Validación de Compra")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que no se permite compra sin disponibilidad suficiente")
    void validatePurchaseThrowsExceptionForInsufficientAvailability() {
        when(purchaseLineService.getAvailableQuantity(anyString(), any(LocalDate.class))).thenReturn(5);

        PaymentRequest request = PaymentRequest.builder()
                .visitDate(LocalDate.now().plusDays(1))
                .items(List.of(PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName("ADULT")
                        .quantity(10)
                        .build()))
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        assertThrows(InvalidOperationException.class, () -> paymentService.processPayment(request, "es"));
    }

    @Test
    @DisplayName("GetAvailability retorna lista de disponibilidad")
    @Story("Consulta de Disponibilidad")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se obtiene la disponibilidad correctamente")
    void getAvailabilityReturnsAvailabilityList() {
        TicketType ticketType = TicketType.builder()
                .typeName("ADULT")
                .cost(new BigDecimal("50.00"))
                .description("Adult ticket")
                .maxPerDay(100)
                .photoUrl("http://example.com/photo.jpg")
                .build();
        ticketType.setId(1L);

        when(ticketTypeService.findAll()).thenReturn(List.of(ticketType));
        when(purchaseLineService.getAvailableQuantity(anyString(), any(LocalDate.class))).thenReturn(100);

        List<TicketAvailabilityDTO> result = paymentService.getAvailability(LocalDate.now().plusDays(1));

        assertFalse(result.isEmpty());
        assertEquals("ADULT", result.getFirst().getTypeName());
    }

    @Test
    @DisplayName("GetAvailability incluye información de precio")
    @Story("Consulta de Disponibilidad")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que la disponibilidad incluye el costo")
    void getAvailabilityIncludesCost() {
        TicketType ticketType = TicketType.builder()
                .typeName("ADULT")
                .cost(new BigDecimal("50.00"))
                .description("Adult ticket")
                .maxPerDay(100)
                .photoUrl("http://example.com/photo.jpg")
                .build();
        ticketType.setId(1L);

        when(ticketTypeService.findAll()).thenReturn(List.of(ticketType));
        when(purchaseLineService.getAvailableQuantity(anyString(), any(LocalDate.class))).thenReturn(80);

        List<TicketAvailabilityDTO> result = paymentService.getAvailability(LocalDate.now().plusDays(1));

        assertEquals(new BigDecimal("50.00"), result.getFirst().getCost());
        assertEquals(80, result.getFirst().getAvailable());
    }

    @Test
    @DisplayName("CalculatePrice retorna subtotal correcto")
    @Story("Cálculo de Precios")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que el subtotal se calcula correctamente")
    void calculatePriceReturnsCorrectSubtotal() {
        TicketType ticketType = TicketType.builder()
                .typeName("ADULT")
                .cost(new BigDecimal("50.00"))
                .build();

        when(ticketTypeService.findByTypeName("ADULT")).thenReturn(ticketType);

        List<PaymentRequest.PaymentLineItem> items = List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName("ADULT")
                        .quantity(2)
                        .build()
        );

        PriceCalculationResponse response = paymentService.calculatePrice(items, null);

        assertEquals(new BigDecimal("100.00"), response.getSubtotal());
    }

    @Test
    @DisplayName("CalculatePrice con código inválido lo marca")
    @Story("Cálculo de Precios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que códigos de descuento inválidos se marcan")
    void calculatePriceMarksInvalidCodes() {
        TicketType ticketType = TicketType.builder()
                .typeName("ADULT")
                .cost(new BigDecimal("50.00"))
                .build();

        when(ticketTypeService.findByTypeName("ADULT")).thenReturn(ticketType);
        when(discountService.findByCode("INVALID")).thenReturn(null);

        List<PaymentRequest.PaymentLineItem> items = List.of(
                PaymentRequest.PaymentLineItem.builder()
                        .ticketTypeName("ADULT")
                        .quantity(1)
                        .build()
        );

        PriceCalculationResponse response = paymentService.calculatePrice(items, List.of("INVALID"));

        assertTrue(response.getInvalidDiscountCodes().contains("INVALID"));
    }

    @Test
    @DisplayName("NotifyAvailabilityChange envía mensaje WebSocket")
    @Story("Notificación de Disponibilidad")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que se notifica cambio de disponibilidad por WebSocket")
    void notifyAvailabilityChangeSendsWebSocketMessage() {
        when(ticketTypeService.findAll()).thenReturn(List.of());

        paymentService.notifyAvailabilityChange(LocalDate.now().plusDays(1));

        verify(messagingTemplate).convertAndSend(anyString(), any(List.class));
    }

    @Test
    @DisplayName("NotifyAvailabilityChange usa tópico correcto")
    @Story("Notificación de Disponibilidad")
    @Severity(SeverityLevel.MINOR)
    @Description("Verifica que se usa el tópico correcto para WebSocket")
    void notifyAvailabilityChangeUsesCorrectTopic() {
        LocalDate date = LocalDate.now().plusDays(1);
        when(ticketTypeService.findAll()).thenReturn(List.of());

        paymentService.notifyAvailabilityChange(date);

        verify(messagingTemplate).convertAndSend(eq("/topic/availability/" + date), any(List.class));
    }
}
