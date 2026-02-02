package com.magicworld.tfg_angular_springboot.configuration.oauth2;

import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.user.Role;
import com.magicworld.tfg_angular_springboot.user.User;
import com.magicworld.tfg_angular_springboot.user.UserRepository;
import io.qameta.allure.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Epic("Autenticación OAuth2")
@Feature("OAuth2 Success Handler")
public class OAuth2AuthenticationSuccessHandlerTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Authentication authentication;
    @Mock
    private OAuth2User oAuth2User;
    @Mock
    private RedirectStrategy redirectStrategy;

    private OAuth2AuthenticationSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        successHandler = new OAuth2AuthenticationSuccessHandler(userRepository, jwtService);
        ReflectionTestUtils.setField(successHandler, "frontendUrl", "http://localhost:4200");
        successHandler.setRedirectStrategy(redirectStrategy);
    }

    @Test
    @DisplayName("onAuthenticationSuccess genera token JWT")
    @Story("Login OAuth2 Exitoso")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se genera un token JWT al autenticarse con OAuth2")
    void onAuthenticationSuccessGeneratesJwtToken() throws Exception {
        User existingUser = createTestUser();
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.getToken(any(User.class))).thenReturn("test-jwt-token");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(jwtService).getToken(existingUser);
    }

    @Test
    @DisplayName("onAuthenticationSuccess establece cookie de token")
    @Story("Login OAuth2 Exitoso")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se establece una cookie con el token JWT")
    void onAuthenticationSuccessSetsCookie() throws Exception {
        User existingUser = createTestUser();
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.getToken(any(User.class))).thenReturn("test-jwt-token");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        assertEquals("token", cookieCaptor.getValue().getName());
    }

    @Test
    @DisplayName("onAuthenticationSuccess redirige al frontend")
    @Story("Login OAuth2 Exitoso")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se redirige al usuario al frontend")
    void onAuthenticationSuccessRedirectsToFrontend() throws Exception {
        User existingUser = createTestUser();
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.getToken(any(User.class))).thenReturn("test-jwt-token");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(redirectStrategy).sendRedirect(request, response, "http://localhost:4200");
    }

    @Test
    @DisplayName("onAuthenticationSuccess crea usuario nuevo si no existe")
    @Story("Registro OAuth2")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se crea un usuario nuevo si no existe en la base de datos")
    void onAuthenticationSuccessCreatesNewUserIfNotExists() throws Exception {
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("newuser@example.com");
        when(oAuth2User.getAttribute("given_name")).thenReturn("John");
        when(oAuth2User.getAttribute("family_name")).thenReturn("Doe");
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.getToken(any(User.class))).thenReturn("new-user-token");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("onAuthenticationSuccess usa usuario existente si ya existe")
    @Story("Login OAuth2 Exitoso")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que se usa el usuario existente si ya está registrado")
    void onAuthenticationSuccessUsesExistingUser() throws Exception {
        User existingUser = createTestUser();
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.getToken(existingUser)).thenReturn("existing-user-token");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Cookie tiene HttpOnly activado")
    @Story("Seguridad OAuth2")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que la cookie tiene HttpOnly activado por seguridad")
    void cookieHasHttpOnlyEnabled() throws Exception {
        User existingUser = createTestUser();
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.getToken(any(User.class))).thenReturn("test-jwt-token");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        assertTrue(cookieCaptor.getValue().isHttpOnly());
    }

    @Test
    @DisplayName("Cookie tiene MaxAge configurado")
    @Story("Seguridad OAuth2")
    @Severity(SeverityLevel.MINOR)
    @Description("Verifica que la cookie tiene tiempo de expiración")
    void cookieHasMaxAgeSet() throws Exception {
        User existingUser = createTestUser();
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.getToken(any(User.class))).thenReturn("test-jwt-token");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        assertEquals(7200, cookieCaptor.getValue().getMaxAge());
    }

    @Test
    @DisplayName("Usuario nuevo tiene rol USER")
    @Story("Registro OAuth2")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que el usuario nuevo creado tiene rol USER")
    void newUserHasUserRole() throws Exception {
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("newuser@example.com");
        when(oAuth2User.getAttribute("given_name")).thenReturn("John");
        when(oAuth2User.getAttribute("family_name")).thenReturn("Doe");
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.getToken(any(User.class))).thenReturn("new-user-token");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(Role.USER, userCaptor.getValue().getUserRole());
    }

    private User createTestUser() {
        User user = User.builder()
                .username("test@example.com")
                .email("test@example.com")
                .firstname("Test")
                .lastname("User")
                .password("")
                .userRole(Role.USER)
                .build();
        user.setId(1L);
        return user;
    }
}
