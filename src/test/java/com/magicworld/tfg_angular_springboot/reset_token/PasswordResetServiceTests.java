package com.magicworld.tfg_angular_springboot.reset_token;

import com.magicworld.tfg_angular_springboot.email.EmailService;
import com.magicworld.tfg_angular_springboot.exceptions.BadRequestException;
import com.magicworld.tfg_angular_springboot.exceptions.InvalidPasswordPattern;
import com.magicworld.tfg_angular_springboot.exceptions.InvalidTokenException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.user.Role;
import com.magicworld.tfg_angular_springboot.user.User;
import com.magicworld.tfg_angular_springboot.user.UserRepository;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Autenticación y Autorización")
@Feature("Servicio de Restablecimiento de Contraseña")
public class PasswordResetServiceTests {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_FIRSTNAME = "Test";
    private static final String TEST_LASTNAME = "User";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String NONEXISTENT_EMAIL = "nonexistent@example.com";
    private static final String OLD_PASSWORD = "OldPassword1@";
    private static final String NEW_PASSWORD = "NewPassword1@";
    private static final String WEAK_PASSWORD = "weak";
    private static final String VALID_TOKEN = "valid-token";
    private static final String VALID_TOKEN_2 = "valid-token-2";
    private static final String INVALID_TOKEN = "invalid-token";
    private static final String EXPIRED_TOKEN = "expired-token";
    private static final String TOKEN_FOR_NULL = "token-for-null";
    private static final String TOKEN_FOR_WEAK = "token-for-weak";

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EmailService emailService;

    private User testUser;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        testUser = userRepository.save(User.builder()
                .username(TEST_USERNAME)
                .firstname(TEST_FIRSTNAME)
                .lastname(TEST_LASTNAME)
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode(OLD_PASSWORD))
                .userRole(Role.USER)
                .build());
        doNothing().when(emailService).sendSimpleMessage(anyString(), anyString(), anyString());
    }

    @AfterEach
    void tearDown() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Story("Crear Token de Restablecimiento")
    @Description("Verifica que se crea un token de restablecimiento de contraseña")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear token de restablecimiento crea token")
    void testCreatePasswordResetTokenCreatesToken() {
        passwordResetService.createPasswordResetToken(TEST_EMAIL);
        assertFalse(tokenRepository.findAll().isEmpty());
    }

    @Test
    @Story("Crear Token de Restablecimiento")
    @Description("Verifica que se envía email con el token")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear token envía email")
    void testCreatePasswordResetTokenSendsEmail() {
        passwordResetService.createPasswordResetToken(TEST_EMAIL);
        verify(emailService).sendSimpleMessage(eq(TEST_EMAIL), anyString(), anyString());
    }

    @Test
    @Story("Crear Token de Restablecimiento")
    @Description("Verifica que lanza excepción si el usuario no existe")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear token con usuario inexistente lanza excepción")
    void testCreatePasswordResetTokenUserNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> passwordResetService.createPasswordResetToken(NONEXISTENT_EMAIL));
    }

    @Test
    @Story("Crear Token de Restablecimiento")
    @Description("Verifica que se eliminan tokens antiguos al crear uno nuevo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear token elimina tokens antiguos")
    void testCreatePasswordResetTokenDeletesOldTokens() {
        passwordResetService.createPasswordResetToken(TEST_EMAIL);
        passwordResetService.createPasswordResetToken(TEST_EMAIL);
        assertEquals(1, tokenRepository.findAll().size());
    }

    @Test
    @Story("Restablecer Contraseña")
    @Description("Verifica que se restablece la contraseña correctamente")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Restablecer contraseña exitoso")
    void testResetPasswordSuccess() {
        PasswordResetToken token = tokenRepository.save(PasswordResetToken.builder()
                .token(VALID_TOKEN)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build());
        passwordResetService.resetPassword(VALID_TOKEN, NEW_PASSWORD);
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, updatedUser.getPassword()));
    }

    @Test
    @Story("Restablecer Contraseña")
    @Description("Verifica que se elimina el token después de usarlo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Restablecer contraseña elimina token")
    void testResetPasswordDeletesToken() {
        tokenRepository.save(PasswordResetToken.builder()
                .token(VALID_TOKEN_2)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build());
        passwordResetService.resetPassword(VALID_TOKEN_2, NEW_PASSWORD);
        assertTrue(tokenRepository.findByToken(VALID_TOKEN_2).isEmpty());
    }

    @Test
    @Story("Restablecer Contraseña")
    @Description("Verifica que lanza excepción con token no encontrado")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Restablecer con token inexistente lanza excepción")
    void testResetPasswordTokenNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> passwordResetService.resetPassword(INVALID_TOKEN, NEW_PASSWORD));
    }

    @Test
    @Story("Restablecer Contraseña")
    @Description("Verifica que lanza excepción con token expirado")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Restablecer con token expirado lanza excepción")
    void testResetPasswordTokenExpired() {
        tokenRepository.save(PasswordResetToken.builder()
                .token(EXPIRED_TOKEN)
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .build());
        assertThrows(InvalidTokenException.class,
                () -> passwordResetService.resetPassword(EXPIRED_TOKEN, NEW_PASSWORD));
    }

    @Test
    @Story("Restablecer Contraseña")
    @Description("Verifica que lanza excepción con contraseña null")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Restablecer con contraseña null lanza excepción")
    void testResetPasswordInvalidPasswordNull() {
        tokenRepository.save(PasswordResetToken.builder()
                .token(TOKEN_FOR_NULL)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build());
        assertThrows(InvalidPasswordPattern.class,
                () -> passwordResetService.resetPassword(TOKEN_FOR_NULL, null));
    }

    @Test
    @Story("Restablecer Contraseña")
    @Description("Verifica que lanza excepción con contraseña débil")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Restablecer con contraseña débil lanza excepción")
    void testResetPasswordInvalidPasswordWeak() {
        tokenRepository.save(PasswordResetToken.builder()
                .token(TOKEN_FOR_WEAK)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build());
        assertThrows(InvalidPasswordPattern.class,
                () -> passwordResetService.resetPassword(TOKEN_FOR_WEAK, WEAK_PASSWORD));
    }
}
