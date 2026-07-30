package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourImageJpaRepository extends JpaRepository<TourImageEntity, Long> {
    List<TourImageEntity> findByTour_IdOrderByDisplayOrderAsc(Long tourId);

    /** Admin save replaces the whole image set rather than diffing it row by row. */
    void deleteByTour_Id(Long tourId);
}
