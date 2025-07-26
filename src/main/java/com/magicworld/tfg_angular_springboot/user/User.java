package com.magicworld.tfg_angular_springboot.user;

import com.magicworld.tfg_angular_springboot.util.BaseEntity;
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
    @NotBlank(message = "validation.username.required")
    @Size(min = 1, max = 50, message = "validation.username.size")
    private String username;

    @NotBlank(message = "validation.firstname.required")
    @Size(min = 1, max = 50, message = "validation.firstname.size")
    @Pattern(regexp = "^[\\p{L}\\s'-]+$", message = "validation.firstname.pattern")
    @Column(nullable = false)
    private String firstname;

    @NotBlank(message = "validation.lastname.required")
    @Size(min = 1, max = 50, message = "validation.lastname.size")
    @Pattern(regexp = "^[\\p{L}\\s'-]+$", message = "validation.lastname.pattern")
    @Column(nullable = false)
    private String lastname;

    @Email(message = "validation.email.invalid")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "validation.password.required")
    @Size(min = 8, max = 100, message = "validation.password.size")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
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
