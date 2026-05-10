package hightech.edu.Inscription.controller;

import hightech.edu.Inscription.model.Cours;
import hightech.edu.Inscription.service.CoursService;
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
@RequestMapping("/cours")
@RequiredArgsConstructor
public class CoursController {

    private final CoursService coursService;
    private final InscriptionService inscriptionService;

    @GetMapping
    public String liste(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        Page<Cours> pageResult = coursService.findPaginated(search, page, size);
        model.addAttribute("coursList", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("search", search);
        return "cours/liste";
    }

    @GetMapping("/{id}/etudiants")
    public String etudiantsCours(@PathVariable Long id, Model model) {
        Cours cours = coursService.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));
        model.addAttribute("cours", cours);
        model.addAttribute("inscriptions", inscriptionService.findByCoursId(id));
        return "cours/etudiants-cours";
    }

    @GetMapping("/nouveau")
    public String formulaireAjout(Model model) {
        model.addAttribute("cours", new Cours());
        return "cours/formulaire";
    }

    @GetMapping("/modifier/{id}")
    public String formulaireModification(@PathVariable Long id, Model model) {
        Cours cours = coursService.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));
        model.addAttribute("cours", cours);
        return "cours/formulaire";
    }

    @PostMapping("/sauvegarder")
    public String sauvegarder(@Valid @ModelAttribute Cours cours,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "cours/formulaire";
        }
        coursService.save(cours);
        redirectAttributes.addFlashAttribute("success", "Cours sauvegardé avec succès !");
        return "redirect:/cours";
    }

    @GetMapping("/supprimer/{id}")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        coursService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Cours supprimé avec succès !");
        return "redirect:/cours";
    }
}
