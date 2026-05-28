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

    // ─── Admin : liste complète ────────────────────────────────────────────
    @GetMapping("/inscriptions")
    public String liste(Model model) {
        model.addAttribute("inscriptions", inscriptionService.findAll());
        model.addAttribute("enAttente",    inscriptionService.findByStatut(InscriptionStatut.EN_ATTENTE));
        model.addAttribute("statuts",      InscriptionStatut.values());
        return "inscriptions/liste";
    }

    // ─── Admin : demandes en attente ───────────────────────────────────────
    @GetMapping("/inscriptions/en-attente")
    public String enAttente(Model model) {
        model.addAttribute("inscriptions", inscriptionService.findByStatut(InscriptionStatut.EN_ATTENTE));
        model.addAttribute("statuts",      InscriptionStatut.values());
        model.addAttribute("filterLabel",  "Demandes en attente");
        return "inscriptions/liste";
    }

    // ─── Admin : formulaire ────────────────────────────────────────────────
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

        // ── Validation basique ───────────────────────────────────────────────
        if (nom == null || nom.isBlank() || prenom == null || prenom.isBlank()
                || email == null || email.isBlank() || coursId == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Veuillez remplir tous les champs obligatoires et sélectionner un programme.");
            return "redirect:/s-inscrire";
        }

        // Créer ou retrouver l'étudiant
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

        // Mettre à jour les infos de contact si étudiant existant
        if (telephone != null) etudiant.setTelephone(telephone);
        if (whatsapp  != null) etudiant.setWhatsapp(whatsapp);
        if (pays      != null) etudiant.setPays(pays);
        if (ville     != null) etudiant.setVille(ville);
        etudiantService.save(etudiant);

        // Vérifier doublon
        if (inscriptionService.existsByEtudiantIdAndCoursId(etudiant.getId(), coursId)) {
            redirectAttributes.addFlashAttribute("error", "Vous avez déjà une demande pour ce cours.");
            return "redirect:/";
        }

        // Créer demande EN_ATTENTE
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
         // cette ligne sera supprimée car on utilise saved maintenant
        // Redirect to landing page which shows the success modal
        Inscription saved = inscriptionService.save(inscription);
        return "redirect:/confirmation/" + saved.getId();
        // ─── PUBLIC : page de confirmation après inscription ───────────────
        @GetMapping("/confirmation/{id}")
        public String confirmation(@PathVariable Long id, Model model) {
            Inscription inscription = inscriptionService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Inscription introuvable"));
            model.addAttribute("inscription", inscription);
            return "inscriptions/confirmation";
        }

// ─── PUBLIC : télécharger le reçu PDF ──────────────────────────────
        @GetMapping("/confirmation/{id}/pdf")
        public void telechargerPdf(@PathVariable Long id,
                jakarta.servlet.http.HttpServletResponse response) throws Exception {
            Inscription inscription = inscriptionService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Inscription introuvable"));

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=recu-inscription-" + id + ".pdf");

            // Génération PDF avec iText
            com.itextpdf.kernel.pdf.PdfWriter writer =
                    new com.itextpdf.kernel.pdf.PdfWriter(response.getOutputStream());
            com.itextpdf.kernel.pdf.PdfDocument pdf =
                    new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document =
                    new com.itextpdf.layout.Document(pdf);

            // Titre
            document.add(new com.itextpdf.layout.element.Paragraph("HIGHTECH EDU")
                    .setFontSize(20).setBold()
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

            document.add(new com.itextpdf.layout.element.Paragraph("Reçu de candidature")
                    .setFontSize(14)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

            document.add(new com.itextpdf.layout.element.Paragraph(" "));

            // Numéro dossier
            document.add(new com.itextpdf.layout.element.Paragraph(
                    "N° Dossier : HTEC-" + String.format("%04d", id))
                    .setFontSize(12).setBold());

            document.add(new com.itextpdf.layout.element.Paragraph(
                    "Date : " + inscription.getDateInscription())
                    .setFontSize(11));

            document.add(new com.itextpdf.layout.element.Paragraph(" "));

            // Infos étudiant
            document.add(new com.itextpdf.layout.element.Paragraph("INFORMATIONS ÉTUDIANT")
                    .setFontSize(12).setBold());

            document.add(new com.itextpdf.layout.element.Paragraph(
                    "Nom complet : " + inscription.getEtudiant().getPrenom()
                            + " " + inscription.getEtudiant().getNom())
                    .setFontSize(11));

            document.add(new com.itextpdf.layout.element.Paragraph(
                    "Email : " + inscription.getEtudiant().getEmail())
                    .setFontSize(11));

            document.add(new com.itextpdf.layout.element.Paragraph(" "));

            // Infos cours
            document.add(new com.itextpdf.layout.element.Paragraph("PROGRAMME CHOISI")
                    .setFontSize(12).setBold());

            document.add(new com.itextpdf.layout.element.Paragraph(
                    "Cours : " + inscription.getCours().getTitre())
                    .setFontSize(11));

            document.add(new com.itextpdf.layout.element.Paragraph(
                    "Enseignant : " + inscription.getCours().getEnseignant())
                    .setFontSize(11));

            document.add(new com.itextpdf.layout.element.Paragraph(
                    "Durée : " + inscription.getCours().getDuree() + " heures")
                    .setFontSize(11));

            document.add(new com.itextpdf.layout.element.Paragraph(" "));

            // Statut
            document.add(new com.itextpdf.layout.element.Paragraph(
                    "Statut : EN ATTENTE DE VALIDATION")
                    .setFontSize(12).setBold());

            document.add(new com.itextpdf.layout.element.Paragraph(" "));

            document.add(new com.itextpdf.layout.element.Paragraph(
                    "Notre équipe vous contactera sous 48h.")
                    .setFontSize(11));

            document.close();
        }
    }
}
