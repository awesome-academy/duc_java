package com.tripgoapi.application.port.out;

import com.tripgoapi.domain.model.DestinationCard;

import java.util.List;

public interface DestinationRepositoryInterface {
    List<DestinationCard> findAllWithTourCount();
}
