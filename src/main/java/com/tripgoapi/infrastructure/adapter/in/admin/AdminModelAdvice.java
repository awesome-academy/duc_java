package com.tripgoapi.infrastructure.adapter.in.admin;

import com.tripgoapi.infrastructure.adapter.out.security.AdminPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Feeds the shared layout. Scoped to the admin package so it never runs for the REST controllers.
 */
@ControllerAdvice(basePackages = "com.tripgoapi.infrastructure.adapter.in.admin")
public class AdminModelAdvice {

    @ModelAttribute("adminName")
    public String adminName(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AdminPrincipal principal) {
            return principal.getDisplayName();
        }
        return "Admin";
    }
}
