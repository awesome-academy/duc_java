package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.out.DestinationRepositoryInterface;
import com.tripgoapi.domain.model.DestinationCard;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.DestinationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DestinationPersistenceAdapter implements DestinationRepositoryInterface {

    private final DestinationJpaRepository destinationJpaRepository;

    @Override
    public List<DestinationCard> findAllWithTourCount() {
        return destinationJpaRepository.findAllWithTourCount().stream()
                .map(p -> new DestinationCard(p.getId(), p.getName(), p.getSlug(), p.getImageUrl(), p.getTourCount()))
                .toList();
    }
}
