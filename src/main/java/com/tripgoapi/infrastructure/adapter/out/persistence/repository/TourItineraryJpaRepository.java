package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourItineraryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourItineraryJpaRepository extends JpaRepository<TourItineraryEntity, Long> {
    List<TourItineraryEntity> findByTour_IdOrderByDayNumberAsc(Long tourId);

    /** Admin save replaces the whole itinerary rather than diffing it day by day. */
    void deleteByTour_Id(Long tourId);
}
