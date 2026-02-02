package com.magicworld.tfg_angular_springboot.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.purchase.Purchase;
import com.magicworld.tfg_angular_springboot.purchase.PurchaseRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@Epic("Gestión de Valoraciones")
@Feature("Tests E2E de Valoraciones")
public class ReviewE2ETests {

    private static final String API_REVIEWS = "/api/v1/reviews";
    private static final String API_REVIEWS_AVAILABLE = "/api/v1/reviews/available-purchases";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Purchase testPurchase;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        purchaseRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .username("e2euser")
                .firstname("John")
                .lastname("Doe")
                .email("e2e@example.com")
                .password("Password123!")
                .userRole(Role.USER)
                .build());

        testPurchase = purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now())
                .buyer(testUser)
                .build());
    }

    @Test
    @Story("Listar Valoraciones")
    @Description("Verifica que listar valoraciones retorna 200 OK sin autenticación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar valoraciones públicas retorna 200 OK")
    void testGetAllReviewsPublicReturnsOk() throws Exception {
        mockMvc.perform(get(API_REVIEWS)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @Story("Listar Valoraciones")
    @Description("Verifica paginación correcta de valoraciones")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Paginación de valoraciones funciona correctamente")
    void testGetAllReviewsPaginationWorks() throws Exception {
        Review review = Review.builder()
                .stars(4.5)
                .publicationDate(LocalDate.now())
                .visitDate(LocalDate.now())
                .description("Valoración de prueba")
                .purchase(testPurchase)
                .build();
        reviewRepository.save(review);

        mockMvc.perform(get(API_REVIEWS)
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(5));
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Crear Valoración")
    @Description("Verifica que crear valoración retorna 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear valoración E2E retorna 201 Created")
    void testCreateReviewE2EReturnsCreated() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(LocalDate.now())
                .stars(4.5)
                .description("Excelente parque de atracciones")
                .build();

        mockMvc.perform(post(API_REVIEWS)
                .with(csrf())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Crear Valoración")
    @Description("Verifica que valoración creada persiste en base de datos")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Valoración creada persiste en BD")
    void testCreateReviewE2EPersistsInDatabase() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(LocalDate.now())
                .stars(5.0)
                .description("Increíble experiencia")
                .build();

        mockMvc.perform(post(API_REVIEWS)
                .with(csrf())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertEquals(1, reviewRepository.count());
        assertTrue(reviewRepository.existsByPurchaseId(testPurchase.getId()));
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Crear Valoración")
    @Description("Verifica que no se puede crear valoración duplicada")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("No se puede crear valoración duplicada E2E")
    void testCreateReviewE2EDuplicateReturns400() throws Exception {
        Review existing = Review.builder()
                .stars(4.0)
                .publicationDate(LocalDate.now())
                .visitDate(LocalDate.now())
                .description("Primera valoración")
                .purchase(testPurchase)
                .build();
        reviewRepository.save(existing);

        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(LocalDate.now())
                .stars(5.0)
                .description("Segunda valoración")
                .build();

        mockMvc.perform(post(API_REVIEWS)
                .with(csrf())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Compras Disponibles")
    @Description("Verifica obtención de compras disponibles para valorar")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener compras disponibles E2E")
    void testGetAvailablePurchasesE2E() throws Exception {
        mockMvc.perform(get(API_REVIEWS_AVAILABLE)
                .with(csrf())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value(testPurchase.getId()));
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Compras Disponibles")
    @Description("Verifica que compra valorada no aparece en disponibles")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Compra valorada no aparece en disponibles E2E")
    void testGetAvailablePurchasesExcludesReviewedE2E() throws Exception {
        Review review = Review.builder()
                .stars(4.5)
                .publicationDate(LocalDate.now())
                .visitDate(LocalDate.now())
                .description("Valoración existente")
                .purchase(testPurchase)
                .build();
        reviewRepository.save(review);

        mockMvc.perform(get(API_REVIEWS_AVAILABLE)
                .with(csrf())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Story("Crear Valoración")
    @Description("Verifica que crear valoración sin autenticación retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear valoración sin auth retorna 401")
    void testCreateReviewWithoutAuthReturns401() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(LocalDate.now())
                .stars(4.5)
                .description("Intento sin auth")
                .build();

        mockMvc.perform(post(API_REVIEWS)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Crear Valoración")
    @Description("Verifica validación de estrellas mínimas")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Validación de estrellas mínimas")
    void testCreateReviewInvalidStarsMin() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(LocalDate.now())
                .stars(0.0)
                .description("Estrellas inválidas")
                .build();

        mockMvc.perform(post(API_REVIEWS)
                .with(csrf())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Crear Valoración")
    @Description("Verifica validación de estrellas máximas")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Validación de estrellas máximas")
    void testCreateReviewInvalidStarsMax() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(LocalDate.now())
                .stars(6.0)
                .description("Estrellas inválidas")
                .build();

        mockMvc.perform(post(API_REVIEWS)
                .with(csrf())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Crear Valoración")
    @Description("Verifica validación de descripción vacía")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Validación de descripción vacía")
    void testCreateReviewEmptyDescription() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(LocalDate.now())
                .stars(4.0)
                .description("")
                .build();

        mockMvc.perform(post(API_REVIEWS)
                .with(csrf())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
