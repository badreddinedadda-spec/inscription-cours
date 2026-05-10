package hightech.edu.Inscription.repository;

import hightech.edu.Inscription.model.Inscription;
import hightech.edu.Inscription.model.InscriptionStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    List<Inscription> findByEtudiantId(Long etudiantId);
    List<Inscription> findByCoursId(Long coursId);
    boolean existsByEtudiantIdAndCoursId(Long etudiantId, Long coursId);
    List<Inscription> findByStatut(InscriptionStatut statut);
    long countByStatut(InscriptionStatut statut);
    List<Inscription> findTop6ByOrderByDateInscriptionDesc();
}
