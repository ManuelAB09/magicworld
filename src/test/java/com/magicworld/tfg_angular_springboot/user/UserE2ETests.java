package com.magicworld.tfg_angular_springboot.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.purchase.Purchase;
import com.magicworld.tfg_angular_springboot.purchase.PurchaseRepository;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLineRepository;
import com.magicworld.tfg_angular_springboot.review.Review;
import com.magicworld.tfg_angular_springboot.review.ReviewRepository;
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
@Epic("Gestión de Usuarios")
@Feature("Tests E2E de Usuarios")
public class UserE2ETests {

    private static final String API_USERS_PROFILE = "/api/v1/users/profile";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseLineRepository purchaseLineRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        purchaseLineRepository.deleteAll();
        purchaseRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .username("e2euser")
                .firstname("Test")
                .lastname("User")
                .email("e2e@example.com")
                .password("$2a$10$abcdefghijklmnopqrstuv")
                .userRole(Role.USER)
                .build());
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Actualizar Perfil")
    @Description("Verifica que actualizar perfil E2E retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar perfil E2E retorna 200 OK")
    void testUpdateProfileE2EReturnsOk() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("Updated")
                .lastname("Name")
                .email("updated@example.com")
                .build();

        mockMvc.perform(put(API_USERS_PROFILE)
                .with(csrf())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Actualizar Perfil")
    @Description("Verifica que actualizar perfil persiste en BD")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar perfil persiste en BD")
    void testUpdateProfileE2EPersistsInDatabase() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("Persisted")
                .lastname("User")
                .email("persisted@example.com")
                .build();

        mockMvc.perform(put(API_USERS_PROFILE)
                .with(csrf())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        User updated = userRepository.findByUsername("e2euser").orElseThrow();
        assertEquals("Persisted", updated.getFirstname());
        assertEquals("persisted@example.com", updated.getEmail());
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Actualizar Perfil")
    @Description("Verifica que cambio de contraseña persiste")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Cambio de contraseña persiste en BD")
    void testUpdateProfilePasswordPersists() throws Exception {
        String oldPassword = testUser.getPassword();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("Test")
                .lastname("User")
                .email("e2e@example.com")
                .password("NewSecurePass123!")
                .build();

        mockMvc.perform(put(API_USERS_PROFILE)
                .with(csrf())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        User updated = userRepository.findByUsername("e2euser").orElseThrow();
        assertNotEquals(oldPassword, updated.getPassword());
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Actualizar Perfil")
    @Description("Verifica que email duplicado retorna error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Email duplicado retorna error E2E")
    void testUpdateProfileDuplicateEmailReturnsError() throws Exception {
        userRepository.save(User.builder()
                .username("otheruser")
                .firstname("Other")
                .lastname("User")
                .email("other@example.com")
                .password("Password123!")
                .userRole(Role.USER)
                .build());

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("Test")
                .lastname("User")
                .email("other@example.com")
                .build();

        mockMvc.perform(put(API_USERS_PROFILE)
                .with(csrf())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Eliminar Perfil")
    @Description("Verifica que eliminar perfil E2E retorna 204")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar perfil E2E retorna 204")
    void testDeleteProfileE2EReturnsNoContent() throws Exception {
        mockMvc.perform(delete(API_USERS_PROFILE)
                .with(csrf())
                .with(user(testUser)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Eliminar Perfil")
    @Description("Verifica que eliminar perfil elimina de BD")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar perfil elimina de BD")
    void testDeleteProfileE2ERemovesFromDatabase() throws Exception {
        Long userId = testUser.getId();

        mockMvc.perform(delete(API_USERS_PROFILE)
                .with(csrf())
                .with(user(testUser)))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsById(userId));
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Eliminar Perfil")
    @Description("Verifica que eliminar perfil con compras funciona")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar perfil con compras funciona E2E")
    void testDeleteProfileWithPurchasesE2E() throws Exception {
        Purchase purchase = purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now())
                .buyer(testUser)
                .build());

        mockMvc.perform(delete(API_USERS_PROFILE)
                .with(csrf())
                .with(user(testUser)))
                .andExpect(status().isNoContent());

        assertFalse(purchaseRepository.existsById(purchase.getId()));
        assertFalse(userRepository.existsById(testUser.getId()));
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Eliminar Perfil")
    @Description("Verifica que eliminar perfil con reviews funciona")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar perfil con reviews funciona E2E")
    void testDeleteProfileWithReviewsE2E() throws Exception {
        Purchase purchase = purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now())
                .buyer(testUser)
                .build());
        Review review = reviewRepository.save(Review.builder()
                .stars(4.5)
                .publicationDate(LocalDate.now())
                .visitDate(LocalDate.now())
                .description("Test review")
                .purchase(purchase)
                .build());

        mockMvc.perform(delete(API_USERS_PROFILE)
                .with(csrf())
                .with(user(testUser)))
                .andExpect(status().isNoContent());

        assertFalse(reviewRepository.existsById(review.getId()));
        assertFalse(purchaseRepository.existsById(purchase.getId()));
        assertFalse(userRepository.existsById(testUser.getId()));
    }

    @Test
    @Story("Actualizar Perfil")
    @Description("Verifica que actualizar perfil sin auth retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar perfil sin auth retorna 401")
    void testUpdateProfileWithoutAuthReturns401() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("Test")
                .lastname("User")
                .email("test@example.com")
                .build();

        mockMvc.perform(put(API_USERS_PROFILE)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Story("Eliminar Perfil")
    @Description("Verifica que eliminar perfil sin auth retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar perfil sin auth retorna 401")
    void testDeleteProfileWithoutAuthReturns401() throws Exception {
        mockMvc.perform(delete(API_USERS_PROFILE)
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "e2euser", roles = "USER")
    @Story("Actualizar Perfil")
    @Description("Verifica validación de contraseña débil")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contraseña débil retorna error")
    void testUpdateProfileWeakPasswordReturnsError() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("Test")
                .lastname("User")
                .email("e2e@example.com")
                .password("weak")
                .build();

        mockMvc.perform(put(API_USERS_PROFILE)
                .with(csrf())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
