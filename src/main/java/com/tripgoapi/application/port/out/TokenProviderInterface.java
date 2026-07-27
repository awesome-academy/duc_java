package com.tripgoapi.application.port.out;

import com.tripgoapi.domain.model.Role;

import java.util.Optional;

public interface TokenProviderInterface {

    AccessToken generateToken(Long userId, String email, Role role);

    Optional<AuthenticatedPrincipal> parseToken(String token);
}
