package com.tripgoapi.application.port.in;

public interface CreateTourUseCase {

    /**
     * @return id of the newly created tour
     */
    Long createTour(SaveTourCommand command);
}
