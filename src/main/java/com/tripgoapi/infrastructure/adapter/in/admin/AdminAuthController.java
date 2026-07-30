package com.tripgoapi.infrastructure.adapter.in.admin;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    /**
     * The login form itself. Spring Security handles the POST; this only renders the page and
     * bounces an already-signed-in admin straight to the dashboard.
     */
    @GetMapping("/login")
    public String login() {
        return isAuthenticatedAdmin() ? "redirect:/admin" : "admin/login";
    }

    /** Target of {@code accessDeniedPage}: a signed-in non-admin who reached an /admin/** url. */
    @GetMapping("/403")
    public String forbidden() {
        return "admin/403";
    }

    private boolean isAuthenticatedAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
