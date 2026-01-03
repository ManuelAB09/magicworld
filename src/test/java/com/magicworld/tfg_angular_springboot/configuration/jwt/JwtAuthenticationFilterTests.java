package com.magicworld.tfg_angular_springboot.configuration.jwt;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Epic("Seguridad")
@Feature("Filtro de Autenticación JWT")
public class JwtAuthenticationFilterTests {

    private static final String TOKEN_COOKIE_NAME = "token";
    private static final String VALID_TOKEN = "valid-token";
    private static final String INVALID_TOKEN = "invalid-token";
    private static final String SOME_TOKEN = "some-token";
    private static final String TEST_TOKEN = "test-token";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password";

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    @Story("Filtrar Peticiones")
    @Description("Verifica que sin token la cadena de filtros continúa")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Sin token continúa cadena de filtros")
    void testDoFilterInternalNoTokenContinuesChain() throws Exception {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Story("Filtrar Peticiones")
    @Description("Verifica que sin token no hay autenticación")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Sin token no hay autenticación")
    void testDoFilterInternalNoTokenNoAuthentication() throws Exception {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Story("Autenticación JWT")
    @Description("Verifica que con token válido se establece autenticación")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Token válido establece autenticación")
    void testDoFilterInternalWithValidTokenSetsAuthentication() throws Exception {
        request.setCookies(new Cookie(TOKEN_COOKIE_NAME, VALID_TOKEN));

        UserDetails userDetails = User.builder()
                .username(TEST_USERNAME)
                .password(TEST_PASSWORD)
                .authorities(Collections.emptyList())
                .build();

        when(jwtService.getUsernameFromToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);
        when(jwtService.isTokenValid(VALID_TOKEN, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Story("Autenticación JWT")
    @Description("Verifica que con token válido la cadena de filtros continúa")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Token válido continúa cadena de filtros")
    void testDoFilterInternalWithValidTokenContinuesChain() throws Exception {
        request.setCookies(new Cookie(TOKEN_COOKIE_NAME, VALID_TOKEN));

        UserDetails userDetails = User.builder()
                .username(TEST_USERNAME)
                .password(TEST_PASSWORD)
                .authorities(Collections.emptyList())
                .build();

        when(jwtService.getUsernameFromToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);
        when(jwtService.isTokenValid(VALID_TOKEN, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Story("Autenticación JWT")
    @Description("Verifica que con token inválido no hay autenticación")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Token inválido no establece autenticación")
    void testDoFilterInternalWithInvalidTokenNoAuthentication() throws Exception {
        request.setCookies(new Cookie(TOKEN_COOKIE_NAME, INVALID_TOKEN));

        UserDetails userDetails = User.builder()
                .username(TEST_USERNAME)
                .password(TEST_PASSWORD)
                .authorities(Collections.emptyList())
                .build();

        when(jwtService.getUsernameFromToken(INVALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);
        when(jwtService.isTokenValid(INVALID_TOKEN, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Story("Autenticación JWT")
    @Description("Verifica que con username null no hay autenticación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Username null no establece autenticación")
    void testDoFilterInternalNullUsernameNoAuthentication() throws Exception {
        request.setCookies(new Cookie(TOKEN_COOKIE_NAME, SOME_TOKEN));

        when(jwtService.getUsernameFromToken(SOME_TOKEN)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Story("Obtener Token")
    @Description("Verifica que con cookie de token retorna el token")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Con cookie de token retorna token")
    void testGetTokenFromRequestWithTokenCookieReturnsToken() {
        request.setCookies(new Cookie(TOKEN_COOKIE_NAME, TEST_TOKEN));
        String token = jwtAuthenticationFilter.getTokenFromRequest(request);
        assertEquals(TEST_TOKEN, token);
    }

    @Test
    @Story("Obtener Token")
    @Description("Verifica que sin cookies retorna null")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Sin cookies retorna null")
    void testGetTokenFromRequestWithoutCookiesReturnsNull() {
        String token = jwtAuthenticationFilter.getTokenFromRequest(request);
        assertNull(token);
    }

    @Test
    @Story("Obtener Token")
    @Description("Verifica que con otra cookie retorna null")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Con otra cookie retorna null")
    void testGetTokenFromRequestWithOtherCookieReturnsNull() {
        request.setCookies(new Cookie("other", "value"));
        String token = jwtAuthenticationFilter.getTokenFromRequest(request);
        assertNull(token);
    }
}

