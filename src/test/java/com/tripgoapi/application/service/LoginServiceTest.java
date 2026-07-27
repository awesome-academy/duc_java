package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.AuthToken;
import com.tripgoapi.application.port.in.LoginCommand;
import com.tripgoapi.application.port.out.LoginAttemptLimiterPort;
import com.tripgoapi.application.port.out.PasswordEncoderPort;
import com.tripgoapi.application.port.out.UserCredentials;
import com.tripgoapi.application.port.out.UserRepositoryInterface;
import com.tripgoapi.domain.exception.InvalidCredentialsException;
import com.tripgoapi.domain.exception.TooManyLoginAttemptsException;
import com.tripgoapi.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepositoryInterface userRepository;
    @Mock
    private PasswordEncoderPort passwordEncoder;
    @Mock
    private AuthTokenIssuer authTokenIssuer;
    @Mock
    private LoginAttemptLimiterPort loginAttemptLimiter;

    private LoginService service;

    private LoginService newService() {
        return new LoginService(userRepository, passwordEncoder, authTokenIssuer, loginAttemptLimiter);
    }

    @Test
    void blockedByRateLimiter_rejectsBeforeTouchingUserRepositoryOrPassword() {
        service = newService();
        when(loginAttemptLimiter.isBlocked("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.login(new LoginCommand("jane@example.com", "whatever")))
                .isInstanceOf(TooManyLoginAttemptsException.class);

        verifyNoInteractions(userRepository, passwordEncoder, authTokenIssuer);
    }

    @Test
    void unknownEmail_recordsFailureAndThrowsGenericInvalidCredentials() {
        // Message stays generic ("invalid email or password") so the failure path here is
        // indistinguishable from a wrong-password failure below — prevents user enumeration.
        service = newService();
        when(loginAttemptLimiter.isBlocked("nobody@example.com")).thenReturn(false);
        when(userRepository.findCredentialsByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginCommand("nobody@example.com", "pw")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(loginAttemptLimiter).onLoginFailed("nobody@example.com");
        verifyNoInteractions(authTokenIssuer);
    }

    @Test
    void wrongPassword_recordsFailureAndThrowsInvalidCredentials() {
        service = newService();
        UserCredentials credentials = new UserCredentials(1L, "Jane", "jane@example.com", "hashed", Role.USER);
        when(loginAttemptLimiter.isBlocked("jane@example.com")).thenReturn(false);
        when(userRepository.findCredentialsByEmail("jane@example.com")).thenReturn(Optional.of(credentials));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginCommand("jane@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(loginAttemptLimiter).onLoginFailed("jane@example.com");
        verifyNoInteractions(authTokenIssuer);
    }

    @Test
    void correctCredentials_resetsLimiterAndIssuesToken() {
        service = newService();
        UserCredentials credentials = new UserCredentials(1L, "Jane", "jane@example.com", "hashed", Role.USER);
        AuthToken issued = new AuthToken("access", "refresh", "Bearer", 3600);
        when(loginAttemptLimiter.isBlocked("jane@example.com")).thenReturn(false);
        when(userRepository.findCredentialsByEmail("jane@example.com")).thenReturn(Optional.of(credentials));
        when(passwordEncoder.matches("correct", "hashed")).thenReturn(true);
        when(authTokenIssuer.issue(1L, "jane@example.com", Role.USER)).thenReturn(issued);

        AuthToken result = service.login(new LoginCommand("jane@example.com", "correct"));

        assertThat(result).isSameAs(issued);
        verify(loginAttemptLimiter).onLoginSucceeded("jane@example.com");
        verify(loginAttemptLimiter, never()).onLoginFailed(anyString());
    }
}
