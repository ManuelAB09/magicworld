// src/main/java/com/magicworld/tfg_angular_springboot/auth/PasswordResetTokenRepository.java
package com.magicworld.tfg_angular_springboot.reset_token;

import com.magicworld.tfg_angular_springboot.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteAllByUser(User user);
}
