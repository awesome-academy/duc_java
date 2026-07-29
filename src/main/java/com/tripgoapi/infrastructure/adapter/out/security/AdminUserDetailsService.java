package com.tripgoapi.infrastructure.adapter.out.security;

import com.tripgoapi.application.port.out.UserRepositoryInterface;
import com.tripgoapi.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Backs the /admin/login form. Non-admin accounts are rejected here rather than after
 * authentication, so a customer who knows their own password never gets an admin session at all —
 * {@code hasRole('ADMIN')} on the routes is the second line of defence, not the only one.
 * The failure is a plain UsernameNotFoundException so the login page cannot be used to probe
 * which emails exist.
 */
@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final UserRepositoryInterface userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findCredentialsByEmail(email)
                .filter(credentials -> credentials.role() == Role.ADMIN)
                .map(AdminPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản quản trị: " + email));
    }
}
