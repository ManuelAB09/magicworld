package com.magicworld.tfg_angular_springboot.configuration.oauth2;

import io.qameta.allure.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@Epic("Autenticación OAuth2")
@Feature("OAuth2 Failure Handler")
public class OAuth2AuthenticationFailureHandlerTests {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private AuthenticationException exception;
    @Mock
    private RedirectStrategy redirectStrategy;

    private OAuth2AuthenticationFailureHandler failureHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        failureHandler = new OAuth2AuthenticationFailureHandler();
        ReflectionTestUtils.setField(failureHandler, "frontendUrl", "http://localhost:4200");
        failureHandler.setRedirectStrategy(redirectStrategy);
    }

    @Test
    @DisplayName("onAuthenticationFailure redirige con error")
    @Story("Error en OAuth2")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que al fallar OAuth2 se redirige con parámetro de error")
    void onAuthenticationFailureRedirectsWithError() throws Exception {
        failureHandler.onAuthenticationFailure(request, response, exception);

        verify(redirectStrategy).sendRedirect(request, response, "http://localhost:4200/login?error=oauth2_error");
    }

    @Test
    @DisplayName("onAuthenticationFailure usa URL de frontend configurada")
    @Story("Error en OAuth2")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que se usa la URL del frontend configurada")
    void onAuthenticationFailureUsesFrontendUrl() throws Exception {
        ReflectionTestUtils.setField(failureHandler, "frontendUrl", "https://myapp.com");

        failureHandler.onAuthenticationFailure(request, response, exception);

        verify(redirectStrategy).sendRedirect(request, response, "https://myapp.com/login?error=oauth2_error");
    }

    @Test
    @DisplayName("onAuthenticationFailure llama a redirectStrategy")
    @Story("Error en OAuth2")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que se usa la estrategia de redirección")
    void onAuthenticationFailureCallsRedirectStrategy() throws Exception {
        failureHandler.onAuthenticationFailure(request, response, exception);

        verify(redirectStrategy, times(1)).sendRedirect(any(), any(), anyString());
    }

    @Test
    @DisplayName("onAuthenticationFailure redirige a página de login")
    @Story("Error en OAuth2")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que redirige a la página de login")
    void onAuthenticationFailureRedirectsToLoginPage() throws Exception {
        failureHandler.onAuthenticationFailure(request, response, exception);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("/login"));
    }
}
