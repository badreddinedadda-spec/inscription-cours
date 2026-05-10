package hightech.edu.Inscription.repository;

import hightech.edu.Inscription.model.Etudiant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    Optional<Etudiant> findByEmail(String email);

    @Query("SELECT e FROM Etudiant e WHERE LOWER(e.nom) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(e.prenom) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(e.email) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Etudiant> search(@Param("q") String query, Pageable pageable);
}
