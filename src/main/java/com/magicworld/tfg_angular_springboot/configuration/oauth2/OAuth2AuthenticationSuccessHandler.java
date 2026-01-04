package com.magicworld.tfg_angular_springboot.configuration.oauth2;

import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.user.Role;
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

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final int TOKEN_MAX_AGE = 2 * 60 * 60;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        User user = processOAuth2User(oAuth2User);
        String token = jwtService.getToken(user);
        addTokenCookie(response, token);
        getRedirectStrategy().sendRedirect(request, response, frontendUrl);
    }

    private User processOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        return userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(oAuth2User, email));
    }

    private User createNewUser(OAuth2User oAuth2User, String email) {
        User newUser = User.builder()
                .email(email)
                .username(email)
                .firstname(oAuth2User.getAttribute("given_name"))
                .lastname(oAuth2User.getAttribute("family_name"))
                .password("")
                .userRole(Role.USER)
                .build();
        return userRepository.save(newUser);
    }

    private void addTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(TOKEN_MAX_AGE);
        response.addCookie(cookie);
    }
}

