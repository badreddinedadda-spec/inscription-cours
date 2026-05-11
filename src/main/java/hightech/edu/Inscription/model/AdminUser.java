package hightech.edu.Inscription.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin_users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String role = "ADMIN";

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Column(length = 100)
    private String poste;

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public String getInitiales() {
        return (prenom != null && !prenom.isEmpty() ? String.valueOf(prenom.charAt(0)) : "")
             + (nom    != null && !nom.isEmpty()    ? String.valueOf(nom.charAt(0))    : "");
    }
}
