package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.AuthToken;
import com.tripgoapi.application.port.in.LoginCommand;
import com.tripgoapi.application.port.in.LoginUseCase;
import com.tripgoapi.application.port.out.LoginAttemptLimiterPort;
import com.tripgoapi.application.port.out.PasswordEncoderPort;
import com.tripgoapi.application.port.out.UserCredentials;
import com.tripgoapi.application.port.out.UserRepositoryInterface;
import com.tripgoapi.domain.exception.InvalidCredentialsException;
import com.tripgoapi.domain.exception.TooManyLoginAttemptsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final UserRepositoryInterface userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final AuthTokenIssuer authTokenIssuer;
    private final LoginAttemptLimiterPort loginAttemptLimiter;

    @Override
    public AuthToken login(LoginCommand command) {
        if (loginAttemptLimiter.isBlocked(command.email(), command.ipAddress())) {
            throw new TooManyLoginAttemptsException();
        }

        Optional<UserCredentials> credentials = userRepository.findCredentialsByEmail(command.email());
        if (credentials.isEmpty() || !passwordEncoder.matches(command.password(), credentials.get().passwordHash())) {
            loginAttemptLimiter.onLoginFailed(command.email(), command.ipAddress());
            throw new InvalidCredentialsException();
        }

        loginAttemptLimiter.onLoginSucceeded(command.email(), command.ipAddress());
        UserCredentials user = credentials.get();
        return authTokenIssuer.issue(user.id(), user.email(), user.role());
    }
}
