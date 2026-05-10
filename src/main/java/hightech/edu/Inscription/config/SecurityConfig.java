package hightech.edu.Inscription.config;

import hightech.edu.Inscription.service.AdminUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité Spring Security.
 *
 * ACCÈS ADMIN :
 *   L'URL /login n'est PAS exposée sur la page publique (landing).
 *   L'administrateur doit naviguer manuellement vers :
 *     /portail-admin-ht2025
 *   Cette URL redirige vers le formulaire de connexion.
 *   Aucun lien vers cette URL n'est présent dans le HTML public.
 *   Cela protège l'interface d'administration contre les bots et
 *   les utilisateurs non informés.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Clé secrète de l'URL admin — à personnaliser avant déploiement
    public static final String ADMIN_ENTRY_PATH = "/portail-admin-ht2025";

    private final AdminUserService adminUserService;

    public SecurityConfig(@Lazy AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminUserService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                .ignoringRequestMatchers("/s-inscrire", "/s-inscrire/**")
        )
            .authenticationProvider(authProvider())
            .authorizeHttpRequests(auth -> auth
                // ── Pages publiques ──────────────────────────────────────
                .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/favicon.svg").permitAll()

                // ── Formulaire d'inscription étudiant (public) ───────────
                .requestMatchers("/s-inscrire", "/s-inscrire/**").permitAll()

                // ── Accès admin via URL secrète ──────────────────────────
                // L'admin tape /portail-admin-ht2025 dans le navigateur.
                // Spring Security le redirige vers /login (formulaire caché).
                // L'URL /login elle-même reste accessible pour traiter le POST.
                .requestMatchers(ADMIN_ENTRY_PATH).permitAll()
                .requestMatchers("/login", "/forgot-password", "/reset-password").permitAll()

                // ── Toutes les autres routes nécessitent une authentification
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")  // Retour à la page publique après déconnexion
                .permitAll()
            );

        return http.build();
    }
}
