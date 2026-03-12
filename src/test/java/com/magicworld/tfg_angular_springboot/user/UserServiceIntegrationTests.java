package com.magicworld.tfg_angular_springboot.user;

import com.magicworld.tfg_angular_springboot.exceptions.UsernameAlreadyExistsException;
import com.magicworld.tfg_angular_springboot.purchase.PurchaseRepository;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLineRepository;
import com.magicworld.tfg_angular_springboot.review.ReviewRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Gestión de Usuarios")
@Feature("Servicio de Usuarios - Integración")
public class UserServiceIntegrationTests {

    private static final String ORIGINAL_USERNAME = "usrintuser";
    private static final String ORIGINAL_EMAIL = "usrint@example.com";
    private static final String UPDATED_USERNAME = "usrintupdated";
    private static final String OTHER_USERNAME = "usrintother";
    private static final String OTHER_EMAIL = "usrintother@example.com";

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private PurchaseRepository purchaseRepository;
    @Autowired private PurchaseLineRepository purchaseLineRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        reviewRepository.deleteAll();
        purchaseLineRepository.deleteAll();
        purchaseRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Story("Actualizar Perfil")
    @Description("Verifica que cambiar username exitosamente actualiza el usuario")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("updateProfileConCambioDeUsernameActualizaCorrectamente")
    void updateProfileConCambioDeUsernameActualizaCorrectamente() {
        userRepository.save(User.builder()
                .username(ORIGINAL_USERNAME).firstname("John").lastname("Doe")
                .email(ORIGINAL_EMAIL).password("Password123!")
                .userRole(Role.USER).build());
        User user;
        entityManager.flush();
        entityManager.clear();

        user = userRepository.findByUsername(ORIGINAL_USERNAME).orElseThrow();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .username(UPDATED_USERNAME).firstname("John").lastname("Doe")
                .email(ORIGINAL_EMAIL).build();

        User updated = userService.updateProfile(user, request);

        assertEquals(UPDATED_USERNAME, updated.getUsername());
    }

    @Test
    @Story("Actualizar Perfil")
    @Description("Verifica que cambiar username a uno existente lanza excepción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("updateProfileConUsernameDuplicadoLanzaExcepcion")
    void updateProfileConUsernameDuplicadoLanzaExcepcion() {
        userRepository.save(User.builder()
                .username(OTHER_USERNAME).firstname("Other").lastname("User")
                .email(OTHER_EMAIL).password("Password123!")
                .userRole(Role.USER).build());

        userRepository.save(User.builder()
                .username(ORIGINAL_USERNAME).firstname("John").lastname("Doe")
                .email(ORIGINAL_EMAIL).password("Password123!")
                .userRole(Role.USER).build());
        User user;
        entityManager.flush();
        entityManager.clear();

        user = userRepository.findByUsername(ORIGINAL_USERNAME).orElseThrow();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .username(OTHER_USERNAME).firstname("John").lastname("Doe")
                .email(ORIGINAL_EMAIL).build();

        User finalUser = user;
        assertThrows(UsernameAlreadyExistsException.class,
                () -> userService.updateProfile(finalUser, request));
    }

    @Test
    @Story("Actualizar Perfil")
    @Description("Verifica que username null o vacío no cambia el username")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("updateProfileConUsernameNullNoCambiaUsername")
    void updateProfileConUsernameNullNoCambiaUsername() {
        userRepository.save(User.builder()
                .username(ORIGINAL_USERNAME).firstname("John").lastname("Doe")
                .email(ORIGINAL_EMAIL).password("Password123!")
                .userRole(Role.USER).build());
        User user;
        entityManager.flush();
        entityManager.clear();

        user = userRepository.findByUsername(ORIGINAL_USERNAME).orElseThrow();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .username(null).firstname("Updated").lastname("Name")
                .email(ORIGINAL_EMAIL).build();

        User updated = userService.updateProfile(user, request);

        assertEquals(ORIGINAL_USERNAME, updated.getUsername());
    }

    @Test
    @Story("Actualizar Perfil")
    @Description("Verifica que username en blanco no cambia el username")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("updateProfileConUsernameEnBlancoNoCambiaUsername")
    void updateProfileConUsernameEnBlancoNoCambiaUsername() {
        userRepository.save(User.builder()
                .username(ORIGINAL_USERNAME).firstname("John").lastname("Doe")
                .email(ORIGINAL_EMAIL).password("Password123!")
                .userRole(Role.USER).build());
        User user;
        entityManager.flush();
        entityManager.clear();

        user = userRepository.findByUsername(ORIGINAL_USERNAME).orElseThrow();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .username("  ").firstname("Updated").lastname("Name")
                .email(ORIGINAL_EMAIL).build();

        User updated = userService.updateProfile(user, request);

        assertEquals(ORIGINAL_USERNAME, updated.getUsername());
    }

    @Test
    @Story("Actualizar Perfil")
    @Description("Verifica que mismo username no cambia nada")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("updateProfileConMismoUsernameNoCambiaNada")
    void updateProfileConMismoUsernameNoCambiaNada() {
        userRepository.save(User.builder()
                .username(ORIGINAL_USERNAME).firstname("John").lastname("Doe")
                .email(ORIGINAL_EMAIL).password("Password123!")
                .userRole(Role.USER).build());
        User user;
        entityManager.flush();
        entityManager.clear();

        user = userRepository.findByUsername(ORIGINAL_USERNAME).orElseThrow();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .username(ORIGINAL_USERNAME).firstname("Updated").lastname("Name")
                .email(ORIGINAL_EMAIL).build();

        User updated = userService.updateProfile(user, request);

        assertEquals(ORIGINAL_USERNAME, updated.getUsername());
        assertEquals("Updated", updated.getFirstname());
    }

    @Test
    @Story("Actualizar Perfil")
    @Description("Verifica que contraseña con solo minúsculas falla el patrón")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("updateProfileConPasswordSoloMinusculasLanzaExcepcion")
    void updateProfileConPasswordSoloMinusculasLanzaExcepcion() {
        userRepository.save(User.builder()
                .username(ORIGINAL_USERNAME).firstname("John").lastname("Doe")
                .email(ORIGINAL_EMAIL).password("Password123!")
                .userRole(Role.USER).build());
        User user;
        entityManager.flush();
        entityManager.clear();

        user = userRepository.findByUsername(ORIGINAL_USERNAME).orElseThrow();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("John").lastname("Doe")
                .email(ORIGINAL_EMAIL).password("onlylowercase")
                .build();

        User finalUser = user;
        assertThrows(Exception.class, () -> userService.updateProfile(finalUser, request));
    }

    @Test
    @Story("Eliminar Usuario")
    @Description("Verifica que deleteUserWithRelatedData elimina usuario correctamente")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("deleteUserWithRelatedDataEliminaUsuarioCorrectamente")
    void deleteUserWithRelatedDataEliminaUsuarioCorrectamente() {
        User user = userRepository.save(User.builder()
                .username(ORIGINAL_USERNAME).firstname("John").lastname("Doe")
                .email(ORIGINAL_EMAIL).password("Password123!")
                .userRole(Role.USER).build());
        entityManager.flush();

        userService.deleteUserWithRelatedData(user);
        entityManager.flush();

        assertFalse(userRepository.findByUsername(ORIGINAL_USERNAME).isPresent());
    }
}

