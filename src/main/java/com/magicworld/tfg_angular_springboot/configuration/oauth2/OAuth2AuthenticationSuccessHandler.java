package com.magicworld.tfg_angular_springboot.configuration.oauth2;

import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.user.User;
import com.magicworld.tfg_angular_springboot.user.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final int TOKEN_MAX_AGE = 2 * 60 * 60;
    private static final int PENDING_TOKEN_MAX_AGE = 10 * 60;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            String token = jwtService.getToken(existingUser.get());
            addTokenCookie(response, token);
            getRedirectStrategy().sendRedirect(request, response, frontendUrl);
        } else {
            String firstname = oAuth2User.getAttribute("given_name");
            String lastname = oAuth2User.getAttribute("family_name");
            String pendingToken = jwtService.generateOAuth2PendingToken(email, firstname, lastname);
            addPendingTokenCookie(response, pendingToken);
            getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/oauth2-set-password");
        }
    }

    private void addTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(TOKEN_MAX_AGE);
        response.addCookie(cookie);
    }

    private void addPendingTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("oauth2_pending", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(PENDING_TOKEN_MAX_AGE);
        response.addCookie(cookie);
    }
}

