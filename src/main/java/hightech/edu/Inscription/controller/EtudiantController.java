package hightech.edu.Inscription.controller;

import hightech.edu.Inscription.model.Etudiant;
import hightech.edu.Inscription.service.EtudiantService;
import hightech.edu.Inscription.service.InscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/etudiants")
@RequiredArgsConstructor
public class EtudiantController {

    private final EtudiantService etudiantService;
    private final InscriptionService inscriptionService;

    @GetMapping
    public String liste(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        Page<Etudiant> pageResult = etudiantService.findPaginated(search, page, size);
        model.addAttribute("etudiants", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("search", search);
        return "etudiants/liste";
    }

    @GetMapping("/{id}/cours")
    public String coursEtudiant(@PathVariable Long id, Model model) {
        Etudiant etudiant = etudiantService.findById(id)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));
        model.addAttribute("etudiant", etudiant);
        model.addAttribute("inscriptions", inscriptionService.findByEtudiantId(id));
        return "etudiants/cours-etudiant";
    }

    @GetMapping("/nouveau")
    public String formulaireAjout(Model model) {
        model.addAttribute("etudiant", new Etudiant());
        return "etudiants/formulaire";
    }

    @GetMapping("/modifier/{id}")
    public String formulaireModification(@PathVariable Long id, Model model) {
        Etudiant etudiant = etudiantService.findById(id)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));
        model.addAttribute("etudiant", etudiant);
        return "etudiants/formulaire";
    }

    @PostMapping("/sauvegarder")
    public String sauvegarder(@Valid @ModelAttribute Etudiant etudiant,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "etudiants/formulaire";
        }
        etudiantService.save(etudiant);
        redirectAttributes.addFlashAttribute("success", "Étudiant sauvegardé avec succès !");
        return "redirect:/etudiants";
    }

    @GetMapping("/supprimer/{id}")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        etudiantService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Étudiant supprimé avec succès !");
        return "redirect:/etudiants";
    }
}
