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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Gestión de Usuarios")
@Feature("Servicio de Usuarios")
public class UserServiceTests {

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Story("Establecer Usuario Actual")
    @Description("Verifica que setCurrentUser establece correctamente el contexto de seguridad")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("setCurrentUser establece el contexto de seguridad correctamente")
    void testSetCurrentUser() {
        User mockUser = Mockito.mock(User.class);
        Mockito.when(mockUser.getUsername()).thenReturn("testUser");
        Mockito.when(mockUser.getPassword()).thenReturn("testPassword");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(mockUser).getAuthorities();

        userService.setCurrentUser(mockUser);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testUser", SecurityContextHolder.getContext().getAuthentication().getName());
        assertEquals("testPassword", SecurityContextHolder.getContext().getAuthentication().getCredentials());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("[ROLE_USER]")));
    }
}
