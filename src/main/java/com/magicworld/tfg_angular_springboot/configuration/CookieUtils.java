package com.magicworld.tfg_angular_springboot.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

public final class CookieUtils {

    private CookieUtils() {}

    public static boolean isSecureRequest(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return request.isSecure()
                || "https".equalsIgnoreCase(request.getScheme())
                || (forwardedProto != null && forwardedProto.toLowerCase().contains("https"));
    }

    public static void addCookie(HttpServletResponse response, HttpServletRequest request,
                                  String name, String value, boolean httpOnly, int maxAge) {
        boolean secure = isSecureRequest(request);
        String sameSite = secure ? "None" : "Lax";

        ResponseCookie cookie = ResponseCookie.from(name, value != null ? value : "")
                .path("/")
                .httpOnly(httpOnly)
                .secure(secure)
                .sameSite(sameSite)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
