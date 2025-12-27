package com.magicworld.tfg_angular_springboot.configuration.jwt;

import com.magicworld.tfg_angular_springboot.user.Role;
import com.magicworld.tfg_angular_springboot.user.User;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Epic("Seguridad")
@Feature("Servicio JWT")
public class JwtServiceTests {

    @Autowired
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password1@")
                .firstname("Test")
                .lastname("User")
                .userRole(Role.USER)
                .build();
    }

    @Test
    @Story("Generar Token")
    @Description("Verifica que generar token retorna un token no nulo")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Generar token retorna token no nulo")
    void testGetToken_returnsNonNullToken() {
        String token = jwtService.getToken(testUser);
        assertNotNull(token);
    }

    @Test
    @Story("Generar Token")
    @Description("Verifica que generar token retorna un token no vacío")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Generar token retorna token no vacío")
    void testGetToken_returnsNonEmptyToken() {
        String token = jwtService.getToken(testUser);
        assertFalse(token.isEmpty());
    }

    @Test
    @Story("Extraer Username")
    @Description("Verifica que extraer username del token retorna el username correcto")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Extraer username retorna username correcto")
    void testGetUsernameFromToken_returnsCorrectUsername() {
        String token = jwtService.getToken(testUser);
        String username = jwtService.getUsernameFromToken(token);
        assertEquals("testuser", username);
    }

    @Test
    @Story("Validar Token")
    @Description("Verifica que validar token válido retorna true")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Token válido retorna true")
    void testIsTokenValid_withValidToken_returnsTrue() {
        String token = jwtService.getToken(testUser);
        boolean isValid = jwtService.isTokenValid(token, testUser);
        assertTrue(isValid);
    }

    @Test
    @Story("Validar Token")
    @Description("Verifica que validar token con usuario diferente retorna false")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Token con usuario diferente retorna false")
    void testIsTokenValid_withDifferentUser_returnsFalse() {
        String token = jwtService.getToken(testUser);
        User differentUser = User.builder()
                .username("differentuser")
                .email("different@example.com")
                .password("Password1@")
                .userRole(Role.USER)
                .build();
        boolean isValid = jwtService.isTokenValid(token, differentUser);
        assertFalse(isValid);
    }

    @Test
    @Story("Expiración de Token")
    @Description("Verifica que token nuevo no está expirado")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Token nuevo no está expirado")
    void testIsTokenExpired_withNewToken_returnsFalse() {
        String token = jwtService.getToken(testUser);
        boolean isExpired = jwtService.isTokenExpired(token);
        assertFalse(isExpired);
    }

    @Test
    @Story("Generar Token")
    @Description("Verifica que generar token para admin retorna token válido")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Generar token para admin retorna token válido")
    void testGetToken_forAdminUser_returnsValidToken() {
        User adminUser = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("Password1@")
                .userRole(Role.ADMIN)
                .build();
        String token = jwtService.getToken(adminUser);
        assertNotNull(token);
    }

    @Test
    @Story("Extraer Username")
    @Description("Verifica que extraer username para admin retorna username correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Extraer username para admin retorna username correcto")
    void testGetUsernameFromToken_forAdminUser_returnsCorrectUsername() {
        User adminUser = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("Password1@")
                .userRole(Role.ADMIN)
                .build();
        String token = jwtService.getToken(adminUser);
        assertEquals("admin", jwtService.getUsernameFromToken(token));
    }
}

