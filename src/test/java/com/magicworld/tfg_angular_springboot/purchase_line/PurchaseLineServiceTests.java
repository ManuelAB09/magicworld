package com.magicworld.tfg_angular_springboot.purchase_line;

import com.magicworld.tfg_angular_springboot.purchase.Purchase;
import com.magicworld.tfg_angular_springboot.purchase.PurchaseRepository;
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
@Epic("Gestión de Líneas de Compra")
@Feature("Servicio de Líneas de Compra")
public class PurchaseLineServiceTests {

    private static final String TYPE_NAME_ADULT = "ADULT";
    private static final BigDecimal COST_50 = new BigDecimal("50.00");

    @Autowired
    private PurchaseLineService purchaseLineService;

    @Autowired
    private PurchaseLineRepository purchaseLineRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private Purchase testPurchase;

    @BeforeEach
    void setUp() {
        purchaseLineRepository.deleteAll();
        purchaseRepository.deleteAll();
        ticketTypeRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        User testUser = userRepository.save(User.builder()
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

        testPurchase = purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now())
                .buyer(testUser)
                .build());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Guardar línea de compra retorna línea con ID")
    @Story("Guardar Línea de Compra")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se puede guardar una línea de compra y obtiene ID")
    void savePurchaseLineReturnsWithId() {
        PurchaseLine line = PurchaseLine.builder()
                .validDate(LocalDate.now().plusDays(1))
                .quantity(2)
                .totalCost(COST_50.multiply(BigDecimal.valueOf(2)))
                .ticketTypeName(TYPE_NAME_ADULT)
                .purchase(purchaseRepository.findById(testPurchase.getId()).orElseThrow())
                .build();

        PurchaseLine saved = purchaseLineService.save(line);

        assertNotNull(saved.getId());
    }

    @Test
    @DisplayName("Guardar línea de compra mantiene datos correctos")
    @Story("Guardar Línea de Compra")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que los datos de la línea se guardan correctamente")
    void savePurchaseLineKeepsCorrectData() {
        PurchaseLine line = PurchaseLine.builder()
                .validDate(LocalDate.now().plusDays(1))
                .quantity(2)
                .totalCost(COST_50.multiply(BigDecimal.valueOf(2)))
                .ticketTypeName(TYPE_NAME_ADULT)
                .purchase(purchaseRepository.findById(testPurchase.getId()).orElseThrow())
                .build();

        PurchaseLine saved = purchaseLineService.save(line);

        assertEquals(2, saved.getQuantity());
        assertEquals(TYPE_NAME_ADULT, saved.getTicketTypeName());
    }

    @Test
    @DisplayName("Buscar líneas por ID de compra retorna líneas")
    @Story("Buscar Líneas de Compra")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se pueden buscar líneas por ID de compra")
    void findByPurchaseIdReturnsLines() {
        Purchase purchase = purchaseRepository.findById(testPurchase.getId()).orElseThrow();
        purchaseLineRepository.save(PurchaseLine.builder()
                .validDate(LocalDate.now().plusDays(1))
                .quantity(1)
                .totalCost(COST_50)
                .ticketTypeName(TYPE_NAME_ADULT)
                .purchase(purchase)
                .build());
        entityManager.flush();

        List<PurchaseLine> lines = purchaseLineService.findByPurchaseId(testPurchase.getId());

        assertFalse(lines.isEmpty());
        assertEquals(1, lines.size());
    }

    @Test
    @DisplayName("Buscar líneas de compra inexistente retorna lista vacía")
    @Story("Buscar Líneas de Compra")
    @Severity(SeverityLevel.MINOR)
    @Description("Verifica que buscar líneas de compra inexistente retorna lista vacía")
    void findByPurchaseIdNonExistingReturnsEmptyList() {
        List<PurchaseLine> lines = purchaseLineService.findByPurchaseId(999999L);

        assertTrue(lines.isEmpty());
    }

    @Test
    @DisplayName("Obtener cantidad vendida sin ventas retorna cero")
    @Story("Consultar Cantidad Vendida")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que la cantidad vendida es cero sin ventas previas")
    void getSoldQuantityWithNoSalesReturnsZero() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        int sold = purchaseLineService.getSoldQuantity(TYPE_NAME_ADULT, tomorrow);

