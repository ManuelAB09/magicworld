package com.magicworld.tfg_angular_springboot.auth;

import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.reset_token.PasswordResetService;
import com.magicworld.tfg_angular_springboot.reset_token.ResetPasswordRequest;
import com.magicworld.tfg_angular_springboot.user.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name="Authentication",description = "The authentication management API")
public class AuthController {

    private final AuthService authService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final PasswordResetService passwordResetService;

    @Operation(summary = "User login", description = "Authenticate user and return JWT token", tags = {"Authentication"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid login data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping(value = "/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        Cookie cookie = new Cookie("token", authResponse.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(2 * 60 * 60);
        response.addCookie(cookie);
        return ResponseEntity.ok(new AuthResponse(null));
    }
    @Operation(summary = "User registration", description = "Register a new user", tags = {"Authentication"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registration successful", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid registration data", content = @Content),
            @ApiResponse(responseCode = "409", description = "Username or email already exists", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })

    @PostMapping(value = "/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        Cookie cookie = new Cookie("token", authResponse.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(2 * 60 * 60);
        response.addCookie(cookie);
        return ResponseEntity.status(201).body(new AuthResponse(null));
    }
    @Operation(summary = "User logout", description = "Logout user and clear JWT token", tags = {"Authentication"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout successful", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping(value = "/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Expire the cookie
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "Get current user", description = "Returns information about the currently authenticated user", tags = {"Authentication"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User is authenticated", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @GetMapping(value = "/me")
    public ResponseEntity<UserDTO> me(HttpServletRequest request) {
        String token = jwtAuthenticationFilter.getTokenFromRequest(request);
        UserDTO userInfo = authService.getCurrentUser(token);
        return ResponseEntity.ok(userInfo);
    }

    @Operation(
            summary = "Get CSRF token",
            description = "Generates and returns a CSRF token in a cookie to protect against CSRF attacks.",
            tags = {"Authentication"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSRF token generated successfully", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/csrf-token")
    public ResponseEntity<Void> csrf(HttpServletRequest request, HttpServletResponse response) {
        CsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfToken token = repo.generateToken(request);
        // repo.saveToken(token, request, response); // avoid default save if you want to control cookie
        boolean secure = false;
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (request.isSecure() || "https".equalsIgnoreCase(request.getScheme()) ||
                (forwardedProto != null && forwardedProto.toLowerCase().contains("https"))) {
            secure = true;
        }

        String sameSite = "None";

        ResponseCookie cookie = ResponseCookie.from("XSRF-TOKEN", token.getToken())
                .path("/")
                .httpOnly(false)
                .secure(true)
                .sameSite(sameSite)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }



    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody String email) {
        passwordResetService.createPasswordResetToken(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

}
