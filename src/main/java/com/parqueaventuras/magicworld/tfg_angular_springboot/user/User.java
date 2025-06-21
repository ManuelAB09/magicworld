package com.parqueaventuras.magicworld.tfg_angular_springboot.user;

import com.parqueaventuras.magicworld.tfg_angular_springboot.util.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User extends BaseEntity implements UserDetails {

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 1, max = 50, message = "Username must be between 1 and 50 characters")
    private String username;

    @NotBlank(message = "Firstname cannot be blank")
    @Size(min = 1, max = 50, message = "Firstname must be between 1 and 50 characters")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s'-]+$", message = "Firstname must contain only letters and valid characters")
    @Column(nullable = false)
    private String firstname;

    @NotBlank(message = "Lastname cannot be blank")
    @Size(min = 1, max = 50, message = "Lastname must be between 1 and 50 characters")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s'-]+$", message = "Lastname must contain only letters and valid characters")
    @Column(nullable = false)
    private String lastname;

    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, one special character, and be at least 8 characters long."
    )
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
