package hightech.edu.Inscription;

import hightech.edu.Inscription.model.Cours;
import hightech.edu.Inscription.model.Etudiant;
import hightech.edu.Inscription.model.Inscription;
import hightech.edu.Inscription.model.InscriptionStatut;
import hightech.edu.Inscription.repository.CoursRepository;
import hightech.edu.Inscription.repository.EtudiantRepository;
import hightech.edu.Inscription.repository.InscriptionRepository;
import hightech.edu.Inscription.service.CoursService;
import hightech.edu.Inscription.service.EtudiantService;
import hightech.edu.Inscription.service.InscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscriptionApplicationTests {

    @Mock EtudiantRepository etudiantRepository;
    @Mock CoursRepository coursRepository;
    @Mock InscriptionRepository inscriptionRepository;

    @InjectMocks EtudiantService etudiantService;
    @InjectMocks CoursService coursService;
    @InjectMocks InscriptionService inscriptionService;

    private Etudiant etudiant;
    private Cours cours;
    private Inscription inscription;

    @BeforeEach
    void setUp() {
        // Build etudiant via setters (avoids @AllArgsConstructor fragility)
        etudiant = new Etudiant();
        etudiant.setId(1L);
        etudiant.setCivilite("M.");
        etudiant.setNom("Alaoui");
        etudiant.setPrenom("Youssef");
        etudiant.setEmail("youssef@test.edu");
        etudiant.setDateNaissance(LocalDate.of(2002, 3, 15));
        etudiant.setTelephone("0661001001");
        etudiant.setPays("Maroc");
        etudiant.setVille("Rabat");

        cours = new Cours();
        cours.setId(1L);
        cours.setTitre("Spring Boot");
        cours.setDescription("Architecture MVC");
        cours.setDuree(40);
        cours.setEnseignant("Dr. Bennani");

        inscription = new Inscription();
        inscription.setId(1L);
        inscription.setEtudiant(etudiant);
        inscription.setCours(cours);
        inscription.setDateInscription(LocalDate.now());
        inscription.setStatut(InscriptionStatut.EN_ATTENTE);
    }

    // ── EtudiantService ───────────────────────────────────────

    @Test @DisplayName("findAll() retourne la liste complète des étudiants")
    void etudiant_findAll_returnsAll() {
        when(etudiantRepository.findAll()).thenReturn(List.of(etudiant));
        List<Etudiant> result = etudiantService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNom()).isEqualTo("Alaoui");
    }

    @Test @DisplayName("findById() retourne l'étudiant correspondant")
    void etudiant_findById_found() {
        when(etudiantRepository.findById(1L)).thenReturn(Optional.of(etudiant));
        Optional<Etudiant> result = etudiantService.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("youssef@test.edu");
    }

    @Test @DisplayName("findById() retourne vide si étudiant inexistant")
    void etudiant_findById_notFound() {
        when(etudiantRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(etudiantService.findById(99L)).isEmpty();
    }

    @Test @DisplayName("save() appelle le repository et retourne l'étudiant sauvegardé")
    void etudiant_save_callsRepository() {
        when(etudiantRepository.save(any(Etudiant.class))).thenReturn(etudiant);
        Etudiant saved = etudiantService.save(etudiant);
        assertThat(saved.getId()).isEqualTo(1L);
        verify(etudiantRepository, times(1)).save(etudiant);
    }

    @Test @DisplayName("deleteById() appelle le repository")
    void etudiant_delete_callsRepository() {
        etudiantService.deleteById(1L);
        verify(etudiantRepository, times(1)).deleteById(1L);
    }

    @Test @DisplayName("count() retourne le bon nombre d'étudiants")
    void etudiant_count() {
        when(etudiantRepository.count()).thenReturn(10L);
        assertThat(etudiantService.count()).isEqualTo(10L);
    }

    @Test @DisplayName("findPaginated() sans recherche retourne une page")
    void etudiant_findPaginated_noSearch() {
        Page<Etudiant> mockPage = new PageImpl<>(List.of(etudiant));
        when(etudiantRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
        Page<Etudiant> result = etudiantService.findPaginated("", 0, 5);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test @DisplayName("findPaginated() avec recherche appelle search()")
    void etudiant_findPaginated_withSearch() {
        Page<Etudiant> mockPage = new PageImpl<>(List.of(etudiant));
        when(etudiantRepository.search(eq("Alaoui"), any(Pageable.class))).thenReturn(mockPage);
        Page<Etudiant> result = etudiantService.findPaginated("Alaoui", 0, 5);
        assertThat(result.getContent()).hasSize(1);
        verify(etudiantRepository).search(eq("Alaoui"), any(Pageable.class));
    }

    // ── CoursService ──────────────────────────────────────────

    @Test @DisplayName("findAll() retourne la liste complète des cours")
    void cours_findAll_returnsAll() {
        when(coursRepository.findAll()).thenReturn(List.of(cours));
        assertThat(coursService.findAll()).hasSize(1);
    }

    @Test @DisplayName("save() sauvegarde un cours et retourne l'objet")
    void cours_save() {
        when(coursRepository.save(any(Cours.class))).thenReturn(cours);
        Cours saved = coursService.save(cours);
        assertThat(saved.getTitre()).isEqualTo("Spring Boot");
        verify(coursRepository).save(cours);
    }

    @Test @DisplayName("findPaginated() avec recherche filtre par titre/enseignant")
    void cours_findPaginated_withSearch() {
        Page<Cours> mockPage = new PageImpl<>(List.of(cours));
        when(coursRepository.search(eq("Spring"), any(Pageable.class))).thenReturn(mockPage);
        Page<Cours> result = coursService.findPaginated("Spring", 0, 5);
        assertThat(result.getContent().get(0).getTitre()).isEqualTo("Spring Boot");
    }

    // ── InscriptionService ────────────────────────────────────

    @Test @DisplayName("existsByEtudiantIdAndCoursId() retourne true si doublon")
    void inscription_doublon_detected() {
        when(inscriptionRepository.existsByEtudiantIdAndCoursId(1L, 1L)).thenReturn(true);
        assertThat(inscriptionService.existsByEtudiantIdAndCoursId(1L, 1L)).isTrue();
    }

    @Test @DisplayName("existsByEtudiantIdAndCoursId() retourne false si pas doublon")
    void inscription_doublon_notDetected() {
        when(inscriptionRepository.existsByEtudiantIdAndCoursId(1L, 2L)).thenReturn(false);
        assertThat(inscriptionService.existsByEtudiantIdAndCoursId(1L, 2L)).isFalse();
    }

    @Test @DisplayName("findByEtudiantId() retourne les inscriptions de l'étudiant")
    void inscription_findByEtudiantId() {
        when(inscriptionRepository.findByEtudiantId(1L)).thenReturn(List.of(inscription));
        List<Inscription> result = inscriptionService.findByEtudiantId(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEtudiant().getNom()).isEqualTo("Alaoui");
    }

    @Test @DisplayName("findByCoursId() retourne les inscrits à un cours")
    void inscription_findByCoursId() {
        when(inscriptionRepository.findByCoursId(1L)).thenReturn(List.of(inscription));
        List<Inscription> result = inscriptionService.findByCoursId(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCours().getTitre()).isEqualTo("Spring Boot");
    }

    @Test @DisplayName("deleteById() supprime une inscription")
    void inscription_delete() {
        inscriptionService.deleteById(1L);
        verify(inscriptionRepository, times(1)).deleteById(1L);
    }

    @Test @DisplayName("Inscription statut est EN_ATTENTE par défaut")
    void inscription_statut_default() {
        assertThat(inscription.getStatut()).isEqualTo(InscriptionStatut.EN_ATTENTE);
    }
}
