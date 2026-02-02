package com.magicworld.tfg_angular_springboot.purchase;

import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLine;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLineRepository;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketTypeRepository;
import com.magicworld.tfg_angular_springboot.user.Role;
import com.magicworld.tfg_angular_springboot.user.User;
import com.magicworld.tfg_angular_springboot.user.UserRepository;
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
@Epic("Gestión de Compras")
@Feature("Servicio de Compras")
public class PurchaseServiceTests {

    private static final String TYPE_NAME_ADULT = "ADULT";
    private static final BigDecimal COST_50 = new BigDecimal("50.00");

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseLineRepository purchaseLineRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        purchaseLineRepository.deleteAll();
        purchaseRepository.deleteAll();
        ticketTypeRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        testUser = userRepository.save(User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password1@")
                .firstname("Test")
                .lastname("User")
                .userRole(Role.USER)
                .build());

        ticketTypeRepository.save(TicketType.builder()
                .cost(COST_50)
                .typeName(TYPE_NAME_ADULT)
                .description("Adult ticket")
                .maxPerDay(100)
                .photoUrl("https://example.com/adult.jpg")
                .build());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Crear compra guarda la compra correctamente")
    @Story("Crear Compra")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se puede crear una compra con líneas de compra")
    void createPurchaseSavesPurchase() {
        List<PurchaseLine> lines = List.of(
                PurchaseLine.builder()
                        .validDate(LocalDate.now().plusDays(1))
                        .quantity(2)
                        .totalCost(COST_50.multiply(BigDecimal.valueOf(2)))
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .build()
        );

        Purchase purchase = purchaseService.createPurchase(testUser, lines);

        assertNotNull(purchase.getId());
        assertEquals(LocalDate.now(), purchase.getPurchaseDate());
    }

    @Test
    @DisplayName("Crear compra asocia el comprador correctamente")
    @Story("Crear Compra")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que el comprador se asocia correctamente a la compra")
    void createPurchaseAssociatesBuyer() {
        List<PurchaseLine> lines = List.of(
                PurchaseLine.builder()
                        .validDate(LocalDate.now().plusDays(1))
                        .quantity(1)
                        .totalCost(COST_50)
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .build()
        );

        Purchase purchase = purchaseService.createPurchase(testUser, lines);

        assertNotNull(purchase.getBuyer());
        assertEquals(testUser.getId(), purchase.getBuyer().getId());
    }

    @Test
    @DisplayName("Buscar compra por ID existente retorna compra")
    @Story("Buscar Compra")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se puede buscar una compra por su ID")
    void findByIdExistingReturnsPurchase() {
        List<PurchaseLine> lines = List.of(
                PurchaseLine.builder()
                        .validDate(LocalDate.now().plusDays(1))
                        .quantity(1)
                        .totalCost(COST_50)
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .build()
        );
        Purchase created = purchaseService.createPurchase(testUser, lines);

        Purchase found = purchaseService.findById(created.getId());

        assertEquals(created.getId(), found.getId());
    }

    @Test
    @DisplayName("Buscar compra por ID inexistente lanza excepción")
    @Story("Buscar Compra")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que buscar compra inexistente lanza ResourceNotFoundException")
    void findByIdNonExistingThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> purchaseService.findById(999999L));
    }

    @Test
    @DisplayName("Buscar compras por comprador retorna lista")
    @Story("Buscar Compras por Comprador")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que se pueden buscar compras por ID de comprador")
    void findByBuyerIdReturnsPurchases() {
        List<PurchaseLine> lines = List.of(
                PurchaseLine.builder()
                        .validDate(LocalDate.now().plusDays(1))
                        .quantity(1)
                        .totalCost(COST_50)
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .build()
        );
        purchaseService.createPurchase(testUser, lines);

        List<Purchase> purchases = purchaseService.findByBuyerId(testUser.getId());

        assertFalse(purchases.isEmpty());
        assertEquals(1, purchases.size());
    }

    @Test
    @DisplayName("Buscar compras de usuario sin compras retorna lista vacía")
    @Story("Buscar Compras por Comprador")
    @Severity(SeverityLevel.MINOR)
    @Description("Verifica que buscar compras de usuario sin compras retorna lista vacía")
    void findByBuyerIdWithNoPurchasesReturnsEmptyList() {
        List<Purchase> purchases = purchaseService.findByBuyerId(testUser.getId());

        assertTrue(purchases.isEmpty());
    }

    @Test
    @DisplayName("Crear compra guarda las líneas de compra")
    @Story("Crear Compra")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que las líneas de compra se guardan correctamente")
    void createPurchaseSavesPurchaseLines() {
        List<PurchaseLine> lines = List.of(
                PurchaseLine.builder()
                        .validDate(LocalDate.now().plusDays(1))
                        .quantity(2)
                        .totalCost(COST_50.multiply(BigDecimal.valueOf(2)))
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .build()
        );

        Purchase purchase = purchaseService.createPurchase(testUser, lines);
        entityManager.flush();
        entityManager.clear();

        List<PurchaseLine> savedLines = purchaseLineRepository.findByPurchaseId(purchase.getId());

        assertFalse(savedLines.isEmpty());
        assertEquals(1, savedLines.size());
    }

    @Test
    @DisplayName("Crear compra con múltiples líneas guarda todas")
    @Story("Crear Compra")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que múltiples líneas de compra se guardan correctamente")
    void createPurchaseWithMultipleLinesAllSaved() {
        TicketType childTicket = ticketTypeRepository.save(TicketType.builder()
                .cost(new BigDecimal("25.00"))
                .typeName("CHILD")
                .description("Child ticket")
                .maxPerDay(100)
                .photoUrl("https://example.com/child.jpg")
                .build());
        entityManager.flush();

        List<PurchaseLine> lines = List.of(
                PurchaseLine.builder()
                        .validDate(LocalDate.now().plusDays(1))
                        .quantity(2)
                        .totalCost(COST_50.multiply(BigDecimal.valueOf(2)))
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .build(),
                PurchaseLine.builder()
                        .validDate(LocalDate.now().plusDays(1))
                        .quantity(1)
                        .totalCost(childTicket.getCost())
                        .ticketTypeName("CHILD")
                        .build()
        );

        Purchase purchase = purchaseService.createPurchase(testUser, lines);
        entityManager.flush();
        entityManager.clear();

        List<PurchaseLine> savedLines = purchaseLineRepository.findByPurchaseId(purchase.getId());

        assertEquals(2, savedLines.size());
    }
}
