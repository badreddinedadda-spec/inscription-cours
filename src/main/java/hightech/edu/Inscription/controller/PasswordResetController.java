package hightech.edu.Inscription.controller;

import hightech.edu.Inscription.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    //A."forgot password" form i USED MAILTrap for it
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    // B.Submit username, send email
    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(
            @RequestParam String email,
            RedirectAttributes ra) {
        boolean sent = passwordResetService.sendResetEmail(email);
        if (sent) {
            ra.addFlashAttribute("success",
                "Un lien de réinitialisation a été envoyé. Vérifiez votre boîte mail.");
        } else {
            ra.addFlashAttribute("error",
                "Aucun compte trouvé pour cet identifiant.");
        }
        return "redirect:/forgot-password";
    }

    // C.Show new password form (token in URL)
    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        boolean valid = passwordResetService.validateToken(token).isPresent();
        model.addAttribute("token", token);
        model.addAttribute("valid", valid);
        return "reset-password";
    }

    // D.Submit new password
    @PostMapping("/reset-password")
    public String resetPasswordSubmit(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes ra) {

        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Les mots de passe ne correspondent pas.");
            return "redirect:/reset-password?token=" + token;
        }
        if (password.length() < 6) {
            ra.addFlashAttribute("error", "Le mot de passe doit contenir au moins 6 caractères.");
            return "redirect:/reset-password?token=" + token;
        }

        boolean ok = passwordResetService.resetPassword(token, password);
        if (ok) {
            ra.addFlashAttribute("success",
                "Mot de passe mis à jour. Vous pouvez maintenant vous connecter.");
            return "redirect:/login";
        } else {
            ra.addFlashAttribute("error", "Lien invalide ou expiré.");
            return "redirect:/forgot-password";
        }
    }
}
