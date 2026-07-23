package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourItineraryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourItineraryJpaRepository extends JpaRepository<TourItineraryEntity, Long> {
    List<TourItineraryEntity> findByTour_IdOrderByDayNumberAsc(Long tourId);
}
