package hightech.edu.Inscription.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "cours")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "inscriptions")
@EqualsAndHashCode(exclude = "inscriptions")
public class Cours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 2, max = 200)
    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String objectifs;

    @Column(columnDefinition = "TEXT")
    private String prerequis;

    @Column(columnDefinition = "TEXT")
    private String programme;

    private String niveau;

    @Min(value = 1)
    @Column(nullable = false)
    private int duree;

    @NotBlank(message = "L'enseignant est obligatoire")
    @Column(nullable = false)
    private String enseignant;

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inscription> inscriptions;
}