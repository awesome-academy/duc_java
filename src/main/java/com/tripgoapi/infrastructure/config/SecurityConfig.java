package com.tripgoapi.infrastructure.config;

import com.tripgoapi.infrastructure.adapter.out.security.JwtAccessDeniedHandler;
import com.tripgoapi.infrastructure.adapter.out.security.JwtAuthenticationEntryPoint;
import com.tripgoapi.infrastructure.adapter.out.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/register",
            "/auth/login",
            "/auth/refresh",
            "/auth/logout",
            "/destinations/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    /**
     * The admin portal's CSS/JS and its uploaded images. This chain ends in
     * {@code anyRequest().authenticated()}, so without these the login page would render unstyled
     * — the browser fetches assets without an Authorization header.
     */
    private static final String[] STATIC_RESOURCES = {
            "/css/**",
            "/js/**",
            "/images/**",
            "/uploads/**",
            "/favicon.ico",
            "/error"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    // Runs after AdminSecurityConfig's chain, which claims /admin/** first.
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // GET-only: POST /tours/{id}/reviews needs the authenticated user's id,
                        // so it must fall through to .anyRequest().authenticated() below.
                        .requestMatchers(HttpMethod.GET, "/tours/**").permitAll()
                        .requestMatchers(STATIC_RESOURCES).permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Cost 12 instead of the library default (10): BCrypt encodes its cost factor in the
        // hash itself, so this only raises the work factor for newly-encoded passwords —
        // existing hashes at a lower cost still verify fine.
        return new BCryptPasswordEncoder(12);
    }
}
