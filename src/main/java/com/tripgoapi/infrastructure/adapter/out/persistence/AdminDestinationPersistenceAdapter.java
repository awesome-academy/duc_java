package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.out.AdminDestinationRepositoryInterface;
import com.tripgoapi.domain.model.Destination;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.DestinationEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.DestinationJpaRepository;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.TourJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminDestinationPersistenceAdapter implements AdminDestinationRepositoryInterface {

    private final DestinationJpaRepository destinationJpaRepository;
    private final TourJpaRepository tourJpaRepository;

    @Override
    public List<Destination> findAll() {
        return destinationJpaRepository.findAllByOrderByNameAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Destination> findById(Long id) {
        return destinationJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug, Long excludeDestinationId) {
        return excludeDestinationId == null
                ? destinationJpaRepository.existsBySlug(slug)
                : destinationJpaRepository.existsBySlugAndIdNot(slug, excludeDestinationId);
    }

    @Override
    public Long createDestination(Destination destination) {
        return destinationJpaRepository.save(DestinationEntity.builder()
                .name(destination.name())
                .slug(destination.slug())
                .description(destination.description())
                .imageUrl(destination.imageUrl())
                .build()).getId();
    }

    @Override
    public boolean updateDestination(Destination destination) {
        Optional<DestinationEntity> found = destinationJpaRepository.findById(destination.id());
        if (found.isEmpty()) {
            return false;
        }

        DestinationEntity entity = found.get();
        entity.setName(destination.name());
        entity.setSlug(destination.slug());
        entity.setDescription(destination.description());
        entity.setImageUrl(destination.imageUrl());
        destinationJpaRepository.save(entity);
        return true;
    }

    @Override
    public boolean deleteDestination(Long id) {
        if (!destinationJpaRepository.existsById(id)) {
            return false;
        }
        destinationJpaRepository.deleteById(id);
        return true;
    }

    @Override
    public long countTours(Long destinationId) {
        // Counts DELETED tours too: they still hold a destination_id, so removing the destination
        // would break the FK regardless of the tour's status.
        return tourJpaRepository.countByDestination_Id(destinationId);
    }

    private Destination toDomain(DestinationEntity entity) {
        return new Destination(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getDescription(),
                entity.getImageUrl()
        );
    }
}
