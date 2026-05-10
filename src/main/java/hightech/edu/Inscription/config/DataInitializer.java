package hightech.edu.Inscription.config;

import hightech.edu.Inscription.model.AdminUser;
import hightech.edu.Inscription.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (adminUserRepository.count() == 0) {
            AdminUser admin = new AdminUser();
            admin.setNom("EDDINE");
            admin.setPrenom("Badr");
            admin.setEmail("admin@hightech.edu");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setPoste("Directeur pédagogique");
            adminUserRepository.save(admin);
            System.out.println("✅ Compte admin créé : admin@hightech.edu / admin123");
        }
    }
}
