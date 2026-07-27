package com.tripgoapi.application.port.out;

import com.tripgoapi.domain.model.Role;

public record AuthenticatedPrincipal(Long userId, String email, Role role) {
}
