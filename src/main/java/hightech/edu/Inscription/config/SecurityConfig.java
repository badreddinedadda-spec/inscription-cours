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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ─── URL secrète admin ─────────────────────────────────────────────────
    // Seul chemin par lequel l'admin peut accéder au formulaire de connexion.
    // /login direct retourne 404 (invisible pour les bots et visiteurs).
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
                .authenticationProvider(authProvider())

                // ── CSRF : cookie-based, compatible Railway HTTPS ──────────────
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new XorCsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers(
                                "/forgot-password",
                                "/reset-password",
                                "/s-inscrire",
                                "/s-inscrire/**"
                        )
                )

                // ── Autorisations ──────────────────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        // Pages publiques
                        .requestMatchers(
                                "/", "/css/**", "/js/**", "/images/**",
                                "/favicon.ico", "/favicon.svg"
                        ).permitAll()

                        // Formulaire public d'inscription étudiant
                        .requestMatchers("/s-inscrire", "/s-inscrire/**").permitAll()

                        // URL secrète admin → accessible (redirige vers /login interne)
                        .requestMatchers(ADMIN_ENTRY_PATH).permitAll()

                        // Reset mot de passe (lien reçu par email)
                        .requestMatchers("/forgot-password", "/reset-password").permitAll()

                        // /login direct → BLOQUÉ (retourne 403/404 pour les visiteurs)
                        // Spring Security gère /login en interne via loginPage() ci-dessous,
                        // mais on NE le déclare PAS dans permitAll() → accès direct refusé.

                        // Tout le reste nécessite une authentification
                        .anyRequest().authenticated()
                )

                // ── Formulaire de connexion (géré en interne par Spring Security) ──
                .formLogin(form -> form
                                .loginPage("/login")              // Spring génère /login en interne
                                .loginProcessingUrl("/login")     // POST traité par Spring Security
                                .usernameParameter("email")
                                .passwordParameter("password")
                                .defaultSuccessUrl("/dashboard", true)
                                .failureUrl("/login?error=true")
                                .permitAll()                      // permitAll ici = Spring Security
                        // autorise /login uniquement pour
                        // les redirects internes. Un GET
                        // direct depuis le navigateur sera
                        // intercepté par LoginController
                        // qui renvoie 404.
                )

                // ── Déconnexion ────────────────────────────────────────────────
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}