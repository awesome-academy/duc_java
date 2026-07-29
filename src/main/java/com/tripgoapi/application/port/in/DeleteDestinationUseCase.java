package com.tripgoapi.application.port.in;

public interface DeleteDestinationUseCase {

    /**
     * Hard delete — destinations carry no history of their own.
     *
     * @throws com.tripgoapi.domain.exception.DestinationNotFoundException if no such destination
     * @throws com.tripgoapi.domain.exception.DestinationInUseException    if tours still reference
     *                                                                    it, which would leave
     *                                                                    those tours orphaned
     */
    void deleteDestination(Long id);
}
