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

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Password1@";
    private static final String TEST_FIRSTNAME = "Test";
    private static final String TEST_LASTNAME = "User";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String DIFFERENT_USERNAME = "differentuser";
    private static final String DIFFERENT_EMAIL = "different@example.com";

    @Autowired
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .firstname(TEST_FIRSTNAME)
                .lastname(TEST_LASTNAME)
                .userRole(Role.USER)
                .build();
    }

    @Test
    @Story("Generar Token")
    @Description("Verifica que generar token retorna un token no nulo")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Generar token retorna token no nulo")
    void testGetTokenReturnsNonNullToken() {
        String token = jwtService.getToken(testUser);
        assertNotNull(token);
    }

    @Test
    @Story("Generar Token")
    @Description("Verifica que generar token retorna un token no vacío")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Generar token retorna token no vacío")
    void testGetTokenReturnsNonEmptyToken() {
        String token = jwtService.getToken(testUser);
        assertFalse(token.isEmpty());
    }

    @Test
    @Story("Extraer Username")
    @Description("Verifica que extraer username del token retorna el username correcto")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Extraer username retorna username correcto")
    void testGetUsernameFromTokenReturnsCorrectUsername() {
        String token = jwtService.getToken(testUser);
        String username = jwtService.getUsernameFromToken(token);
        assertEquals(TEST_USERNAME, username);
    }

    @Test
    @Story("Validar Token")
    @Description("Verifica que validar token válido retorna true")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Token válido retorna true")
    void testIsTokenValidWithValidTokenReturnsTrue() {
        String token = jwtService.getToken(testUser);
        boolean isValid = jwtService.isTokenValid(token, testUser);
        assertTrue(isValid);
    }

    @Test
    @Story("Validar Token")
    @Description("Verifica que validar token con usuario diferente retorna false")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Token con usuario diferente retorna false")
    void testIsTokenValidWithDifferentUserReturnsFalse() {
        String token = jwtService.getToken(testUser);
        User differentUser = User.builder()
                .username(DIFFERENT_USERNAME)
                .email(DIFFERENT_EMAIL)
                .password(TEST_PASSWORD)
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
    void testIsTokenExpiredWithNewTokenReturnsFalse() {
        String token = jwtService.getToken(testUser);
        boolean isExpired = jwtService.isTokenExpired(token);
        assertFalse(isExpired);
    }

    @Test
    @Story("Generar Token")
    @Description("Verifica que generar token para admin retorna token válido")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Generar token para admin retorna token válido")
    void testGetTokenForAdminUserReturnsValidToken() {
        User adminUser = User.builder()
                .username(ADMIN_USERNAME)
                .email(ADMIN_EMAIL)
                .password(TEST_PASSWORD)
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
    void testGetUsernameFromTokenForAdminUserReturnsCorrectUsername() {
        User adminUser = User.builder()
                .username(ADMIN_USERNAME)
                .email(ADMIN_EMAIL)
                .password(TEST_PASSWORD)
                .userRole(Role.ADMIN)
                .build();
        String token = jwtService.getToken(adminUser);
        assertEquals(ADMIN_USERNAME, jwtService.getUsernameFromToken(token));
    }
}
