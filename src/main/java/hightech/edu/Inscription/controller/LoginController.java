package hightech.edu.Inscription.controller;

import hightech.edu.Inscription.config.SecurityConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    /**
     * URL secrète : /portail-admin-ht2025
     * Seul vrai point d'entrée vers le login.
     */
    @GetMapping(SecurityConfig.ADMIN_ENTRY_PATH)
    public String adminEntry() {
        return "redirect:/login";
    }

    /**
     * /login — si accès direct sans paramètre → renvoie à l'accueil.
     * Seuls les redirects légitimes (error, logout, reset) affichent le formulaire.
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String reset,
            HttpServletRequest request,
            Model model) {

        // Accès direct sans paramètre → cacher le login aux visiteurs
        if (error == null && logout == null && reset == null) {
            String referer = request.getHeader("Referer");
            boolean fromSite = referer != null && referer.contains(
                    request.getServerName()
            );
            if (!fromSite) {
                return "redirect:/";
            }
        }

        if (error  != null) model.addAttribute("error",  "Identifiants incorrects.");
        if (logout != null) model.addAttribute("logout", "Vous avez été déconnecté.");
        if (reset  != null) model.addAttribute("reset",  "Mot de passe réinitialisé.");

        return "login";
    }
}