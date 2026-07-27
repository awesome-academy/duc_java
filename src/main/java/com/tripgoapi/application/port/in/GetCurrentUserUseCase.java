package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.User;

public interface GetCurrentUserUseCase {
    User getCurrentUser(Long userId);
}
