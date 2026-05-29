package hightech.edu.Inscription.controller;

import hightech.edu.Inscription.model.AdminUser;
import hightech.edu.Inscription.model.Inscription;
import hightech.edu.Inscription.model.InscriptionStatut;
import hightech.edu.Inscription.model.ModePaiement;
import hightech.edu.Inscription.service.AdminUserService;
import hightech.edu.Inscription.service.CoursService;
import hightech.edu.Inscription.service.EtudiantService;
import hightech.edu.Inscription.service.InscriptionService;
import hightech.edu.Inscription.service.SiteContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final EtudiantService    etudiantService;
    private final CoursService       coursService;
    private final InscriptionService inscriptionService;
    private final AdminUserService   adminUserService;
    private final PasswordEncoder    passwordEncoder;
    private final SiteContentService siteContentService;

    private AdminUser getAdmin(Authentication auth) {
        return auth != null ? adminUserService.findByEmail(auth.getName()) : null;
    }

    /* ─── Landing (public) ─── */
    @GetMapping("/")
    public String landing(Model model) {
        long inscriptionsValidees = inscriptionService.countByStatut(InscriptionStatut.VALIDE);
        model.addAttribute("totalEtudiants",    inscriptionsValidees);
        model.addAttribute("totalEtudiants",   etudiantService.count());
        model.addAttribute("totalCours",       coursService.count());
        model.addAttribute("totalInscriptions",inscriptionService.count());
        model.addAttribute("totalInscriptions",  inscriptionsValidees);
        model.addAttribute("coursList",        coursService.findAll());
        model.addAttribute("cms",              siteContentService.asMap());
        return "landing";
    }

    /* ─── Dashboard (admin) ─── */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("adminUser",            getAdmin(auth));
        model.addAttribute("totalCours",           coursService.count());
        model.addAttribute("demandesEnAttente",    inscriptionService.countByStatut(InscriptionStatut.EN_ATTENTE));
        model.addAttribute("inscriptionsValidees", inscriptionService.countByStatut(InscriptionStatut.VALIDE));
        model.addAttribute("totalEtudiants",       etudiantService.count());
        model.addAttribute("recentInscriptions",   inscriptionService.findRecent());
        model.addAttribute("recentCours",          coursService.findRecent());
        return "index";
    }

    /* ─── Paiements ─── */
    @GetMapping("/paiements")
    public String paiements(Model model, Authentication auth) {
        model.addAttribute("adminUser", getAdmin(auth));
        List<Inscription> validees = inscriptionService.findByStatut(InscriptionStatut.VALIDE);
        long payees   = validees.stream().filter(i -> Boolean.TRUE.equals(i.getPaiementEffectue())).count();
        long nonPayes = validees.size() - payees;
        model.addAttribute("inscriptionsValidees", validees);
        model.addAttribute("paiementsEffectues",   payees);
        model.addAttribute("nonPayes",             nonPayes);
        model.addAttribute("demandesEnAttente",    inscriptionService.countByStatut(InscriptionStatut.EN_ATTENTE));
        return "paiements";
    }

    @PostMapping("/paiements/enregistrer")
    public String enregistrerPaiement(
            @RequestParam Long inscriptionId,
            @RequestParam(required = false) String modePaiement,
            @RequestParam(required = false) Double montant,
            @RequestParam(required = false) String referencePaiement,
            RedirectAttributes redirectAttributes) {

        inscriptionService.findById(inscriptionId).ifPresent(insc -> {
            if (modePaiement != null && !modePaiement.isBlank()) {
                insc.setModePaiement(ModePaiement.valueOf(modePaiement));
                insc.setPaiementEffectue(true);
                insc.setDatePaiement(LocalDate.now());
            }
            if (montant != null) insc.setMontant(montant);
            if (referencePaiement != null && !referencePaiement.isBlank())
                insc.setReferencePaiement(referencePaiement);
            inscriptionService.save(insc);
        });

        redirectAttributes.addFlashAttribute("success", "Paiement enregistré avec succès.");
        return "redirect:/paiements";
    }

    /* ─── Paramètres ─── */
    @GetMapping("/parametres")
    public String parametres(Model model, Authentication auth) {
        model.addAttribute("adminUser", getAdmin(auth));
        return "parametres";
    }

    /**
     * Mise à jour du profil administrateur.
     * Accessible via POST /parametres/modifier.
     * Le mot de passe n'est modifié que si le champ newPassword est rempli.
     */
    @PostMapping("/parametres/modifier")
    public String modifierParametres(
            @RequestParam String prenom,
            @RequestParam String nom,
            @RequestParam String email,
            @RequestParam(required = false) String poste,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        AdminUser admin = getAdmin(auth);
        if (admin == null) {
            redirectAttributes.addFlashAttribute("error", "Session expirée, veuillez vous reconnecter.");
            return "redirect:/login";
        }

        // Mise à jour des champs de base
        admin.setPrenom(prenom.trim());
        admin.setNom(nom.trim());
        admin.setEmail(email.trim());
        if (poste != null) admin.setPoste(poste.trim());

        // Mise à jour du mot de passe (optionnel)
        if (newPassword != null && !newPassword.isBlank()) {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas.");
                return "redirect:/parametres";
            }
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Le mot de passe doit contenir au moins 6 caractères.");
                return "redirect:/parametres";
            }
            admin.setPassword(passwordEncoder.encode(newPassword));
        }

        adminUserService.save(admin);
        redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès.");
        return "redirect:/parametres";
    }

    /* ─── Mini-CMS : éditeur de la page d'accueil ─── */
    @GetMapping("/cms")
    public String cmsEditor(Model model, Authentication auth) {
        model.addAttribute("adminUser",  getAdmin(auth));
        model.addAttribute("contenus",   siteContentService.findAll());
        model.addAttribute("demandesEnAttente", inscriptionService.countByStatut(InscriptionStatut.EN_ATTENTE));
        return "cms";
    }

    @PostMapping("/cms/sauvegarder")
    public String cmsSauvegarder(
            @RequestParam java.util.Map<String, String> params,
            RedirectAttributes redirectAttributes) {
        // On retire le token CSRF qui passe aussi dans la map
        params.remove("_csrf");
        siteContentService.sauvegarderTout(params);
        redirectAttributes.addFlashAttribute("success", "Page d'accueil mise à jour avec succès !");
        return "redirect:/cms";
    }
}