        assertEquals(0, sold);
    }

    @Test
    @DisplayName("Obtener cantidad vendida con ventas retorna suma")
    @Story("Consultar Cantidad Vendida")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que la cantidad vendida suma todas las líneas")
    void getSoldQuantityWithSalesReturnsSum() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Purchase purchase = purchaseRepository.findById(testPurchase.getId()).orElseThrow();
        purchaseLineRepository.save(PurchaseLine.builder()
                .validDate(tomorrow)
                .quantity(5)
                .totalCost(COST_50.multiply(BigDecimal.valueOf(5)))
                .ticketTypeName(TYPE_NAME_ADULT)
                .purchase(purchase)
                .build());
        entityManager.flush();

        int sold = purchaseLineService.getSoldQuantity(TYPE_NAME_ADULT, tomorrow);

        assertEquals(5, sold);
    }

    @Test
    @DisplayName("Obtener disponibilidad sin ventas retorna máximo")
    @Story("Consultar Disponibilidad")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que la disponibilidad sin ventas es el máximo por día")
    void getAvailableQuantityWithNoSalesReturnsMax() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        int available = purchaseLineService.getAvailableQuantity(TYPE_NAME_ADULT, tomorrow);

        assertEquals(100, available);
    }

    @Test
    @DisplayName("Obtener disponibilidad con ventas descuenta correctamente")
    @Story("Consultar Disponibilidad")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que la disponibilidad descuenta las ventas realizadas")
    void getAvailableQuantityWithSalesDeductsCorrectly() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Purchase purchase = purchaseRepository.findById(testPurchase.getId()).orElseThrow();
        purchaseLineRepository.save(PurchaseLine.builder()
                .validDate(tomorrow)
                .quantity(30)
                .totalCost(COST_50.multiply(BigDecimal.valueOf(30)))
                .ticketTypeName(TYPE_NAME_ADULT)
                .purchase(purchase)
                .build());
        entityManager.flush();

        int available = purchaseLineService.getAvailableQuantity(TYPE_NAME_ADULT, tomorrow);

        assertEquals(70, available);
    }

    @Test
    @DisplayName("Guardar múltiples líneas guarda todas")
    @Story("Guardar Líneas de Compra")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que saveAll guarda múltiples líneas correctamente")
    void saveAllSavesMultipleLines() {
        Purchase purchase = purchaseRepository.findById(testPurchase.getId()).orElseThrow();
        List<PurchaseLine> lines = List.of(
                PurchaseLine.builder()
                        .validDate(LocalDate.now().plusDays(1))
                        .quantity(2)
                        .totalCost(COST_50.multiply(BigDecimal.valueOf(2)))
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .purchase(purchase)
                        .build(),
                PurchaseLine.builder()
                        .validDate(LocalDate.now().plusDays(1))
                        .quantity(3)
                        .totalCost(COST_50.multiply(BigDecimal.valueOf(3)))
                        .ticketTypeName(TYPE_NAME_ADULT)
                        .purchase(purchase)
                        .build()
        );

        List<PurchaseLine> saved = purchaseLineService.saveAll(lines);

        assertEquals(2, saved.size());
    }

    @Test
    @DisplayName("Disponibilidad no es negativa aunque se sobrepase")
    @Story("Consultar Disponibilidad")
    @Severity(SeverityLevel.MINOR)
    @Description("Verifica que la disponibilidad nunca retorna valores negativos")
    void getAvailableQuantityNeverNegative() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Purchase purchase = purchaseRepository.findById(testPurchase.getId()).orElseThrow();
        purchaseLineRepository.save(PurchaseLine.builder()
                .validDate(tomorrow)
                .quantity(150)
                .totalCost(COST_50.multiply(BigDecimal.valueOf(150)))
                .ticketTypeName(TYPE_NAME_ADULT)
                .purchase(purchase)
                .build());
        entityManager.flush();

        int available = purchaseLineService.getAvailableQuantity(TYPE_NAME_ADULT, tomorrow);

        assertEquals(0, available);
    }
}
