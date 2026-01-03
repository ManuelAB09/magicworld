package com.magicworld.tfg_angular_springboot.user;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Gestión de Usuarios")
@Feature("Entidad Usuario")
public class UserTests {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_FIRSTNAME = "Test";
    private static final String TEST_LASTNAME = "User";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Password1@";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username(TEST_USERNAME)
                .firstname(TEST_FIRSTNAME)
                .lastname(TEST_LASTNAME)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .userRole(Role.USER)
                .build();
    }

    @Test
    @Story("Autoridades de Usuario")
    @Description("Verifica que getAuthorities retorna el rol correcto para USER")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getAuthorities retorna ROLE_USER para usuario con rol USER")
    void testGetAuthoritiesReturnsCorrectRole() {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_USER)));
    }

    @Test
    @Story("Autoridades de Usuario")
    @Description("Verifica que getAuthorities retorna el rol correcto para ADMIN")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getAuthorities retorna ROLE_ADMIN para usuario con rol ADMIN")
    void testGetAuthoritiesAdminRoleReturnsCorrectRole() {
        user.setUserRole(Role.ADMIN);
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_ADMIN)));
    }

    @Test
    @Story("Estado de Cuenta")
    @Description("Verifica que isAccountNonExpired retorna true")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("isAccountNonExpired retorna true")
    void testIsAccountNonExpiredReturnsTrue() {
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    @Story("Estado de Cuenta")
    @Description("Verifica que isAccountNonLocked retorna true")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("isAccountNonLocked retorna true")
    void testIsAccountNonLockedReturnsTrue() {
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    @Story("Estado de Cuenta")
    @Description("Verifica que isCredentialsNonExpired retorna true")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("isCredentialsNonExpired retorna true")
    void testIsCredentialsNonExpiredReturnsTrue() {
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    @Story("Estado de Cuenta")
    @Description("Verifica que isEnabled retorna true")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("isEnabled retorna true")
    void testIsEnabledReturnsTrue() {
        assertTrue(user.isEnabled());
    }

    @Test
    @Story("Propiedades de Usuario")
    @Description("Verifica que getUsername retorna el nombre de usuario correcto")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getUsername retorna el nombre de usuario correcto")
    void testGetUsernameReturnsCorrectUsername() {
        assertEquals(TEST_USERNAME, user.getUsername());
    }

    @Test
    @Story("Propiedades de Usuario")
    @Description("Verifica que getPassword retorna la contraseña correcta")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getPassword retorna la contraseña correcta")
    void testGetPasswordReturnsCorrectPassword() {
        assertEquals(TEST_PASSWORD, user.getPassword());
    }
}
