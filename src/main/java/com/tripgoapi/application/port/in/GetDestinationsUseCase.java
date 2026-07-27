package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.DestinationCard;

import java.util.List;

public interface GetDestinationsUseCase {
    List<DestinationCard> getDestinations();
}
