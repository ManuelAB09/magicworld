package com.magicworld.tfg_angular_springboot.reset_token;

import com.magicworld.tfg_angular_springboot.email.EmailService;
import com.magicworld.tfg_angular_springboot.exceptions.InvalidPasswordPattern;
import com.magicworld.tfg_angular_springboot.exceptions.InvalidTokenException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.user.User;
import com.magicworld.tfg_angular_springboot.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend.url:${FRONTEND_URL:http://localhost:4200}}")
    private String frontendUrl;

    @Transactional
    public void createPasswordResetToken(String email) {
        createPasswordResetToken(email, "es");
        }

        @Transactional
        public void createPasswordResetToken(String email, String locale) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(email));
        tokenRepository.deleteAllByUser(user);
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();
        tokenRepository.save(resetToken);

        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String normalizedLang = normalizeLang(locale);
        boolean isSpanish = normalizedLang.startsWith("es");

        String displayName = user.getFirstname();
        if (displayName == null || displayName.isBlank()) {
            displayName = user.getUsername();
        }

        String subject = isSpanish
            ? "Recuperación de contraseña - MagicWorld"
            : "Password Recovery - MagicWorld";

        Map<String, Object> vars = new HashMap<>();
        vars.put("subject", subject);
        vars.put("headerText", isSpanish ? "Recuperación de contraseña" : "Password Recovery");
        vars.put("greeting", isSpanish ? "¡Hola " + displayName + "!" : "Hello " + displayName + "!");
        vars.put("introText", isSpanish
            ? "Hemos recibido una solicitud para restablecer tu contraseña."
            : "We received a request to reset your password.");
        vars.put("actionText", isSpanish
            ? "Haz clic en el botón de abajo para continuar."
            : "Click the button below to continue.");
        vars.put("buttonLabel", isSpanish ? "Restablecer contraseña" : "Reset Password");
        vars.put("resetLink", resetUrl);
        vars.put("expiryText", isSpanish
            ? "Este enlace expira en 15 minutos."
            : "This link expires in 15 minutes.");
        vars.put("linkFallback", isSpanish
            ? "Si el botón no funciona, copia y pega este enlace en tu navegador:"
            : "If the button doesn't work, copy and paste this link into your browser:");
        vars.put("footerMessage", isSpanish
            ? "Si no solicitaste este cambio, puedes ignorar este correo."
            : "If you did not request this change, you can ignore this email.");
        vars.put("footerRights", isSpanish ? "Todos los derechos reservados" : "All rights reserved");
        vars.put("contactInfo", isSpanish
            ? "Contáctanos en info@magicworld.com"
            : "Contact us at info@magicworld.com");

        emailService.sendHtmlEmailWithQr(user.getEmail(), subject, "password-reset", vars, null);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {

        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (newPassword == null || !newPassword.matches(passwordPattern)) {
            throw new InvalidPasswordPattern();
        }
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(token));
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException();
        }
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken);
    }

    private String normalizeLang(String locale) {
        if (locale == null || locale.isBlank()) {
            return "es";
        }
        String primary = locale.split(",")[0].trim();
        if (primary.isEmpty()) {
            return "es";
        }
        return primary.toLowerCase();
    }

}
