package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.RegisterUserCommand;
import com.tripgoapi.application.port.out.PasswordEncoderPort;
import com.tripgoapi.application.port.out.UserRepositoryInterface;
import com.tripgoapi.domain.exception.EmailAlreadyExistsException;
import com.tripgoapi.domain.model.Role;
import com.tripgoapi.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepositoryInterface userRepository;
    @Mock
    private PasswordEncoderPort passwordEncoder;

    private RegisterUserService service;

    private RegisterUserService newService() {
        return new RegisterUserService(userRepository, passwordEncoder);
    }

    @Test
    void emailAlreadyRegistered_throwsConflict_withoutHashingOrCreating() {
        service = newService();
        RegisterUserCommand command = new RegisterUserCommand("Jane", "jane@example.com", "password123", null);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(command)).isInstanceOf(EmailAlreadyExistsException.class);

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).createUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void newEmail_hashesPasswordAndCreatesUser() {
        service = newService();
        RegisterUserCommand command = new RegisterUserCommand("Jane", "jane@example.com", "password123", "0900000000");
        User created = new User(1L, "Jane", "jane@example.com", "0900000000", Role.USER);

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.createUser("Jane", "jane@example.com", "hashed", "0900000000")).thenReturn(created);

        User result = service.register(command);

        assertThat(result).isSameAs(created);
    }
}
