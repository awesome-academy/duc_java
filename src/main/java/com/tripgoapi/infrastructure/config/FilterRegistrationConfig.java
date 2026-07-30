package com.tripgoapi.infrastructure.config;

import com.tripgoapi.infrastructure.adapter.out.security.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterRegistrationConfig {

    /**
     * {@link JwtAuthenticationFilter} is a {@code @Component} of type {@code Filter}, so Boot also
     * registers it directly on the servlet container — where it would run for <em>every</em>
     * request, including the session-based /admin/** chain that deliberately does not use it.
     * Turning the auto-registration off leaves exactly one instance: the one
     * {@link SecurityConfig} adds to the API chain.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
