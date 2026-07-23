package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourImageJpaRepository extends JpaRepository<TourImageEntity, Long> {
    List<TourImageEntity> findByTour_IdOrderByDisplayOrderAsc(Long tourId);
}
