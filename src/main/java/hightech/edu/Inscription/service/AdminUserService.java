package hightech.edu.Inscription.service;

import hightech.edu.Inscription.model.AdminUser;
import hightech.edu.Inscription.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AdminUser user = adminUserRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }

    public AdminUser findByEmail(String email) {
        return adminUserRepository.findByEmail(email).orElse(null);
    }

    //Sauvegarde (création ou mise à jour) d'un AdminUser
    public AdminUser save(AdminUser admin) {
        return adminUserRepository.save(admin);
    }
}
