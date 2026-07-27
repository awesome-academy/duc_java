package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.GetDestinationsUseCase;
import com.tripgoapi.application.port.out.DestinationRepositoryInterface;
import com.tripgoapi.domain.model.DestinationCard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDestinationsService implements GetDestinationsUseCase {

    private final DestinationRepositoryInterface destinationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DestinationCard> getDestinations() {
        return destinationRepository.findAllWithTourCount();
    }
}
