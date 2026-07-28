package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourDepartureEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.TourDepartureJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourDeparturePersistenceAdapterTest {

    private static final Long TOUR_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 8, 15);

    @Mock
    private TourDepartureJpaRepository tourDepartureJpaRepository;

    private TourDeparturePersistenceAdapter adapter;

    private TourDeparturePersistenceAdapter newAdapter() {
        return new TourDeparturePersistenceAdapter(tourDepartureJpaRepository);
    }

    @Test
    void findDepartureId_found_returnsId() {
        adapter = newAdapter();
        TourDepartureEntity entity = TourDepartureEntity.builder().id(42L).build();
        when(tourDepartureJpaRepository.findByTour_IdAndDepartureDate(TOUR_ID, DATE)).thenReturn(Optional.of(entity));

        assertThat(adapter.findDepartureId(TOUR_ID, DATE)).contains(42L);
    }

    @Test
    void findDepartureId_notFound_returnsEmpty() {
        adapter = newAdapter();
        when(tourDepartureJpaRepository.findByTour_IdAndDepartureDate(TOUR_ID, DATE)).thenReturn(Optional.empty());

        assertThat(adapter.findDepartureId(TOUR_ID, DATE)).isEmpty();
    }

    @Test
    void reserveSlots_oneRowAffected_returnsTrue() {
        adapter = newAdapter();
        when(tourDepartureJpaRepository.reserveSlotsIfAvailable(42L, 3)).thenReturn(1);

        assertThat(adapter.reserveSlots(42L, 3)).isTrue();
    }

    @Test
    void reserveSlots_zeroRowsAffected_returnsFalse() {
        // Zero rows means the WHERE (total-booked) >= guestCount predicate didn't match —
        // not enough remaining capacity. Must not be reported as a success.
        adapter = newAdapter();
        when(tourDepartureJpaRepository.reserveSlotsIfAvailable(42L, 3)).thenReturn(0);

        assertThat(adapter.reserveSlots(42L, 3)).isFalse();
    }

    @Test
    void releaseSlots_delegatesToRepository() {
        adapter = newAdapter();

        adapter.releaseSlots(42L, 3);

        org.mockito.Mockito.verify(tourDepartureJpaRepository).releaseSlots(42L, 3);
    }
}
