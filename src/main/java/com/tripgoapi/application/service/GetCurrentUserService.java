package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.GetCurrentUserUseCase;
import com.tripgoapi.application.port.out.UserRepositoryInterface;
import com.tripgoapi.domain.exception.UserNotFoundException;
import com.tripgoapi.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCurrentUserService implements GetCurrentUserUseCase {

    private final UserRepositoryInterface userRepository;

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
