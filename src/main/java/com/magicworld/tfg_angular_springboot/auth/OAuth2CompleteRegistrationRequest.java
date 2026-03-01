package com.magicworld.tfg_angular_springboot.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2CompleteRegistrationRequest {

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "validation.password.pattern"
    )
    @NotBlank(message = "validation.password.required")
    private String password;

    @NotBlank(message = "validation.confirmPassword.required")
    private String confirmPassword;
}
