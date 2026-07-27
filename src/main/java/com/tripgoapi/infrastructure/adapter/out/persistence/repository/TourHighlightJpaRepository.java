package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourHighlightEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourHighlightJpaRepository extends JpaRepository<TourHighlightEntity, Long> {
    List<TourHighlightEntity> findByTour_Id(Long tourId);
}
