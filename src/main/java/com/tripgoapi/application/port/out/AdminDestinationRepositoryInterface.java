package com.tripgoapi.application.port.out;

import com.tripgoapi.domain.model.Destination;

import java.util.List;
import java.util.Optional;

/**
 * Write side of destinations. Separate from {@link DestinationRepositoryInterface}, which stays
 * a read-only projection for the public API.
 */
public interface AdminDestinationRepositoryInterface {

    List<Destination> findAll();

    Optional<Destination> findById(Long id);

    boolean existsBySlug(String slug, Long excludeDestinationId);

    Long createDestination(Destination destination);

    /** @return false when the destination does not exist */
    boolean updateDestination(Destination destination);

    /** @return false when the destination does not exist */
    boolean deleteDestination(Long id);

    /** Tours pointing at this destination — a non-zero count blocks deletion. */
    long countTours(Long destinationId);
}
