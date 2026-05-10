package hightech.edu.Inscription.repository;

import hightech.edu.Inscription.model.SiteContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteContentRepository extends JpaRepository<SiteContent, String> {
}
