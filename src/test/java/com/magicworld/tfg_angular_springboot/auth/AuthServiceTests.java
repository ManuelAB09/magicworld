package com.magicworld.tfg_angular_springboot.auth;

import com.magicworld.tfg_angular_springboot.exceptions.EmailAlreadyExistsException;
import com.magicworld.tfg_angular_springboot.exceptions.InvalidCredentialsException;
import com.magicworld.tfg_angular_springboot.exceptions.PasswordsDoNoMatchException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.exceptions.UsernameAlreadyExistsException;
import com.magicworld.tfg_angular_springboot.user.Role;
import com.magicworld.tfg_angular_springboot.user.User;
import com.magicworld.tfg_angular_springboot.user.UserDTO;
import com.magicworld.tfg_angular_springboot.user.UserRepository;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
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
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Autenticación y Autorización")
@Feature("Servicio de Autenticación")
public class AuthServiceTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        existingUser = userRepository.save(User.builder()
                .username("existinguser")
                .firstname("John")
                .lastname("Doe")
                .email("john@example.com")
                .password(passwordEncoder.encode("Password1@"))
                .userRole(Role.USER)
                .build());
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @Story("Login")
    @Description("Verifica que login exitoso retorna token")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Login exitoso retorna token")
    void testLoginSuccessReturnsToken() {
        LoginRequest request = LoginRequest.builder()
                .username("existinguser")
                .password("Password1@")
                .build();
        AuthResponse response = authService.login(request);
        assertNotNull(response.getToken());
    }

    @Test
    @Story("Login")
    @Description("Verifica que el token retornado es válido")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Login exitoso retorna token válido")
    void testLoginSuccessTokenIsValid() {
        LoginRequest request = LoginRequest.builder()
                .username("existinguser")
                .password("Password1@")
                .build();
        AuthResponse response = authService.login(request);
        String username = jwtService.getUsernameFromToken(response.getToken());
        assertEquals("existinguser", username);
    }

    @Test
    @Story("Login")
    @Description("Verifica que login con usuario inexistente lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Login con usuario inexistente lanza excepción")
    void testLoginUserNotFound() {
        LoginRequest request = LoginRequest.builder()
                .username("nonexistent")
                .password("Password1@")
                .build();
        assertThrows(ResourceNotFoundException.class, () -> authService.login(request));
    }

    @Test
    @Story("Login")
    @Description("Verifica que login con contraseña inválida lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Login con contraseña inválida lanza excepción")
    void testLoginInvalidPassword() {
        LoginRequest request = LoginRequest.builder()
                .username("existinguser")
                .password("WrongPassword1@")
                .build();
        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @Story("Registro")
    @Description("Verifica que registro exitoso retorna token")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Registro exitoso retorna token")
    void testRegisterSuccessReturnsToken() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .firstname("Jane")
                .lastname("Smith")
                .email("jane@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .build();
        AuthResponse response = authService.register(request);
        assertNotNull(response.getToken());
    }

    @Test
    @Story("Registro")
    @Description("Verifica que registro exitoso crea usuario")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Registro exitoso crea usuario")
    void testRegisterSuccessCreatesUser() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .firstname("Jane")
                .lastname("Smith")
                .email("jane@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .build();
        authService.register(request);
        assertTrue(userRepository.existsByUsername("newuser"));
    }

    @Test
    @Story("Registro")
    @Description("Verifica que registro con username existente lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Registro con username existente lanza excepción")
    void testRegisterUsernameAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .firstname("Jane")
                .lastname("Smith")
                .email("different@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .build();
        assertThrows(UsernameAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    @Story("Registro")
    @Description("Verifica que registro con email existente lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Registro con email existente lanza excepción")
    void testRegisterEmailAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("differentuser")
                .firstname("Jane")
                .lastname("Smith")
                .email("john@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .build();
        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    @Story("Registro")
    @Description("Verifica que registro con contraseñas diferentes lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Registro con contraseñas diferentes lanza excepción")
    void testRegisterPasswordsDoNotMatch() {
        RegisterRequest request = RegisterRequest.builder()
                .username("anotheruser")
                .firstname("Jane")
                .lastname("Smith")
                .email("another@example.com")
                .password("Password1@")
                .confirmPassword("DifferentPassword1@")
                .build();
        assertThrows(PasswordsDoNoMatchException.class, () -> authService.register(request));
    }

    @Test
    @Story("Obtener Usuario Actual")
    @Description("Verifica que getCurrentUser retorna username correcto")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getCurrentUser retorna username correcto")
    void testGetCurrentUserReturnsUsername() {
        String token = jwtService.getToken(existingUser);
        UserDTO dto = authService.getCurrentUser(token);
        assertEquals("existinguser", dto.getUsername());
    }

    @Test
    @Story("Obtener Usuario Actual")
    @Description("Verifica que getCurrentUser retorna email correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getCurrentUser retorna email correcto")
    void testGetCurrentUserReturnsEmail() {
        String token = jwtService.getToken(existingUser);
        UserDTO dto = authService.getCurrentUser(token);
        assertEquals("john@example.com", dto.getEmail());
    }

    @Test
    @Story("Obtener Usuario Actual")
    @Description("Verifica que getCurrentUser con token de usuario inexistente lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getCurrentUser con usuario inexistente lanza excepción")
    void testGetCurrentUserNotFound() {
        String fakeToken = jwtService.getToken(User.builder()
                .username("nonexistent")
                .password("fake")
                .userRole(Role.USER)
                .build());
        assertThrows(ResourceNotFoundException.class, () -> authService.getCurrentUser(fakeToken));
    }
}
