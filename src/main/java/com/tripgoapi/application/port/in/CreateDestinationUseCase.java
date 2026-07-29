package com.tripgoapi.application.port.in;

public interface CreateDestinationUseCase {

    /**
     * @return id of the newly created destination
     */
    Long createDestination(SaveDestinationCommand command);
}
