package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourDepartureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TourDepartureJpaRepository extends JpaRepository<TourDepartureEntity, Long> {
    List<TourDepartureEntity> findByTour_IdAndDepartureDateBetweenOrderByDepartureDateAsc(
            Long tourId, LocalDate from, LocalDate to);
}
