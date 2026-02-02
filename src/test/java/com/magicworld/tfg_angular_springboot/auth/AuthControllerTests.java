package com.magicworld.tfg_angular_springboot.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
import com.magicworld.tfg_angular_springboot.exceptions.InvalidCredentialsException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.reset_token.PasswordResetService;
import com.magicworld.tfg_angular_springboot.user.Role;
import com.magicworld.tfg_angular_springboot.user.UserDTO;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Autenticación y Autorización")
@Feature("API REST de Autenticación")
public class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @Story("Login")
    @Description("Verifica que login exitoso retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Login exitoso retorna 200 OK")
    public void testLoginSuccessReturnsOk() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("Password1@")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(AuthResponse.builder().token("fake-token").build());

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Login")
    @Description("Verifica que login exitoso establece cookie de token")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Login exitoso establece cookie token")
    public void testLoginSuccessSetsCookie() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("Password1@")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(AuthResponse.builder().token("fake-token").build());

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(cookie().exists("token"))
                .andReturn();
        assertNotNull(result.getResponse().getCookie("token"));
    }

    @Test
    @Story("Login")
    @Description("Verifica que login con credenciales inválidas retorna 401")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Login inválido retorna 401")
    public void testLoginInvalidCredentialsReturns401() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("WrongPassword")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException());

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        assertEquals(401, result.getResponse().getStatus());
    }

    @Test
    @Story("Login")
    @Description("Verifica que login con usuario no encontrado retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Login usuario no encontrado retorna 404")
    public void testLoginUserNotFoundReturns404() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("nonexistent")
                .password("Password1@")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new ResourceNotFoundException("nonexistent"));

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }

    @Test
    @Story("Registro")
    @Description("Verifica que registro exitoso retorna 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Registro exitoso retorna 201")
    public void testRegisterSuccessReturns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .firstname("Jane")
                .lastname("Smith")
                .email("jane@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(AuthResponse.builder().token("fake-token").build());

        var result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    @Story("Registro")
    @Description("Verifica que registro exitoso establece cookie de token")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Registro exitoso establece cookie token")
    public void testRegisterSuccessSetsCookie() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .firstname("Jane")
                .lastname("Smith")
                .email("jane@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(AuthResponse.builder().token("fake-token").build());

        var result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(cookie().exists("token"))
                .andReturn();
        assertNotNull(result.getResponse().getCookie("token"));
    }

    @Test
    @Story("Logout")
    @Description("Verifica que logout retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Logout retorna 200 OK")
    public void testLogoutReturnsOk() throws Exception {
        var result = mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Logout")
    @Description("Verifica que logout limpia cookie de token")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Logout limpia cookie token")
    public void testLogoutClearsTokenCookie() throws Exception {
        var result = mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(cookie().maxAge("token", 0))
                .andReturn();
        assertEquals(0, Objects.requireNonNull(result.getResponse().getCookie("token")).getMaxAge());
    }

    @Test
    @Story("Obtener Usuario Actual")
    @Description("Verifica que /me retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("/me retorna 200 OK")
    public void testMeReturnsOk() throws Exception {
        UserDTO userDTO = new UserDTO("testuser", "Test", "User", "test@example.com", Role.USER);

        when(jwtAuthenticationFilter.getTokenFromRequest(any())).thenReturn("fake-token");
        when(authService.getCurrentUser("fake-token")).thenReturn(userDTO);

        var result = mockMvc.perform(get("/api/v1/auth/me")
                        .cookie(new jakarta.servlet.http.Cookie("token", "fake-token")))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("CSRF Token")
    @Description("Verifica que obtener CSRF token retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("CSRF token retorna 200 OK")
    public void testCsrfTokenReturnsOk() throws Exception {
        var result = mockMvc.perform(get("/api/v1/auth/csrf-token"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("CSRF Token")
    @Description("Verifica que CSRF token establece header X-XSRF-TOKEN")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("CSRF token establece header")
    public void testCsrfTokenSetsHeader() throws Exception {
        var result = mockMvc.perform(get("/api/v1/auth/csrf-token"))
                .andReturn();
        assertNotNull(result.getResponse().getHeader("X-XSRF-TOKEN"));
    }

    @Test
    @Story("Recuperar Contraseña")
    @Description("Verifica que forgot-password retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Forgot password retorna 200 OK")
    public void testForgotPasswordReturnsOk() throws Exception {
        var result = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"test@example.com\""))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Restablecer Contraseña")
    @Description("Verifica que reset-password retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Reset password retorna 200 OK")
    public void testResetPasswordReturnsOk() throws Exception {
        var result = mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"valid-token\",\"newPassword\":\"NewPassword1@\"}"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @TestConfiguration
    static class AuthControllerTestConfig {
        @Bean
        public AuthService authService() {
            return Mockito.mock(AuthService.class);
        }

        @Bean
        public JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return Mockito.mock(JwtAuthenticationFilter.class);
        }

        @Bean
        public PasswordResetService passwordResetService() {
            return Mockito.mock(PasswordResetService.class);
        }

        @Bean
        public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
            return Mockito.mock(OAuth2AuthenticationSuccessHandler.class);
        }

        @Bean
        public OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
            return Mockito.mock(OAuth2AuthenticationFailureHandler.class);
        }
    }
}
