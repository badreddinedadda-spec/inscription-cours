package hightech.edu.Inscription.controller;

import hightech.edu.Inscription.model.Etudiant;
import hightech.edu.Inscription.model.Inscription;
import hightech.edu.Inscription.model.InscriptionStatut;
import hightech.edu.Inscription.service.AdminUserService;
import hightech.edu.Inscription.service.CoursService;
import hightech.edu.Inscription.service.EmailService;
import hightech.edu.Inscription.service.EtudiantService;
import hightech.edu.Inscription.service.InscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class InscriptionController {

    private final EmailService emailService;
    private final AdminUserService adminUserService;
    private final InscriptionService inscriptionService;
    private final EtudiantService etudiantService;
    private final CoursService coursService;

    //Admin_liste_complète
    @GetMapping("/inscriptions")
    public String liste(Model model) {
        model.addAttribute("inscriptions", inscriptionService.findAll());
        model.addAttribute("enAttente",    inscriptionService.findByStatut(InscriptionStatut.EN_ATTENTE));
        model.addAttribute("statuts",      InscriptionStatut.values());
        return "inscriptions/liste";
    }

    //Admin_demandes_en_attente
    @GetMapping("/inscriptions/en-attente")
    public String enAttente(Model model) {
        model.addAttribute("inscriptions", inscriptionService.findByStatut(InscriptionStatut.EN_ATTENTE));
        model.addAttribute("statuts",      InscriptionStatut.values());
        model.addAttribute("filterLabel",  "Demandes en attente");
        return "inscriptions/liste";
    }

    //Admin_formulaire
    @GetMapping("/inscriptions/nouveau")
    public String formulaireAjout(Model model, Authentication auth) {
        model.addAttribute("inscription", new Inscription());
        model.addAttribute("etudiants",   etudiantService.findAll());
        model.addAttribute("coursList",   coursService.findAll());
        model.addAttribute("statuts",     InscriptionStatut.values());
        model.addAttribute("adminUser",   adminUserService.findByEmail(auth != null ? auth.getName() : ""));
        return "inscriptions/formulaire";
    }

    @GetMapping("/inscriptions/modifier/{id}")
    public String formulaireModification(@PathVariable Long id, Model model, Authentication auth) {
        Inscription inscription = inscriptionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription introuvable : " + id));
        model.addAttribute("inscription", inscription);
        model.addAttribute("etudiants",   etudiantService.findAll());
        model.addAttribute("coursList",   coursService.findAll());
        model.addAttribute("statuts",     InscriptionStatut.values());
        model.addAttribute("adminUser",   adminUserService.findByEmail(auth != null ? auth.getName() : ""));
        return "inscriptions/formulaire";
    }

    @PostMapping("/inscriptions/sauvegarder")
    public String sauvegarder(
            @RequestParam Long etudiantId,
            @RequestParam Long coursId,
            @RequestParam(required = false) Long inscriptionId,
            @RequestParam(required = false) String statut,
            RedirectAttributes redirectAttributes) {

        boolean isNew = (inscriptionId == null);
        if (isNew && inscriptionService.existsByEtudiantIdAndCoursId(etudiantId, coursId)) {
            redirectAttributes.addFlashAttribute("error", "⚠️ Cet étudiant est déjà inscrit à ce cours !");
            return "redirect:/inscriptions/nouveau";
        }

        Inscription inscription = (inscriptionId != null)
                ? inscriptionService.findById(inscriptionId).orElse(new Inscription())
                : new Inscription();

        inscription.setEtudiant(etudiantService.findById(etudiantId).orElseThrow());
        inscription.setCours(coursService.findById(coursId).orElseThrow());
        if (inscription.getDateInscription() == null) inscription.setDateInscription(LocalDate.now());
        if (statut != null && !statut.isBlank()) inscription.setStatut(InscriptionStatut.valueOf(statut));

        inscriptionService.save(inscription);
        redirectAttributes.addFlashAttribute("success", "Inscription enregistrée !");
        return "redirect:/inscriptions";
    }

    @GetMapping("/inscriptions/supprimer/{id}")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        inscriptionService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Inscription supprimée !");
        return "redirect:/inscriptions";
    }

    @GetMapping("/inscriptions/valider/{id}")
    public String valider(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        inscriptionService.findById(id).ifPresent(insc -> {
            inscriptionService.valider(id);
            emailService.envoyerAcceptation(
                insc.getEtudiant().getEmail(),
                insc.getEtudiant().getPrenom(),
                insc.getCours().getTitre()
            );
        });
        redirectAttributes.addFlashAttribute("success", "Inscription validée. Un email a été envoyé à l'étudiant.");
        return "redirect:/inscriptions";
    }

    @GetMapping("/inscriptions/annuler/{id}")
    public String annuler(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        inscriptionService.findById(id).ifPresent(insc -> {
            inscriptionService.annuler(id);
            emailService.envoyerRefus(
                insc.getEtudiant().getEmail(),
                insc.getEtudiant().getPrenom(),
                insc.getCours().getTitre()
            );
        });
        redirectAttributes.addFlashAttribute("success", "Inscription refusée. Un email a été envoyé à l'étudiant.");
        return "redirect:/inscriptions";
    }

    // ─── PUBLIC : formulaire d'auto-inscription HighTech ──────────────────
    @GetMapping("/s-inscrire")
    public String publicForm() {
        // Le formulaire d'inscription est intégré dans la landing page (modal).
        // Cette URL redirige vers l'accueil pour éviter une page orpheline.
        return "redirect:/";
    }

    @PostMapping("/s-inscrire")
    public String publicSubmit(
            // Identité
            @RequestParam(required = false) String civilite,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String email,
            // Contact
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) String whatsapp,
            @RequestParam(required = false) String pays,
            @RequestParam(required = false) String ville,
            // Inscription
            @RequestParam(required = false) Long coursId,
            @RequestParam(required = false) String souhaitInscription,
            @RequestParam(required = false) String ecole,
            @RequestParam(required = false) String monBac,
            @RequestParam(required = false) String anneeBac,
            @RequestParam(required = false) String doubleDiplomation,
            @RequestParam(required = false) String message,
            RedirectAttributes redirectAttributes) {

        //Validation_basique
        if (nom == null || nom.isBlank() || prenom == null || prenom.isBlank()
                || email == null || email.isBlank() || coursId == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Veuillez remplir tous les champs obligatoires et sélectionner un programme.");
            return "redirect:/s-inscrire";
        }

        // Créer_ou_retrouver_l'étudiant
        Etudiant etudiant = etudiantService.findByEmail(email).orElseGet(() -> {
            Etudiant e = new Etudiant();
            e.setCivilite(civilite);
            e.setNom(nom);
            e.setPrenom(prenom);
            e.setEmail(email);
            e.setTelephone(telephone);
            e.setWhatsapp(whatsapp);
            e.setPays(pays);
            e.setVille(ville);
            return etudiantService.save(e);
        });

        // Mettre_à_jour_les_infos_de_contact_si_étudiant existant
        if (telephone != null) etudiant.setTelephone(telephone);
        if (whatsapp  != null) etudiant.setWhatsapp(whatsapp);
        if (pays      != null) etudiant.setPays(pays);
        if (ville     != null) etudiant.setVille(ville);
        etudiantService.save(etudiant);

        // Vérifier_doublon
        if (inscriptionService.existsByEtudiantIdAndCoursId(etudiant.getId(), coursId)) {
            redirectAttributes.addFlashAttribute("error", "Vous avez déjà une demande pour ce cours.");
            return "redirect:/";
        }

        // Créer_demande_EN_ATTENTE
        Inscription inscription = new Inscription();
        inscription.setEtudiant(etudiant);
        inscription.setCours(coursService.findById(coursId).orElseThrow());
        inscription.setDateInscription(LocalDate.now());
        inscription.setStatut(InscriptionStatut.EN_ATTENTE);
        inscription.setSouhaitInscription(souhaitInscription);
        inscription.setEcole(ecole);
        inscription.setMonBac(monBac);
        inscription.setAnneeBac(anneeBac);
        inscription.setDoubleDiplomation(doubleDiplomation);
        inscription.setMessage(message);
        inscriptionService.save(inscription);

        // Redirect to accueil which shows the success modal (white/blue style)
        return "redirect:/?success=1";
    }
}
