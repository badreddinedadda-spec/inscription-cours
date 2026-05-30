package hightech.edu.Inscription.service;

import hightech.edu.Inscription.model.AdminUser;
import hightech.edu.Inscription.model.PasswordResetToken;
import hightech.edu.Inscription.repository.AdminUserRepository;
import hightech.edu.Inscription.repository.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminUserRepository adminUserRepository;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    @Value("${mailtrap.api.token}")
    private String mailtrapToken;

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

        // Envoyer via Mailtrap API (HTTP) — pas de port SMTP bloqué
        String resetLink = baseUrl + "/reset-password?token=" + token;
        sendViaMailtrapApi(email, userOpt.get().getPrenom(), resetLink);

        return true;
    }

    private void sendViaMailtrapApi(String toEmail, String prenom, String resetLink) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Api-Token", mailtrapToken);

            String body = """
                {
                  "from": {"email": "noreply@hightech.edu", "name": "HighTech EDU"},
                  "to": [{"email": "%s"}],
                  "subject": "HighTech EDU — Réinitialisation de votre mot de passe",
                  "text": "Bonjour %s,\\n\\nCliquez sur ce lien pour réinitialiser votre mot de passe (valable 30 minutes) :\\n\\n%s\\n\\n— HighTech EDU"
                }
                """.formatted(toEmail, prenom, resetLink);

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://send.api.mailtrap.io/api/send",
                    request,
                    String.class
            );

            System.out.println("[PasswordResetService] Email envoyé via API : " + response.getStatusCode());

        } catch (Exception e) {
            System.err.println("[PasswordResetService] Erreur API Mailtrap : " + e.getMessage());
        }
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