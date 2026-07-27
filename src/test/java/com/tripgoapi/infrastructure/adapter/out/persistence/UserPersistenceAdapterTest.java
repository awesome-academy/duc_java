package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.out.UserCredentials;
import com.tripgoapi.domain.exception.EmailAlreadyExistsException;
import com.tripgoapi.domain.model.Role;
import com.tripgoapi.domain.model.User;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    private UserPersistenceAdapter adapter;

    private UserPersistenceAdapter newAdapter() {
        return new UserPersistenceAdapter(userJpaRepository);
    }

    @Test
    void createUser_savesAndReturnsDomainUser() {
        adapter = newAdapter();
        UserEntity saved = UserEntity.builder()
                .id(1L).fullName("Jane").email("jane@example.com").passwordHash("hashed")
                .phone(null).role("USER").createdAt(OffsetDateTime.now())
                .build();
        when(userJpaRepository.saveAndFlush(any(UserEntity.class))).thenReturn(saved);

        User result = adapter.createUser("Jane", "jane@example.com", "hashed", null);

        assertThat(result).isEqualTo(new User(1L, "Jane", "jane@example.com", null, Role.USER));
    }

    @Test
    void createUser_raceLostToUniqueConstraint_translatesToEmailAlreadyExists() {
        // Regression test for the register race-condition fix: existsByEmail-then-createUser
        // is not atomic, so a concurrent duplicate registration can slip past the pre-check
        // and only get caught by the DB's unique constraint on flush. That must surface as a
        // clean 409-mapped domain exception, not an unhandled DataIntegrityViolationException.
        adapter = newAdapter();
        when(userJpaRepository.saveAndFlush(any(UserEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        assertThatThrownBy(() -> adapter.createUser("Jane", "jane@example.com", "hashed", null))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void findCredentialsByEmail_found() {
        adapter = newAdapter();
        UserEntity entity = UserEntity.builder()
                .id(1L).fullName("Jane").email("jane@example.com").passwordHash("hashed").role("ADMIN")
                .build();
        when(userJpaRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(entity));

        Optional<UserCredentials> result = adapter.findCredentialsByEmail("jane@example.com");

        assertThat(result).contains(new UserCredentials(1L, "Jane", "jane@example.com", "hashed", Role.ADMIN));
    }

    @Test
    void findCredentialsByEmail_notFound() {
        adapter = newAdapter();
        when(userJpaRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThat(adapter.findCredentialsByEmail("missing@example.com")).isEmpty();
    }

    @Test
    void findById_mapsToDomain() {
        adapter = newAdapter();
        UserEntity entity = UserEntity.builder()
                .id(1L).fullName("Jane").email("jane@example.com").passwordHash("hashed")
                .phone("0900000000").role("USER").build();
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThat(adapter.findById(1L)).contains(new User(1L, "Jane", "jane@example.com", "0900000000", Role.USER));
    }
}
