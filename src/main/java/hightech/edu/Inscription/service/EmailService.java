package hightech.edu.Inscription.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    @Value("${mailtrap.api.token}")
    private String mailtrapToken;

    @Value("${mailtrap.inbox.id:4675903}")
    private String inboxId;

    private void sendViaApi(String toEmail, String subject, String text) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Api-Token", mailtrapToken);

            String safeText = text
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "");

            String body = String.format("""
                {
                  "from": {"email": "noreply@hightech.edu", "name": "HighTech EDU"},
                  "to": [{"email": "%s"}],
                  "subject": "%s",
                  "text": "%s"
                }
                """, toEmail, subject, safeText);

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            // ── Sandbox endpoint
            String url = "https://sandbox.api.mailtrap.io/api/send/" + inboxId;

            restTemplate.postForEntity(url, request, String.class);
            System.out.println("[EmailService] Email envoye a " + toEmail);

        } catch (Exception e) {
            System.err.println("[EmailService] Erreur envoi email a "
                    + toEmail + " : " + e.getMessage());
        }
    }

    public void envoyerAcceptation(String emailEtudiant,
                                   String prenomEtudiant,
                                   String titreCours) {
        sendViaApi(
                emailEtudiant,
                "Votre inscription est validee - HighTech EDU",
                "Bonjour " + prenomEtudiant + ",\n\n" +
                        "Nous avons le plaisir de vous informer que votre inscription au cours :\n\n" +
                        "  " + titreCours + "\n\n" +
                        "a ete validee par notre equipe pedagogique.\n\n" +
                        "Bienvenue chez HighTech EDU !\n\n" +
                        "Cordialement,\nL'equipe HighTech EDU"
        );
    }

    public void envoyerRefus(String emailEtudiant,
                             String prenomEtudiant,
                             String titreCours) {
        sendViaApi(
                emailEtudiant,
                "Suite a votre candidature - HighTech EDU",
                "Bonjour " + prenomEtudiant + ",\n\n" +
                        "Apres examen de votre dossier, nous ne sommes malheureusement pas en mesure " +
                        "de donner une suite favorable a votre candidature pour le cours :\n\n" +
                        "  " + titreCours + "\n\n" +
                        "Nous vous encourageons a vous presenter a la prochaine session.\n\n" +
                        "Cordialement,\nL'equipe HighTech EDU"
        );
    }
}