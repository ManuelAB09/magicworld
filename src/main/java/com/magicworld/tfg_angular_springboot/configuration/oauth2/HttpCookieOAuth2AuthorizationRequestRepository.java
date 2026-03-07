package com.magicworld.tfg_angular_springboot.configuration.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookieValue(request);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeCookie(request, response);
            return;
        }
        String serialized = Base64.getUrlEncoder().encodeToString(
                SerializationUtils.serialize(authorizationRequest));
        addCookie(response, request, serialized);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
            HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = getCookieValue(request);
        if (authRequest != null) {
            removeCookie(request, response);
        }
        return authRequest;
    }

    private OAuth2AuthorizationRequest getCookieValue(HttpServletRequest request) {
        if (request.getCookies() == null)
            return null;
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                try {
                    byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
                    try (ObjectInputStream ois = new ObjectInputStream(
                            new ByteArrayInputStream(bytes))) {
                        return (OAuth2AuthorizationRequest) ois.readObject();
                    }
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    private void addCookie(HttpServletResponse response, HttpServletRequest request, String value) {
        boolean secure = isSecure(request);
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, value)
                .path("/")
                .httpOnly(true)
                .secure(secure)
                .sameSite(secure ? "None" : "Lax")
                .maxAge(COOKIE_EXPIRE_SECONDS)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void removeCookie(HttpServletRequest request, HttpServletResponse response) {
        boolean secure = isSecure(request);
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .path("/")
                .httpOnly(true)
                .secure(secure)
                .sameSite(secure ? "None" : "Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private boolean isSecure(HttpServletRequest request) {
        String proto = request.getHeader("X-Forwarded-Proto");
        return request.isSecure()
                || "https".equalsIgnoreCase(request.getScheme())
                || (proto != null && proto.toLowerCase().contains("https"));
    }
}
