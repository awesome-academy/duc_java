package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourIncludeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourIncludeJpaRepository extends JpaRepository<TourIncludeEntity, Long> {
    List<TourIncludeEntity> findByTour_Id(Long tourId);
}
