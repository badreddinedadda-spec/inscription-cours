package hightech.edu.Inscription.service;

import hightech.edu.Inscription.model.SiteContent;
import hightech.edu.Inscription.repository.SiteContentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SiteContentService {

    private final SiteContentRepository repo;

    //Valeurs par défaut — insérées au 1er démarrage seulement
    private static final Map<String, String[]> DEFAULTS = new LinkedHashMap<>();
    static {
        // cle -> [valeur, labelAdmin]
        DEFAULTS.put("logo_nom",            new String[]{"HighTech EDU",            "Logo — Nom principal"});
        DEFAULTS.put("logo_sous_titre",     new String[]{"École d'ingénierie",       "Logo — Sous-titre"});
        DEFAULTS.put("hero_eyebrow",        new String[]{"Admissions ouvertes — 2025", "Héro — Bandeau texte"});
        DEFAULTS.put("hero_titre_ligne1",   new String[]{"Formez-vous pour",         "Héro — Titre ligne 1"});
        DEFAULTS.put("hero_titre_italic",   new String[]{"l'ingénierie",             "Héro — Titre mot en italique"});
        DEFAULTS.put("hero_titre_ligne3",   new String[]{"de demain.",               "Héro — Titre ligne 3"});
        DEFAULTS.put("hero_description",    new String[]{"HighTech EDU accompagne les étudiants vers des carrières techniques et managériales, avec des programmes reconnus par l'industrie.", "Héro — Paragraphe de description"});
        DEFAULTS.put("hero_cta_principal",  new String[]{"Déposer une candidature",  "Héro — Bouton principal"});
        DEFAULTS.put("hero_cta_lien",       new String[]{"Voir les programmes",      "Héro — Lien secondaire"});
        DEFAULTS.put("programmes_eyebrow",  new String[]{"Nos formations",           "Section Programmes — Petit titre"});
        DEFAULTS.put("programmes_titre",    new String[]{"Des programmes taillés pour l'industrie", "Section Programmes — Grand titre"});
        DEFAULTS.put("programmes_sous_titre", new String[]{"Chaque cursus est co-construit avec des entreprises partenaires pour garantir une insertion rapide.", "Section Programmes — Sous-titre"});
        DEFAULTS.put("engagements_eyebrow", new String[]{"Nos engagements",          "Section Engagements — Petit titre"});
        DEFAULTS.put("engagements_titre",   new String[]{"Pourquoi choisir HighTech EDU ?", "Section Engagements — Grand titre"});
        DEFAULTS.put("engagement_1_titre",  new String[]{"Formateurs praticiens",    "Engagement 1 — Titre"});
        DEFAULTS.put("engagement_1_texte",  new String[]{"Nos intervenants exercent en entreprise. Vous apprenez des méthodes utilisées aujourd'hui sur le terrain.", "Engagement 1 — Texte"});
        DEFAULTS.put("engagement_2_titre",  new String[]{"Projets industriels réels","Engagement 2 — Titre"});
        DEFAULTS.put("engagement_2_texte",  new String[]{"Chaque semestre est rythmé par des projets avec de vraies contraintes et des livrables concrets.", "Engagement 2 — Texte"});
        DEFAULTS.put("engagement_3_titre",  new String[]{"Suivi personnalisé",       "Engagement 3 — Titre"});
        DEFAULTS.put("engagement_3_texte",  new String[]{"Un conseiller pédagogique vous suit tout au long de votre parcours.", "Engagement 3 — Texte"});
        DEFAULTS.put("engagement_4_titre",  new String[]{"Réseau alumni actif",      "Engagement 4 — Titre"});
        DEFAULTS.put("engagement_4_texte",  new String[]{"Plus de 500 diplômés placés dans des entreprises nationales et internationales.", "Engagement 4 — Texte"});
        DEFAULTS.put("footer_email",        new String[]{"contact@hightech-edu.ma",  "Footer — Email de contact"});
        DEFAULTS.put("footer_copyright",    new String[]{"© 2025 — Tous droits réservés", "Footer — Texte copyright"});
    }

    // Insère les valeurs par défaut si la table est vide
    @PostConstruct
    public void initialiserDefauts() {
        if (repo.count() == 0) {
            DEFAULTS.forEach((cle, val) ->
                repo.save(new SiteContent(cle, val[0], val[1]))
            );
        }
    }

    /** Retourne toutes les entrées (triées par clé) */
    public List<SiteContent> findAll() {
        return repo.findAll();
    }

    //Retourne une map cle→valeur pour injection dans le modèle Thymeleaf
    public Map<String, String> asMap() {
        Map<String, String> map = new LinkedHashMap<>();
        repo.findAll().forEach(sc -> map.put(sc.getCle(), sc.getValeur()));
        return map;
    }

    // Sauvegarde une liste de modifications (venant du formulaire admin)
    public void sauvegarderTout(Map<String, String> valeurs) {
        valeurs.forEach((cle, valeur) -> {
            repo.findById(cle).ifPresent(sc -> {
                sc.setValeur(valeur != null ? valeur.trim() : "");
                repo.save(sc);
            });
        });
    }
}
