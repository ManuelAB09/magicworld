package com.magicworld.tfg_angular_springboot.configuration.oauth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

        StoredAuthorizationRequest storedRequest = StoredAuthorizationRequest.from(authorizationRequest);
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(storedRequest);
            String serialized = Base64.getUrlEncoder().encodeToString(jsonBytes);
            addCookie(response, request, serialized);
        } catch (JsonProcessingException e) {
            removeCookie(request, response);
        }
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
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                try {
                    byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    StoredAuthorizationRequest storedRequest = objectMapper.readValue(json, StoredAuthorizationRequest.class);
                    return storedRequest.toAuthorizationRequest();
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

    private static final class StoredAuthorizationRequest {
        public String authorizationUri;
        public String clientId;
        public String redirectUri;
        public Set<String> scopes;
        public String state;
        public String authorizationRequestUri;
        public Map<String, String> additionalParameters;
        public Map<String, String> attributes;

        private static StoredAuthorizationRequest from(OAuth2AuthorizationRequest request) {
            StoredAuthorizationRequest stored = new StoredAuthorizationRequest();
            stored.authorizationUri = request.getAuthorizationUri();
            stored.clientId = request.getClientId();
            stored.redirectUri = request.getRedirectUri();
            stored.scopes = request.getScopes() == null ? Collections.emptySet() : new LinkedHashSet<>(request.getScopes());
            stored.state = request.getState();
            stored.authorizationRequestUri = request.getAuthorizationRequestUri();
            stored.additionalParameters = toStringMap(request.getAdditionalParameters());
            stored.attributes = toStringMap(request.getAttributes());
            return stored;
        }

        private OAuth2AuthorizationRequest toAuthorizationRequest() {
            if (isBlank(authorizationUri) || isBlank(clientId) || isBlank(redirectUri)) {
                return null;
            }
            Map<String, Object> safeAdditionalParameters = toObjectMap(additionalParameters);
            Map<String, Object> safeAttributes = toObjectMap(attributes);

            OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.authorizationCode()
                    .authorizationUri(authorizationUri)
                    .clientId(clientId)
                    .redirectUri(redirectUri)
                    .scopes(scopes == null ? Collections.emptySet() : scopes)
                    .state(state)
                    .additionalParameters(safeAdditionalParameters)
                    .attributes(safeAttributes);

            if (!isBlank(authorizationRequestUri)) {
                builder.authorizationRequestUri(authorizationRequestUri);
            }
            return builder.build();
        }

        private static Map<String, String> toStringMap(Map<String, Object> source) {
            if (source == null || source.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, String> result = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : source.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    result.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
            return result;
        }

        private static Map<String, Object> toObjectMap(Map<String, String> source) {
            if (source == null || source.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.putAll(source);
            return result;
        }

        private static boolean isBlank(String value) {
            return value == null || value.isBlank();
        }
    }
}
