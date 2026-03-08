package com.magicworld.tfg_angular_springboot.configuration.oauth2;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

    private final HttpCookieOAuth2AuthorizationRequestRepository repository =
            new HttpCookieOAuth2AuthorizationRequestRepository();

    @Test
    void shouldRoundTripAuthorizationRequestInCookie() {
        MockHttpServletRequest saveRequest = new MockHttpServletRequest();
        MockHttpServletResponse saveResponse = new MockHttpServletResponse();

        OAuth2AuthorizationRequest original = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("client-id")
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .scopes(Set.of("openid", "profile"))
                .state("state-123")
                .additionalParameters(Map.of("prompt", "consent"))
                .attributes(Map.of("registration_id", "google", "code_verifier", "verifier-123"))
                .authorizationRequestUri("https://accounts.google.com/o/oauth2/v2/auth?client_id=client-id")
                .build();

        repository.saveAuthorizationRequest(original, saveRequest, saveResponse);

        Cookie cookie = extractCookie(saveResponse);
        MockHttpServletRequest loadRequest = new MockHttpServletRequest();
        loadRequest.setCookies(cookie);

        OAuth2AuthorizationRequest loaded = repository.loadAuthorizationRequest(loadRequest);

        assertNotNull(loaded);
        assertEquals(original.getAuthorizationUri(), loaded.getAuthorizationUri());
        assertEquals(original.getClientId(), loaded.getClientId());
        assertEquals(original.getRedirectUri(), loaded.getRedirectUri());
        assertEquals(original.getState(), loaded.getState());
        assertEquals(original.getScopes(), loaded.getScopes());
        assertEquals("consent", loaded.getAdditionalParameters().get("prompt"));
        assertEquals("google", loaded.getAttributes().get("registration_id"));
        assertEquals("verifier-123", loaded.getAttributes().get("code_verifier"));
    }

    @Test
    void shouldReturnNullForMalformedCookieValue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("oauth2_auth_request", "not-base64"));

        OAuth2AuthorizationRequest loaded = repository.loadAuthorizationRequest(request);

        assertNull(loaded);
    }

    @Test
    void shouldReturnNullWhenPayloadMissesRequiredFields() {
        String payload = Base64.getUrlEncoder().encodeToString("{}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("oauth2_auth_request", payload));

        OAuth2AuthorizationRequest loaded = repository.loadAuthorizationRequest(request);

        assertNull(loaded);
    }

    private Cookie extractCookie(MockHttpServletResponse response) {
        String setCookie = response.getHeader("Set-Cookie");
        assertNotNull(setCookie);

        String cookieValue = setCookie.substring(setCookie.indexOf('=') + 1, setCookie.indexOf(';'));
        return new Cookie("oauth2_auth_request", cookieValue);
    }
}

