package hightech.edu.Inscription.service;

import hightech.edu.Inscription.model.AdminUser;
import hightech.edu.Inscription.model.PasswordResetToken;
import hightech.edu.Inscription.repository.AdminUserRepository;
import hightech.edu.Inscription.repository.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final AdminUserRepository adminUserRepository;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Transactional
    public boolean sendResetEmail(String email) {
        // Check that an admin with this email exists in the DB
        Optional<AdminUser> userOpt = adminUserRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        // Delete any existing token for this user
        tokenRepository.deleteByUsername(email);

        // Generate token and save it
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUsername(email);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        // Send reset email
        String resetLink = baseUrl + "/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("EduManager — Réinitialisation de votre mot de passe");
        message.setText(
            "Bonjour " + userOpt.get().getPrenom() + ",\n\n" +
            "Une demande de réinitialisation de mot de passe a été effectuée pour votre compte (" + email + ").\n\n" +
            "Cliquez sur ce lien pour définir un nouveau mot de passe (valable 30 minutes) :\n\n" +
            resetLink + "\n\n" +
            "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\n" +
            "— EduManager / HighTech EDU"
        );
        mailSender.send(message);
        return true;
    }

    public Optional<PasswordResetToken> validateToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(t -> !t.isExpired() && !t.isUsed());
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> opt = validateToken(token);
        if (opt.isEmpty()) return false;

        PasswordResetToken resetToken = opt.get();
        String email = resetToken.getUsername();

        // Update password directly in the AdminUser DB record
        adminUserRepository.findByEmail(email).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            adminUserRepository.save(user);
        });

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        return true;
    }
}
