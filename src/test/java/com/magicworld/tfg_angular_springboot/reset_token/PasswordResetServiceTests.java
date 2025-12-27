package com.magicworld.tfg_angular_springboot.reset_token;

import com.magicworld.tfg_angular_springboot.email.EmailService;
import com.magicworld.tfg_angular_springboot.exceptions.BadRequestException;
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
                .username("testuser")
                .firstname("Test")
                .lastname("User")
                .email("test@example.com")
                .password(passwordEncoder.encode("OldPassword1@"))
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
    void testCreatePasswordResetToken_createsToken() {
        passwordResetService.createPasswordResetToken("test@example.com");
        assertFalse(tokenRepository.findAll().isEmpty());
    }

    @Test
    @Story("Crear Token de Restablecimiento")
    @Description("Verifica que se envía email con el token")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear token envía email")
    void testCreatePasswordResetToken_sendsEmail() {
        passwordResetService.createPasswordResetToken("test@example.com");
        verify(emailService).sendSimpleMessage(eq("test@example.com"), anyString(), anyString());
    }

    @Test
    @Story("Crear Token de Restablecimiento")
    @Description("Verifica que lanza excepción si el usuario no existe")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear token con usuario inexistente lanza excepción")
    void testCreatePasswordResetToken_userNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> passwordResetService.createPasswordResetToken("nonexistent@example.com"));
    }

    @Test
    @Story("Crear Token de Restablecimiento")
    @Description("Verifica que se eliminan tokens antiguos al crear uno nuevo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear token elimina tokens antiguos")
    void testCreatePasswordResetToken_deletesOldTokens() {
        passwordResetService.createPasswordResetToken("test@example.com");
        passwordResetService.createPasswordResetToken("test@example.com");
        assertEquals(1, tokenRepository.findAll().size());
    }

    @Test
    @Story("Restablecer Contraseña")
    @Description("Verifica que se restablece la contraseña correctamente")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Restablecer contraseña exitoso")
    void testResetPassword_success() {
        PasswordResetToken token = tokenRepository.save(PasswordResetToken.builder()
                .token("valid-token")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build());
        passwordResetService.resetPassword("valid-token", "NewPassword1@");
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("NewPassword1@", updatedUser.getPassword()));
    }

    @Test
    @Story("Restablecer Contraseña")
    @Description("Verifica que se elimina el token después de usarlo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Restablecer contraseña elimina token")
    void testResetPassword_deletesToken() {
        tokenRepository.save(PasswordResetToken.builder()
                .token("valid-token-2")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build());
        passwordResetService.resetPassword("valid-token-2", "NewPassword1@");
        assertTrue(tokenRepository.findByToken("valid-token-2").isEmpty());
    }

    @Test
    @Story("Restablecer Contraseña")
    @Description("Verifica que lanza excepción con token no encontrado")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Restablecer con token inexistente lanza excepción")
    void testResetPassword_tokenNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> passwordResetService.resetPassword("invalid-token", "NewPassword1@"));
    }

    @Test
    @Story("Restablecer Contraseña")
    @Description("Verifica que lanza excepción con token expirado")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Restablecer con token expirado lanza excepción")
    void testResetPassword_tokenExpired() {
        tokenRepository.save(PasswordResetToken.builder()
                .token("expired-token")
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .build());
        assertThrows(InvalidTokenException.class,
                () -> passwordResetService.resetPassword("expired-token", "NewPassword1@"));
    }

    @Test
    @Story("Restablecer Contraseña")
    @Description("Verifica que lanza excepción con contraseña null")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Restablecer con contraseña null lanza excepción")
    void testResetPassword_invalidPassword_null() {
        tokenRepository.save(PasswordResetToken.builder()
                .token("token-for-null")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build());
        assertThrows(BadRequestException.class,
                () -> passwordResetService.resetPassword("token-for-null", null));
    }

    @Test
    @Story("Restablecer Contraseña")
    @Description("Verifica que lanza excepción con contraseña débil")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Restablecer con contraseña débil lanza excepción")
    void testResetPassword_invalidPassword_weak() {
        tokenRepository.save(PasswordResetToken.builder()
                .token("token-for-weak")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build());
        assertThrows(BadRequestException.class,
                () -> passwordResetService.resetPassword("token-for-weak", "weak"));
    }
}
