package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.Destination;

import java.util.List;

public interface GetAdminDestinationsUseCase {

    List<Destination> getDestinations();

    /**
     * @throws com.tripgoapi.domain.exception.DestinationNotFoundException if no such destination
     */
    Destination getDestination(Long id);
}
