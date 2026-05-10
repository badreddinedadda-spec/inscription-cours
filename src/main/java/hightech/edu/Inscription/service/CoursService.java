package hightech.edu.Inscription.service;

import hightech.edu.Inscription.model.Cours;
import hightech.edu.Inscription.repository.CoursRepository;
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
public class CoursService {

    private final CoursRepository coursRepository;

    public List<Cours> findAll() {
        return coursRepository.findAll();
    }

    public Page<Cours> findPaginated(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("titre").ascending());
        if (search != null && !search.isBlank()) {
            return coursRepository.search(search.trim(), pageable);
        }
        return coursRepository.findAll(pageable);
    }

    public Optional<Cours> findById(Long id) {
        return coursRepository.findById(id);
    }

    public Cours save(Cours cours) {
        return coursRepository.save(cours);
    }

    public void deleteById(Long id) {
        coursRepository.deleteById(id);
    }

    public long count() {
        return coursRepository.count();
    }

    public List<Cours> findRecent() {
        return coursRepository.findTop5ByOrderByIdDesc();
    }
}
