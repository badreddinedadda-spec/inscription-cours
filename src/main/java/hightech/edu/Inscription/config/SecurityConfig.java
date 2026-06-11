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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Clé secrète de l'URL admin à personnaliser avant déploiement
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
                        .ignoringRequestMatchers("/","/forgot-password", "/reset-password", "/s-inscrire", "/s-inscrire/**")
                )
            .authenticationProvider(authProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/favicon.svg").permitAll()
                .requestMatchers("/s-inscrire", "/s-inscrire/**", "/confirmation/**").permitAll()

                .requestMatchers(ADMIN_ENTRY_PATH).permitAll()
                .requestMatchers("/login", "/forgot-password", "/reset-password").permitAll()

                //Toutes les autres routes necessitent une authentification
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
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}
