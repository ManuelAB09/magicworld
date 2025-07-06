package com.magicworld.tfg_angular_springboot.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "validation.username.required")
    String username;

    @NotBlank(message = "validation.password.required")
    String password;
}
