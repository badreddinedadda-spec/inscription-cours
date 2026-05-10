package hightech.edu.Inscription.repository;

import hightech.edu.Inscription.model.Cours;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CoursRepository extends JpaRepository<Cours, Long> {

    @Query("SELECT c FROM Cours c WHERE " +
           "LOWER(c.titre)       LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.enseignant)  LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Cours> search(@Param("q") String query, Pageable pageable);

    Page<Cours> findAll(Pageable pageable);

    List<Cours> findTop5ByOrderByIdDesc();
}
