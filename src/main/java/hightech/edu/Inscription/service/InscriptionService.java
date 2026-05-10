package hightech.edu.Inscription.service;

import hightech.edu.Inscription.model.Inscription;
import hightech.edu.Inscription.model.InscriptionStatut;
import hightech.edu.Inscription.repository.InscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class InscriptionService {

    @Autowired
    private InscriptionRepository inscriptionRepository;

    public List<Inscription> findAll() {
        return inscriptionRepository.findAll();
    }

    public Optional<Inscription> findById(Long id) {
        return inscriptionRepository.findById(id);
    }

    public Inscription save(Inscription inscription) {
        return inscriptionRepository.save(inscription);
    }

    public void deleteById(Long id) {
        inscriptionRepository.deleteById(id);
    }

    public List<Inscription> findByEtudiantId(Long etudiantId) {
        return inscriptionRepository.findByEtudiantId(etudiantId);
    }

    public List<Inscription> findByCoursId(Long coursId) {
        return inscriptionRepository.findByCoursId(coursId);
    }

    public boolean existsByEtudiantIdAndCoursId(Long etudiantId, Long coursId) {
        return inscriptionRepository.existsByEtudiantIdAndCoursId(etudiantId, coursId);
    }

    public long count() {
        return inscriptionRepository.count();
    }

    public long countByStatut(InscriptionStatut statut) {
        return inscriptionRepository.countByStatut(statut);
    }

    public List<Inscription> findByStatut(InscriptionStatut statut) {
        return inscriptionRepository.findByStatut(statut);
    }

    public List<Inscription> findRecent() {
        return inscriptionRepository.findTop6ByOrderByDateInscriptionDesc();
    }

    public void valider(Long id) {
        inscriptionRepository.findById(id).ifPresent(i -> {
            i.setStatut(InscriptionStatut.VALIDE);
            inscriptionRepository.save(i);
        });
    }

    public void annuler(Long id) {
        inscriptionRepository.findById(id).ifPresent(i -> {
            i.setStatut(InscriptionStatut.ANNULE);
            inscriptionRepository.save(i);
        });
    }
}
