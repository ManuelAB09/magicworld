package com.magicworld.tfg_angular_springboot.user;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "validation.firstname.required")
    @Size(min = 1, max = 50, message = "validation.firstname.size")
    @Pattern(regexp = "^[\\p{L}\\s'-]+$", message = "validation.firstname.pattern")
    private String firstname;

    @NotBlank(message = "validation.lastname.required")
    @Size(min = 1, max = 50, message = "validation.lastname.size")
    @Pattern(regexp = "^[\\p{L}\\s'-]+$", message = "validation.lastname.pattern")
    private String lastname;

    @Email(message = "validation.email.invalid")
    @NotBlank(message = "validation.email.required")
    private String email;

    @Size(max = 100, message = "validation.password.size")
    private String password;
}
