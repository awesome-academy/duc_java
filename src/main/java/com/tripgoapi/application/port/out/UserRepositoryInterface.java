package com.tripgoapi.application.port.out;

import com.tripgoapi.domain.model.User;

import java.util.Optional;

public interface UserRepositoryInterface {

    boolean existsByEmail(String email);

    User createUser(String fullName, String email, String passwordHash, String phone);

    Optional<UserCredentials> findCredentialsByEmail(String email);

    Optional<User> findById(Long id);
}
