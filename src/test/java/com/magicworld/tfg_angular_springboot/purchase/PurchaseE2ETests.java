package com.magicworld.tfg_angular_springboot.purchase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLine;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLineRepository;
import com.magicworld.tfg_angular_springboot.user.Role;
import com.magicworld.tfg_angular_springboot.user.User;
import com.magicworld.tfg_angular_springboot.user.UserRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@Epic("Gestión de Compras")
@Feature("Tests E2E de Compras")
public class PurchaseE2ETests {

    private static final String API_MY_PURCHASES = "/api/v1/purchases/my-purchases";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseLineRepository purchaseLineRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        purchaseLineRepository.deleteAll();
        purchaseRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .username("purchaseuser")
                .firstname("John")
                .lastname("Doe")
                .email("purchase@example.com")
                .password("Password123!")
                .userRole(Role.USER)
                .build());

        otherUser = userRepository.save(User.builder()
                .username("otheruser")
                .firstname("Jane")
                .lastname("Smith")
                .email("other@example.com")
                .password("Password123!")
                .userRole(Role.USER)
                .build());
    }

    @Test
    @WithMockUser(username = "purchaseuser", roles = "USER")
    @Story("Listar Mis Compras")
    @Description("Verifica que listar mis compras E2E retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar mis compras E2E retorna 200 OK")
    void testGetMyPurchasesE2EReturnsOk() throws Exception {
        mockMvc.perform(get(API_MY_PURCHASES)
                .with(csrf())
                .with(user(testUser)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "purchaseuser", roles = "USER")
    @Story("Listar Mis Compras")
    @Description("Verifica que sin compras retorna lista vacía")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Sin compras retorna lista vacía E2E")
    void testGetMyPurchasesE2EEmptyList() throws Exception {
        mockMvc.perform(get(API_MY_PURCHASES)
                .with(csrf())
                .with(user(testUser)))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "purchaseuser", roles = "USER")
    @Story("Listar Mis Compras")
    @Description("Verifica que retorna solo compras propias")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Solo retorna compras propias E2E")
    void testGetMyPurchasesE2EOnlyOwnPurchases() throws Exception {
        Purchase myPurchase = purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now())
                .buyer(testUser)
                .build());

        Purchase otherPurchase = purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now())
                .buyer(otherUser)
                .build());

        mockMvc.perform(get(API_MY_PURCHASES)
                .with(csrf())
                .with(user(testUser)))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(myPurchase.getId()));
    }

    @Test
    @WithMockUser(username = "purchaseuser", roles = "USER")
    @Story("Listar Mis Compras")
    @Description("Verifica que retorna datos completos de compra")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Retorna datos completos de compra E2E")
    void testGetMyPurchasesE2EReturnsCompleteData() throws Exception {
        Purchase purchase = purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now())
                .buyer(testUser)
                .build());

        PurchaseLine line = purchaseLineRepository.save(PurchaseLine.builder()
                .purchase(purchase)
                .ticketTypeName("Adult")
                .quantity(2)
                .validDate(LocalDate.now().plusDays(7))
                .totalCost(new BigDecimal("59.80"))
                .build());

        mockMvc.perform(get(API_MY_PURCHASES)
                .with(csrf())
                .with(user(testUser)))
                .andExpect(jsonPath("$[0].id").value(purchase.getId()))
                .andExpect(jsonPath("$[0].purchaseDate").exists())
                .andExpect(jsonPath("$[0].lines").isArray())
                .andExpect(jsonPath("$[0].lines[0].ticketTypeName").value("Adult"))
                .andExpect(jsonPath("$[0].lines[0].quantity").value(2));
    }

    @Test
    @WithMockUser(username = "purchaseuser", roles = "USER")
    @Story("Listar Mis Compras")
    @Description("Verifica que múltiples compras se retornan correctamente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Múltiples compras se retornan E2E")
    void testGetMyPurchasesE2EMultiplePurchases() throws Exception {
        purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now())
                .buyer(testUser)
                .build());

        purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now().minusDays(5))
                .buyer(testUser)
                .build());

        purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now().minusDays(10))
                .buyer(testUser)
                .build());

        mockMvc.perform(get(API_MY_PURCHASES)
                .with(csrf())
                .with(user(testUser)))
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @Story("Listar Mis Compras")
    @Description("Verifica que sin autenticación retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Sin auth retorna 401 E2E")
    void testGetMyPurchasesE2EWithoutAuthReturns401() throws Exception {
        mockMvc.perform(get(API_MY_PURCHASES)
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "purchaseuser", roles = "USER")
    @Story("Listar Mis Compras")
    @Description("Verifica que retorna múltiples líneas por compra")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Retorna múltiples líneas por compra E2E")
    void testGetMyPurchasesE2EMultipleLinesPerPurchase() throws Exception {
        Purchase purchase = purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now())
                .buyer(testUser)
                .build());

        purchaseLineRepository.save(PurchaseLine.builder()
                .purchase(purchase)
                .ticketTypeName("Adult")
                .quantity(2)
                .validDate(LocalDate.now().plusDays(7))
                .totalCost(new BigDecimal("59.80"))
                .build());

        purchaseLineRepository.save(PurchaseLine.builder()
                .purchase(purchase)
                .ticketTypeName("Child")
                .quantity(1)
                .validDate(LocalDate.now().plusDays(7))
                .totalCost(new BigDecimal("19.90"))
                .build());

        mockMvc.perform(get(API_MY_PURCHASES)
                .with(csrf())
                .with(user(testUser)))
                .andExpect(jsonPath("$[0].lines.length()").value(2));
    }

    @Test
    @WithMockUser(username = "otheruser", roles = "USER")
    @Story("Listar Mis Compras")
    @Description("Verifica aislamiento de datos entre usuarios")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Aislamiento de datos entre usuarios E2E")
    void testGetMyPurchasesE2EDataIsolation() throws Exception {
        purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now())
                .buyer(testUser)
                .build());

        mockMvc.perform(get(API_MY_PURCHASES)
                .with(csrf())
                .with(user(otherUser)))
                .andExpect(jsonPath("$.length()").value(0));
    }
}
