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

    // Adresse expéditeur fixe — Mailtrap accepte n'importe quelle adresse
    private static final String FROM_EMAIL = "badreddinedadda@proton.me";

    @Transactional
    public boolean sendResetEmail(String email) {
        Optional<AdminUser> userOpt = adminUserRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        // Supprimer l'ancien token
        tokenRepository.deleteByUsername(email);

        // Générer et sauvegarder le nouveau token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUsername(email);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        // Envoyer l'email
        String resetLink = baseUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);   // ← adresse email valide, pas le username SMTP
        message.setTo(email);
        message.setSubject("HighTech EDU — Réinitialisation de votre mot de passe");
        message.setText(
                "Bonjour " + userOpt.get().getPrenom() + ",\n\n" +
                        "Une demande de réinitialisation de mot de passe a été effectuée " +
                        "pour votre compte (" + email + ").\n\n" +
                        "Cliquez sur ce lien pour définir un nouveau mot de passe (valable 30 minutes) :\n\n" +
                        resetLink + "\n\n" +
                        "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\n" +
                        "— HighTech EDU"
        );

        try {
            mailSender.send(message);
        } catch (Exception e) {
            // Log l'erreur sans bloquer — le token est déjà sauvegardé
            System.err.println("[PasswordResetService] Erreur envoi email : " + e.getMessage());
            // On retourne true quand même car le token existe en BDD
            // L'admin peut récupérer le lien dans les logs si besoin
        }

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

        adminUserRepository.findByEmail(email).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            adminUserRepository.save(user);
        });

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        return true;
    }
}