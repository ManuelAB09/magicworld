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
    void testDoFilterInternal_noToken_continuesChain() throws Exception {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Story("Filtrar Peticiones")
    @Description("Verifica que sin token no hay autenticación")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Sin token no hay autenticación")
    void testDoFilterInternal_noToken_noAuthentication() throws Exception {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Story("Autenticación JWT")
    @Description("Verifica que con token válido se establece autenticación")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Token válido establece autenticación")
    void testDoFilterInternal_withValidToken_setsAuthentication() throws Exception {
        String token = "valid-token";
        request.setCookies(new Cookie("token", token));

        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        when(jwtService.getUsernameFromToken(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Story("Autenticación JWT")
    @Description("Verifica que con token válido la cadena de filtros continúa")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Token válido continúa cadena de filtros")
    void testDoFilterInternal_withValidToken_continuesChain() throws Exception {
        String token = "valid-token";
        request.setCookies(new Cookie("token", token));

        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        when(jwtService.getUsernameFromToken(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Story("Autenticación JWT")
    @Description("Verifica que con token inválido no hay autenticación")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Token inválido no establece autenticación")
    void testDoFilterInternal_withInvalidToken_noAuthentication() throws Exception {
        String token = "invalid-token";
        request.setCookies(new Cookie("token", token));

        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        when(jwtService.getUsernameFromToken(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Story("Autenticación JWT")
    @Description("Verifica que con username null no hay autenticación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Username null no establece autenticación")
    void testDoFilterInternal_nullUsername_noAuthentication() throws Exception {
        String token = "some-token";
        request.setCookies(new Cookie("token", token));

        when(jwtService.getUsernameFromToken(token)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Story("Obtener Token")
    @Description("Verifica que con cookie de token retorna el token")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Con cookie de token retorna token")
    void testGetTokenFromRequest_withTokenCookie_returnsToken() {
        request.setCookies(new Cookie("token", "test-token"));
        String token = jwtAuthenticationFilter.getTokenFromRequest(request);
        assertEquals("test-token", token);
    }

    @Test
    @Story("Obtener Token")
    @Description("Verifica que sin cookies retorna null")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Sin cookies retorna null")
    void testGetTokenFromRequest_withoutCookies_returnsNull() {
        String token = jwtAuthenticationFilter.getTokenFromRequest(request);
        assertNull(token);
    }

    @Test
    @Story("Obtener Token")
    @Description("Verifica que con otra cookie retorna null")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Con otra cookie retorna null")
    void testGetTokenFromRequest_withOtherCookie_returnsNull() {
        request.setCookies(new Cookie("other", "value"));
        String token = jwtAuthenticationFilter.getTokenFromRequest(request);
        assertNull(token);
    }
}

