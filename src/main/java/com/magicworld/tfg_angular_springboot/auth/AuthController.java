package com.magicworld.tfg_angular_springboot.auth;

import com.magicworld.tfg_angular_springboot.configuration.CookieUtils;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.reset_token.PasswordResetService;
import com.magicworld.tfg_angular_springboot.reset_token.ResetPasswordRequest;
import com.magicworld.tfg_angular_springboot.user.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        addTokenCookie(response, httpRequest, authResponse.getToken(), 2 * 60 * 60);
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
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        addTokenCookie(response, httpRequest, authResponse.getToken(), 2 * 60 * 60);
        return ResponseEntity.status(201).body(new AuthResponse(null));
    }
    @Operation(summary = "User logout", description = "Logout user and clear JWT token", tags = {"Authentication"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout successful", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping(value = "/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest, HttpServletResponse response) {
        addTokenCookie(response, httpRequest, "", 0);
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

    private void addTokenCookie(HttpServletResponse response, HttpServletRequest request, String tokenValue, int maxAge) {
        CookieUtils.addCookie(response, request, "token", tokenValue, true, maxAge);
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
        boolean secure = CookieUtils.isSecureRequest(request);

        String sameSite = secure ? "None" : "Lax";

        ResponseCookie cookie = ResponseCookie.from("XSRF-TOKEN", token.getToken())
                .path("/")
                .httpOnly(false)
                .secure(secure)
                .partitioned(secure)
                .sameSite(sameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.setHeader("X-XSRF-TOKEN", token.getToken());
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

    @Operation(summary = "Complete OAuth2 registration", description = "Set password for a new user coming from OAuth2 (Google)", tags = {"Authentication"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registration completed successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid data or token", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid or expired pending token", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content)
    })
    @PostMapping("/oauth2/complete-registration")
    public ResponseEntity<AuthResponse> completeOAuth2Registration(
            @Valid @RequestBody OAuth2CompleteRegistrationRequest request,
            @CookieValue(name = "oauth2_pending", required = false) String pendingToken,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        if (pendingToken == null || pendingToken.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        AuthResponse authResponse = authService.completeOAuth2Registration(pendingToken, request);
        CookieUtils.addCookie(response, httpRequest, "token", authResponse.getToken(), true, 2 * 60 * 60);
        CookieUtils.addCookie(response, httpRequest, "oauth2_pending", "", true, 0);

        return ResponseEntity.status(201).body(new AuthResponse(null));
    }

}
