package hightech.edu.Inscription.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * LoginController
 *
 * Gère deux routes :
 *
 *  1. GET /portail-admin-ht2025
 *     → URL secrète d'entrée pour l'administrateur.
 *        Non exposée sur la page publique (landing.html).
 *        Redirige vers /login (formulaire de connexion).
 *        Protection anti-bot : sans connaître cette URL,
 *        personne ne sait qu'un espace admin existe.
 *
 *  2. GET /login
 *     → Affiche le formulaire de connexion standard.
 *        Si l'utilisateur est déjà authentifié, il est
 *        redirigé vers /dashboard.
 */
@Controller
public class LoginController {

    /**
     * URL secrète d'entrée admin.
     * L'administrateur tape cette adresse directement dans son navigateur.
     */
    @GetMapping("/portail-admin-ht2025")
    public String adminEntryPoint() {
        return "redirect:/login";
    }

    /**
     * Page de connexion standard (formulaire email + mot de passe).
     * Spring Security intercepte le POST automatiquement.
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
