package hightech.edu.Inscription.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    @Value("${mailtrap.api.token}")
    private String mailtrapToken;

    private void sendViaApi(String toEmail, String subject, String text) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Api-Token", mailtrapToken);

            String body = """
                {
                  "from": {"email": "noreply@hightech.edu", "name": "HighTech EDU"},
                  "to": [{"email": "%s"}],
                  "subject": "%s",
                  "text": "%s"
                }
                """.formatted(toEmail, subject, text.replace("\n", "\\n").replace("\"", "\\\""));

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity("https://send.api.mailtrap.io/api/send", request, String.class);
            System.out.println("[EmailService] Email envoyé à " + toEmail);

        } catch (Exception e) {
            System.err.println("[EmailService] Erreur envoi email à " + toEmail + " : " + e.getMessage());
        }
    }

    public void envoyerAcceptation(String emailEtudiant, String prenomEtudiant, String titreCours) {
        sendViaApi(
                emailEtudiant,
                "Votre inscription est validée — HighTech EDU",
                "Bonjour " + prenomEtudiant + ",\n\n" +
                        "Nous avons le plaisir de vous informer que votre inscription au cours :\n\n" +
                        "« " + titreCours + " »\n\n" +
                        "a été validée par notre équipe pédagogique.\n\n" +
                        "Bienvenue chez HighTech EDU !\n\n" +
                        "Cordialement,\nL'équipe HighTech EDU"
        );
    }

    public void envoyerRefus(String emailEtudiant, String prenomEtudiant, String titreCours) {
        sendViaApi(
                emailEtudiant,
                "Suite à votre candidature — HighTech EDU",
                "Bonjour " + prenomEtudiant + ",\n\n" +
                        "Après examen de votre dossier, nous ne sommes malheureusement pas en mesure " +
                        "de donner une suite favorable à votre candidature pour le cours :\n\n" +
                        "« " + titreCours + " »\n\n" +
                        "Nous vous encourageons à vous présenter à la prochaine session.\n\n" +
                        "Cordialement,\nL'équipe HighTech EDU"
        );
    }
}