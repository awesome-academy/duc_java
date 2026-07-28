package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourDepartureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TourDepartureJpaRepository extends JpaRepository<TourDepartureEntity, Long> {
    List<TourDepartureEntity> findByTour_IdAndDepartureDateBetweenOrderByDepartureDateAsc(
            Long tourId, LocalDate from, LocalDate to);

    Optional<TourDepartureEntity> findByTour_IdAndDepartureDate(Long tourId, LocalDate departureDate);

    // clearAutomatically: this bulk UPDATE bypasses the persistence context, so any
    // TourDepartureEntity already loaded in the current transaction (e.g. via
    // findByTour_IdAndDepartureDate) would otherwise keep a stale bookedSlots value.
    @Modifying(clearAutomatically = true)
    @Query("UPDATE TourDepartureEntity d SET d.bookedSlots = d.bookedSlots + :guestCount "
            + "WHERE d.id = :departureId AND (d.totalSlots - d.bookedSlots) >= :guestCount")
    int reserveSlotsIfAvailable(@Param("departureId") Long departureId, @Param("guestCount") int guestCount);

    // Clamped at 0: defends against bookedSlots going negative should this ever run twice
    // for the same booking (e.g. a retried cancel request).
    @Modifying(clearAutomatically = true)
    @Query("UPDATE TourDepartureEntity d SET d.bookedSlots = "
            + "CASE WHEN d.bookedSlots - :guestCount < 0 THEN 0 ELSE d.bookedSlots - :guestCount END "
            + "WHERE d.id = :departureId")
    void releaseSlots(@Param("departureId") Long departureId, @Param("guestCount") int guestCount);
}
