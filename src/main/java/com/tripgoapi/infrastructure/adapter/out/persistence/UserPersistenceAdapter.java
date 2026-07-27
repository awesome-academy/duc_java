package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.out.UserCredentials;
import com.tripgoapi.application.port.out.UserRepositoryInterface;
import com.tripgoapi.domain.exception.EmailAlreadyExistsException;
import com.tripgoapi.domain.model.Role;
import com.tripgoapi.domain.model.User;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryInterface {

    private final UserJpaRepository userJpaRepository;

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public User createUser(String fullName, String email, String passwordHash, String phone) {
        UserEntity entity = UserEntity.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(passwordHash)
                .phone(phone)
                .role(Role.USER.name())
                .createdAt(OffsetDateTime.now())
                .build();

        try {
            // saveAndFlush forces the unique-email constraint check to happen here, inside
            // this try block, instead of silently at end-of-transaction flush time — closing
            // the existsByEmail-then-createUser race between two concurrent registrations.
            return toDomain(userJpaRepository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    @Override
    public Optional<UserCredentials> findCredentialsByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(e -> new UserCredentials(e.getId(), e.getFullName(), e.getEmail(), e.getPasswordHash(), Role.valueOf(e.getRole())));
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id).map(this::toDomain);
    }

    private User toDomain(UserEntity entity) {
        return new User(entity.getId(), entity.getFullName(), entity.getEmail(), entity.getPhone(), Role.valueOf(entity.getRole()));
    }
}
