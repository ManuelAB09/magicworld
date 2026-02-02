package com.magicworld.tfg_angular_springboot.user;

import com.magicworld.tfg_angular_springboot.exceptions.InvalidTokenException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "The users management API")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @Operation(summary = "Update current user's profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = getUserFromContext();
        User saved = userService.updateProfile(user, request);
        return ResponseEntity.ok(toDTO(saved));
    }

    @Operation(summary = "Delete current user's account")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteProfile(
            HttpServletResponse response) {
        User user = getUserFromContext();
        userService.deleteUserWithRelatedData(user);

        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }

    private User getUserFromContext() {
        return getUser(userRepository);
    }

    @NonNull
    public static User getUser(UserRepository userRepository) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null || auth.getName().isBlank()) {
            throw new InvalidTokenException();
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(InvalidTokenException::new);
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getUsername(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getUserRole()
        );
    }
}
