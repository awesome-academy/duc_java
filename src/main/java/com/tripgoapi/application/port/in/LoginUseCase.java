package com.tripgoapi.application.port.in;

public interface LoginUseCase {
    AuthToken login(LoginCommand command);
}
