package hightech.edu.Inscription.model;

public enum InscriptionStatut {
    EN_ATTENTE("En Attente"),
    VALIDE("Validé"),
    ANNULE("Annulé");

    private final String label;

    InscriptionStatut(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
