package com.magicworld.tfg_angular_springboot.reset_token;

import com.magicworld.tfg_angular_springboot.user.User;
import com.magicworld.tfg_angular_springboot.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "password_reset_token")
public class PasswordResetToken extends BaseEntity {

    private String token;
    private LocalDateTime expiryDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
