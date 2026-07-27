package com.tripgoapi.application.port.out;

import com.tripgoapi.domain.model.Role;

public record UserCredentials(Long id, String fullName, String email, String passwordHash, Role role) {
}
