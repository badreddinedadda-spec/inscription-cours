package hightech.edu.Inscription.model;

import jakarta.persistence.*;

@Entity
@Table(name = "site_content")
public class SiteContent {

    @Id
    @Column(name = "cle", length = 100)
    private String cle;

    @Column(name = "valeur", columnDefinition = "TEXT", nullable = false)
    private String valeur;

    @Column(name = "label_admin", length = 200)
    private String labelAdmin;  // Nom lisible pour l'admin ex: "Titre héro"

    public SiteContent() {}

    public SiteContent(String cle, String valeur, String labelAdmin) {
        this.cle = cle;
        this.valeur = valeur;
        this.labelAdmin = labelAdmin;
    }

    public String getCle()               { return cle; }
    public void   setCle(String cle)     { this.cle = cle; }

    public String getValeur()                { return valeur; }
    public void   setValeur(String valeur)   { this.valeur = valeur; }

    public String getLabelAdmin()                    { return labelAdmin; }
    public void   setLabelAdmin(String labelAdmin)   { this.labelAdmin = labelAdmin; }
}
