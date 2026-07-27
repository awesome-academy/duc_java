package com.tripgoapi.application.port.out;

import java.time.LocalDate;
import java.util.Optional;

public interface TourDepartureRepositoryInterface {

    Optional<Long> findDepartureId(Long tourId, LocalDate departureDate);

    /**
     * Atomically reserves {@code guestCount} slots on the given departure.
     * @return true if there was enough remaining capacity and the reservation succeeded
     */
    boolean reserveSlots(Long departureId, int guestCount);
}
