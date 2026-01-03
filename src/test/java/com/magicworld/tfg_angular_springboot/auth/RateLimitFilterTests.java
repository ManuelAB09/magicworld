package com.magicworld.tfg_angular_springboot.auth;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Epic("Autenticación y Autorización")
@Feature("Filtro de Rate Limiting")
public class RateLimitFilterTests {

    private RateLimitFilter rateLimitFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
    }

    @Test
    @Story("Rutas No Limitadas")
    @Description("Verifica que rutas no limitadas continúan la cadena de filtros")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Ruta no limitada continúa cadena de filtros")
    void testDoFilterNonRateLimitedPathContinuesChain() throws Exception {
        request.setRequestURI("/api/v1/attractions");
        rateLimitFilter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Story("Rutas Limitadas")
    @Description("Verifica que primera petición a login continúa la cadena")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Primera petición a login continúa cadena")
    void testDoFilterLoginPathFirstRequestContinuesChain() throws Exception {
        request.setRequestURI("/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.1");
        rateLimitFilter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Story("Rutas Limitadas")
    @Description("Verifica que primera petición a register continúa la cadena")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Primera petición a register continúa cadena")
    void testDoFilterRegisterPathFirstRequestContinuesChain() throws Exception {
        request.setRequestURI("/api/v1/auth/register");
        request.setRemoteAddr("192.168.1.2");
        rateLimitFilter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Story("Rutas Limitadas")
    @Description("Verifica que primera petición a forgot-password continúa la cadena")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Primera petición a forgot-password continúa cadena")
    void testDoFilterForgotPasswordPathFirstRequestContinuesChain() throws Exception {
        request.setRequestURI("/api/v1/auth/forgot-password");
        request.setRemoteAddr("192.168.1.3");
        rateLimitFilter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Story("Rutas Limitadas")
    @Description("Verifica que primera petición a reset-password continúa la cadena")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Primera petición a reset-password continúa cadena")
    void testDoFilterResetPasswordPathFirstRequestContinuesChain() throws Exception {
        request.setRequestURI("/api/v1/auth/reset-password");
        request.setRemoteAddr("192.168.1.4");
        rateLimitFilter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Story("Límite de Peticiones")
    @Description("Verifica que exceder límite retorna 429")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Exceder límite de peticiones retorna 429")
    void testDoFilterLoginPathExceedsLimitReturns429() throws Exception {
        request.setRequestURI("/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.100");

        for (int i = 0; i < 5; i++) {
            rateLimitFilter.doFilter(request, response, filterChain);
        }

        response = new MockHttpServletResponse();
        rateLimitFilter.doFilter(request, response, filterChain);

        assertEquals(429, response.getStatus());
    }

    @Test
    @Story("Límite de Peticiones")
    @Description("Verifica que exceder límite retorna error JSON")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Exceder límite retorna error JSON")
    void testDoFilterLoginPathExceedsLimitReturnsJsonError() throws Exception {
        request.setRequestURI("/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.101");

        for (int i = 0; i < 5; i++) {
            rateLimitFilter.doFilter(request, response, filterChain);
        }

        response = new MockHttpServletResponse();
        rateLimitFilter.doFilter(request, response, filterChain);

        assertTrue(response.getContentAsString().contains("error.too.many.requests"));
    }

    @Test
    @Story("Aislamiento de IPs")
    @Description("Verifica que diferentes IPs no comparten límite")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Diferentes IPs no comparten límite")
    void testDoFilterDifferentIpsNoRateLimitCrossover() throws Exception {
        for (int i = 0; i < 5; i++) {
            request = new MockHttpServletRequest();
            request.setRequestURI("/api/v1/auth/login");
            request.setRemoteAddr("192.168.2." + i);
            response = new MockHttpServletResponse();
            rateLimitFilter.doFilter(request, response, filterChain);
            assertEquals(200, response.getStatus());
        }
    }
}
