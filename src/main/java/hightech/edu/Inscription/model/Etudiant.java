package hightech.edu.Inscription.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "etudiants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "inscriptions")
@EqualsAndHashCode(exclude = "inscriptions")
public class Etudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Id
    @Column(length = 10)
    private String civilite;           // M. / Mme

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100)
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100)
    @Column(nullable = false)
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email n'est pas valide")
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    //Contact
    @Column(length = 30)
    private String telephone;

    @Column(length = 30)
    private String whatsapp;

    @Column(length = 100)
    private String pays;

    @Column(length = 100)
    private String ville;

    //Relation
    @OneToMany(mappedBy = "etudiant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inscription> inscriptions;
}
