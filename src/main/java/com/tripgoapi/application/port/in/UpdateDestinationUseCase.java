package com.tripgoapi.application.port.in;

public interface UpdateDestinationUseCase {

    void updateDestination(Long id, SaveDestinationCommand command);
}
