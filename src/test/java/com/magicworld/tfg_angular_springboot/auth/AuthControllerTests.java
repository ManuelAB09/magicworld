package com.magicworld.tfg_angular_springboot.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
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
    public void testLogin_success_returnsOk() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("Password1@")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(AuthResponse.builder().token("fake-token").build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Story("Login")
    @Description("Verifica que login exitoso establece cookie de token")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Login exitoso establece cookie token")
    public void testLogin_success_setsCookie() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("Password1@")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(AuthResponse.builder().token("fake-token").build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(cookie().exists("token"));
    }

    @Test
    @Story("Login")
    @Description("Verifica que login con credenciales inválidas retorna 401")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Login inválido retorna 401")
    public void testLogin_invalidCredentials_returns401() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("WrongPassword")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Story("Login")
    @Description("Verifica que login con usuario no encontrado retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Login usuario no encontrado retorna 404")
    public void testLogin_userNotFound_returns404() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("nonexistent")
                .password("Password1@")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new ResourceNotFoundException("nonexistent"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Story("Registro")
    @Description("Verifica que registro exitoso retorna 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Registro exitoso retorna 201")
    public void testRegister_success_returns201() throws Exception {
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

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @Story("Registro")
    @Description("Verifica que registro exitoso establece cookie de token")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Registro exitoso establece cookie token")
    public void testRegister_success_setsCookie() throws Exception {
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

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(cookie().exists("token"));
    }

    @Test
    @Story("Logout")
    @Description("Verifica que logout retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Logout retorna 200 OK")
    public void testLogout_returnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk());
    }

    @Test
    @Story("Logout")
    @Description("Verifica que logout limpia cookie de token")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Logout limpia cookie token")
    public void testLogout_clearsTokenCookie() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(cookie().maxAge("token", 0));
    }

    @Test
    @Story("Obtener Usuario Actual")
    @Description("Verifica que /me retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("/me retorna 200 OK")
    public void testMe_returnsOk() throws Exception {
        UserDTO userDTO = new UserDTO("testuser", "Test", "User", "test@example.com", Role.USER);

        when(jwtAuthenticationFilter.getTokenFromRequest(any())).thenReturn("fake-token");
        when(authService.getCurrentUser("fake-token")).thenReturn(userDTO);

        mockMvc.perform(get("/api/v1/auth/me")
                        .cookie(new jakarta.servlet.http.Cookie("token", "fake-token")))
                .andExpect(status().isOk());
    }

    @Test
    @Story("CSRF Token")
    @Description("Verifica que obtener CSRF token retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("CSRF token retorna 200 OK")
    public void testCsrfToken_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf-token"))
                .andExpect(status().isOk());
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
    }
}
