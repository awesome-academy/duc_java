package com.tripgoapi.infrastructure.config;

import com.tripgoapi.infrastructure.adapter.out.security.AdminUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Server-side protection for the Thymeleaf admin portal. Deliberately a separate filter chain
 * from {@link SecurityConfig}: the REST API is stateless + JWT + CSRF-off, while the admin portal
 * is session-based + form login + CSRF-on. Mixing the two into one chain would force one of them
 * to give up its correct defaults.
 */
@Configuration
@RequiredArgsConstructor
public class AdminSecurityConfig {

    /** Matches "/admin" itself as well, so hitting the bare url redirects to the login page. */
    private static final String[] ADMIN_PATHS = {"/admin", "/admin/**"};

    private final AdminUserDetailsService adminUserDetailsService;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http, PasswordEncoder passwordEncoder)
            throws Exception {
        http
                .securityMatcher(ADMIN_PATHS)
                // Built here instead of exposed as a bean: a top-level AuthenticationProvider bean
                // is adopted by the *global* AuthenticationManager, which would push admin
                // form-login credentials onto the stateless API chain too.
                .authenticationProvider(adminAuthenticationProvider(passwordEncoder))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login").permitAll()
                        // Every other admin route, including /admin itself: admins only. A logged-in
                        // customer hitting these gets 403, never the page.
                        .anyRequest().hasRole("ADMIN"))
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        // Always land on the dashboard: the saved-request default would replay the
                        // POST/DELETE that triggered the login redirect.
                        .defaultSuccessUrl("/admin", true)
                        .failureUrl("/admin/login?error")
                        .permitAll())
                .logout(logout -> logout
                        // POST-only (the default): a GET logout url can be triggered by any image
                        // tag on a page the admin visits.
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .exceptionHandling(handling -> handling.accessDeniedPage("/admin/403"));

        // CSRF stays at its default (enabled) here — every admin mutation goes through a
        // Thymeleaf <form th:action>, which injects the token automatically.
        return http.build();
    }

    private AuthenticationProvider adminAuthenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(adminUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
