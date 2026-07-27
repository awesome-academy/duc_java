package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.User;

public interface RegisterUserUseCase {
    User register(RegisterUserCommand command);
}
