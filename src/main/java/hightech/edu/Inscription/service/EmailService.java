package hightech.edu.Inscription.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    //Envoie un email d'acceptation à l'étudiant.

    public void envoyerAcceptation(String emailEtudiant, String prenomEtudiant, String titreCours) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(emailEtudiant);
            msg.setSubject("Votre inscription est validée — HighTech EDU");
            msg.setText(
                "Bonjour " + prenomEtudiant + ",\n\n" +
                "Nous avons le plaisir de vous informer que votre inscription au cours :\n\n" +
                "   « " + titreCours + " »\n\n" +
                "a été validée par notre équipe pédagogique.\n\n" +
                "Bienvenue chez HighTech EDU ! Vous recevrez prochainement les détails " +
                "concernant le démarrage du cours.\n\n" +
                "Cordialement,\n" +
                "L'équipe HighTech EDU"
            );
            mailSender.send(msg);
        } catch (Exception e) {
            // Email non bloquant "si SMTP non configuré, on log simplement"
            System.err.println("[EmailService] Impossible d'envoyer l'email d'acceptation à "
                + emailEtudiant + " : " + e.getMessage());
        }
    }

    //Envoie un email de refus à l'étudiant

    public void envoyerRefus(String emailEtudiant, String prenomEtudiant, String titreCours) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(emailEtudiant);
            msg.setSubject("Suite à votre candidature — HighTech EDU");
            msg.setText(
                "Bonjour " + prenomEtudiant + ",\n\n" +
                "Après examen de votre dossier, nous ne sommes malheureusement pas en mesure " +
                "de donner une suite favorable à votre candidature pour le cours :\n\n" +
                "   « " + titreCours + " »\n\n" +
                "Nous vous encourageons à vous présenter à la prochaine session d'admissions.\n\n" +
                "Cordialement,\n" +
                "L'équipe HighTech EDU"
            );
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("[EmailService] Impossible d'envoyer l'email de refus à "
                + emailEtudiant + " : " + e.getMessage());
        }
    }
}
