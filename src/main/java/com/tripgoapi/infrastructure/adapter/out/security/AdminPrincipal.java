package com.tripgoapi.infrastructure.adapter.out.security;

import com.tripgoapi.application.port.out.UserCredentials;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Session principal for the admin portal. Carries the admin's display name so the topbar can
 * greet them by name instead of by email.
 *
 * <p>Implements {@link CredentialsContainer} so {@code ProviderManager} can erase the password
 * hash right after authentication succeeds. Without it — {@code UserDetails} alone is not
 * enough — the hash would stay on this object, and therefore inside the HttpSession, for the
 * entire admin session.
 */
public class AdminPrincipal implements UserDetails, CredentialsContainer {

    private final Long id;
    private final String email;
    private final String fullName;
    private final List<GrantedAuthority> authorities;

    // Not final: eraseCredentials() nulls this out post-authentication.
    private String passwordHash;

    public AdminPrincipal(UserCredentials credentials) {
        this.id = credentials.id();
        this.email = credentials.email();
        this.fullName = credentials.fullName();
        this.passwordHash = credentials.passwordHash();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + credentials.role().name()));
    }

    @Override
    public void eraseCredentials() {
        this.passwordHash = null;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    /** Falls back to the email so the topbar is never blank. */
    public String getDisplayName() {
        return fullName == null || fullName.isBlank() ? email : fullName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
