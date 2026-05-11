package hightech.edu.Inscription.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "inscriptions")
@Getter @Setter @NoArgsConstructor
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @Column(name = "date_inscription", nullable = false)
    private LocalDate dateInscription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'EN_ATTENTE'")
    private InscriptionStatut statut = InscriptionStatut.EN_ATTENTE;

    // ── Formulaire public ───────────────────────────────────
    @Column(name = "souhait_inscription", length = 100)
    private String souhaitInscription;

    @Column(length = 150)
    private String ecole;

    @Column(name = "mon_bac", length = 100)
    private String monBac;

    @Column(name = "annee_bac", length = 20)
    private String anneeBac;

    @Column(name = "double_diplomation", length = 50)
    private String doubleDiplomation;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    // ── Paiement ────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", length = 30)
    private ModePaiement modePaiement;

    @Column(name = "montant")
    private Double montant;

    @Column(name = "paiement_effectue")
    private Boolean paiementEffectue = false;

    @Column(name = "date_paiement")
    private LocalDate datePaiement;

    @Column(name = "reference_paiement", length = 100)
    private String referencePaiement;

    // Safe getter
    public InscriptionStatut getStatut() {
        return statut != null ? statut : InscriptionStatut.EN_ATTENTE;
    }

    public Boolean getPaiementEffectue() {
        return paiementEffectue != null && paiementEffectue;
    }
}
