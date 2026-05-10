package hightech.edu.Inscription.service;

import hightech.edu.Inscription.model.Etudiant;
import hightech.edu.Inscription.repository.EtudiantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EtudiantService {

    private final EtudiantRepository etudiantRepository;

    public List<Etudiant> findAll() {
        return etudiantRepository.findAll();
    }

    public Page<Etudiant> findPaginated(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nom").ascending());
        if (search != null && !search.isBlank()) {
            return etudiantRepository.search(search.trim(), pageable);
        }
        return etudiantRepository.findAll(pageable);
    }

    public Optional<Etudiant> findById(Long id) {
        return etudiantRepository.findById(id);
    }

    public Optional<Etudiant> findByEmail(String email) {
        return etudiantRepository.findByEmail(email);
    }

    public Etudiant save(Etudiant etudiant) {
        return etudiantRepository.save(etudiant);
    }

    public void deleteById(Long id) {
        etudiantRepository.deleteById(id);
    }

    public long count() {
        return etudiantRepository.count();
    }
}
