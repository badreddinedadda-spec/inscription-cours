package hightech.edu.Inscription.model;

public enum ModePaiement {
    ESPECES("Espèces"),
    CARTE_BANCAIRE("Carte bancaire"),
    VIREMENT("Virement bancaire"),
    CHEQUE("Chèque");

    private final String label;
    ModePaiement(String label) { this.label = label; }
    public String getLabel() { return label; }
}
