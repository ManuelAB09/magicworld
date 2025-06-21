package com.parqueaventuras.magicworld.tfg_angular_springboot.auth;

import com.parqueaventuras.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.parqueaventuras.magicworld.tfg_angular_springboot.exceptions.EmailAlreadyExistsException;
import com.parqueaventuras.magicworld.tfg_angular_springboot.exceptions.PasswordsDoNoMatchException;
import com.parqueaventuras.magicworld.tfg_angular_springboot.exceptions.UsernameAlreadyExistsException;
import com.parqueaventuras.magicworld.tfg_angular_springboot.user.Role;
import com.parqueaventuras.magicworld.tfg_angular_springboot.user.User;
import com.parqueaventuras.magicworld.tfg_angular_springboot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + request.getUsername()));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.getToken(user);
        return AuthResponse.builder()
                .token(token)
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Name already registered");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordsDoNoMatchException("Passwords do not match");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(encodedPassword)
                .role(Role.USER)
                .build();
        userRepository.save(user);
        return AuthResponse.builder()
                .token(jwtService.getToken(user))
                .build();
    }
}
