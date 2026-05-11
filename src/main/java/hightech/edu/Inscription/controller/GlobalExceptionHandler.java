package hightech.edu.Inscription.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private boolean isAdminRequest(HttpServletRequest request, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        String uri = request.getRequestURI();
        return uri.startsWith("/dashboard") || uri.startsWith("/inscriptions")
                || uri.startsWith("/cours") || uri.startsWith("/etudiants")
                || uri.startsWith("/paiements") || uri.startsWith("/parametres");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(RuntimeException ex, Model model,
                                 HttpServletRequest request, Authentication auth) {
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("isAdmin", isAdminRequest(request, auth));
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model,
                                HttpServletRequest request, Authentication auth) {
        model.addAttribute("message", "Une erreur inattendue s'est produite. Veuillez réessayer.");
        model.addAttribute("isAdmin", isAdminRequest(request, auth));
        return "error/500";
    }
}
