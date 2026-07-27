package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.RegisterUserCommand;
import com.tripgoapi.application.port.in.RegisterUserUseCase;
import com.tripgoapi.application.port.out.PasswordEncoderPort;
import com.tripgoapi.application.port.out.UserRepositoryInterface;
import com.tripgoapi.domain.exception.EmailAlreadyExistsException;
import com.tripgoapi.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryInterface userRepository;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    public User register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        String passwordHash = passwordEncoder.encode(command.password());
        return userRepository.createUser(command.fullName(), command.email(), passwordHash, command.phone());
    }
}
