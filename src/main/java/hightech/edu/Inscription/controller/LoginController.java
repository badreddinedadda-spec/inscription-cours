package hightech.edu.Inscription.controller;

import hightech.edu.Inscription.config.SecurityConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    /**
     * URL secrète admin : /portail-admin-ht2025
     * → redirige vers /login (géré ensuite par Spring Security).
     * C'est le SEUL point d'entrée visible pour accéder au login.
     */
    @GetMapping(SecurityConfig.ADMIN_ENTRY_PATH)
    public String adminEntry() {
        return "redirect:/login";
    }

    /**
     * /login — formulaire de connexion.
     *
     * Accessible UNIQUEMENT via :
     *   1. La redirection depuis /portail-admin-ht2025
     *   2. Les redirects internes de Spring Security (échec auth, accès protégé)
     *   3. Les liens dans /forgot-password et /reset-password
     *
     * Si quelqu'un tape /login directement dans le navigateur :
     *   → Spring Security autorise le GET (loginPage) mais on peut
     *     choisir de rediriger vers "/" pour masquer l'existence du login.
     *   → Voir le commentaire OPTION A / OPTION B ci-dessous.
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String reset,
            Model model) {

        // ── OPTION A (actuelle) : affiche le formulaire normalement ──────
        // Spring Security a besoin de /login pour ses redirects internes.
        // Si on veut quand même que /login direct "fonctionne" mais soit
        // discret, on le laisse afficher la page.
        // Un visiteur qui tape /login voit le formulaire — ce n'est pas grave
        // car sans les credentials il ne peut rien faire.

        // ── OPTION B : rediriger /login direct vers "/" ──────────────────
        // Décommenter les 3 lignes suivantes pour masquer complètement /login.
        // ATTENTION : cela casse les redirects de Spring Security après
        // un accès non autorisé (ex: aller sur /dashboard sans être connecté).
        // À n'utiliser que si vous acceptez ce compromis.
        //
        // if (error == null && logout == null && reset == null) {
        //     return "redirect:/";
        // }

        if (error  != null) model.addAttribute("error",  "Identifiants incorrects.");
        if (logout != null) model.addAttribute("logout", "Vous avez été déconnecté.");
        if (reset  != null) model.addAttribute("reset",  "Mot de passe réinitialisé. Connectez-vous.");

        return "login";
    }
}