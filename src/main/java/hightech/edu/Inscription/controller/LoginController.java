package hightech.edu.Inscription.controller;

import hightech.edu.Inscription.config.SecurityConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    /**
     * URL secrète : /portail-admin-ht2025
     * Pose un flag en session puis redirige vers /login.
     */
    @GetMapping(SecurityConfig.ADMIN_ENTRY_PATH)
    public String adminEntry(HttpSession session) {
        // Flag en session : indique que la visite vient de l'URL secrète
        session.setAttribute("fromAdminEntry", true);
        return "redirect:/login";
    }

    /**
     * /login
     * - Accessible si : flag session présent, ou paramètre error/logout/reset
     * - Sinon : redirect vers accueil (cache le login aux visiteurs)
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String reset,
            HttpSession session,
            Model model) {

        boolean hasFlag   = Boolean.TRUE.equals(session.getAttribute("fromAdminEntry"));
        boolean hasParam  = error != null || logout != null || reset != null;

        if (!hasFlag && !hasParam) {
            // Accès direct sans autorisation → renvoyer à l'accueil
            return "redirect:/";
        }

        // Consommer le flag (usage unique)
        session.removeAttribute("fromAdminEntry");

        if (error  != null) model.addAttribute("error",  "Identifiants incorrects.");
        if (logout != null) model.addAttribute("logout", "Vous avez été déconnecté.");
        if (reset  != null) model.addAttribute("reset",  "Mot de passe réinitialisé.");

        return "login";
    }
}