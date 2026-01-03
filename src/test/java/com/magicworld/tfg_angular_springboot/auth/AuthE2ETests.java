package com.magicworld.tfg_angular_springboot.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@Epic("Autenticación y Autorización")
@Feature("API REST de Autenticación E2E")
public class AuthE2ETests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User existingUser = User.builder()
                .username("existinguser")
                .email("existing@example.com")
                .password(passwordEncoder.encode("Password1@"))
                .firstname("Existing")
                .lastname("User")
                .userRole(Role.USER)
                .build();
        userRepository.save(existingUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @Story("Login")
    @Description("Verifica que login con credenciales válidas retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Login válido retorna 200 OK")
    void testLoginValidCredentialsReturnsOk() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("existinguser")
                .password("Password1@")
                .build();

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Login")
    @Description("Verifica que login con credenciales válidas establece cookie")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Login válido establece cookie")
    void testLoginValidCredentialsSetsCookie() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("existinguser")
                .password("Password1@")
                .build();

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(cookie().exists("token"))
                .andReturn();
        assertNotNull(result.getResponse().getCookie("token"));
    }

    @Test
    @Story("Login")
    @Description("Verifica que login con contraseña inválida retorna 401")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Login con contraseña inválida retorna 401")
    void testLoginInvalidPasswordReturns401() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("existinguser")
                .password("WrongPassword1@")
                .build();

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        assertEquals(401, result.getResponse().getStatus());
    }

    @Test
    @Story("Login")
    @Description("Verifica que login con usuario inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Login con usuario inexistente retorna 404")
    void testLoginNonexistentUserReturns404() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("nonexistent")
                .password("Password1@")
                .build();

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }

    @Test
    @Story("Registro")
    @Description("Verifica que registro con datos válidos retorna 201")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Registro válido retorna 201")
    void testRegisterValidDataReturns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .firstname("New")
                .lastname("User")
                .build();

        var result = mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    @Story("Registro")
    @Description("Verifica que registro con datos válidos establece cookie")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Registro válido establece cookie")
    void testRegisterValidDataSetsCookie() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser2")
                .email("newuser2@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .firstname("New")
                .lastname("User")
                .build();

        var result = mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(cookie().exists("token"))
                .andReturn();
        assertNotNull(result.getResponse().getCookie("token"));
    }

    @Test
    @Story("Registro")
    @Description("Verifica que registro con username existente retorna 409")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Registro con username existente retorna 409")
    void testRegisterExistingUsernameReturns409() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .email("new@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .firstname("New")
                .lastname("User")
                .build();

        var result = mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andReturn();
        assertEquals(409, result.getResponse().getStatus());
    }

    @Test
    @Story("Registro")
    @Description("Verifica que registro con email existente retorna 409")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Registro con email existente retorna 409")
    void testRegisterExistingEmailReturns409() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("differentuser")
                .email("existing@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .firstname("New")
                .lastname("User")
                .build();

        var result = mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andReturn();
        assertEquals(409, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser
    @Story("Logout")
    @Description("Verifica que logout retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Logout retorna 200 OK")
    void testLogoutReturnsOk() throws Exception {
        var result = mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser
    @Story("Logout")
    @Description("Verifica que logout expira la cookie")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Logout expira cookie")
    void testLogoutExpiresCookie() throws Exception {
        var result = mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf()))
                .andExpect(cookie().maxAge("token", 0))
                .andReturn();
        assertEquals(0, result.getResponse().getCookie("token").getMaxAge());
    }

    @Test
    @WithMockUser
    @Story("CSRF Token")
    @Description("Verifica que obtener CSRF token retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("CSRF token retorna 200 OK")
    void testCsrfTokenReturnsOk() throws Exception {
        var result = mockMvc.perform(get("/api/v1/auth/csrf-token")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser
    @Story("CSRF Token")
    @Description("Verifica que obtener CSRF token establece header X-XSRF-TOKEN")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("CSRF token establece header")
    void testCsrfTokenSetsXsrfHeader() throws Exception {
        var result = mockMvc.perform(get("/api/v1/auth/csrf-token")
                        .with(csrf()))
                .andExpect(header().exists("X-XSRF-TOKEN"))
                .andReturn();
        assertNotNull(result.getResponse().getHeader("X-XSRF-TOKEN"));
    }

    @Test
    @Story("Reset Password")
    @Description("Verifica que reset password con token inválido retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Reset password con token inválido retorna 404")
    void testResetPasswordInvalidTokenReturns404() throws Exception {
        String requestBody = "{\"token\":\"invalidtoken\",\"newPassword\":\"NewPassword1@\"}";

        var result = mockMvc.perform(post("/api/v1/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }
}
