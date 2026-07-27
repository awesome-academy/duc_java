package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface TourJpaRepository extends JpaRepository<TourEntity, Long>, JpaSpecificationExecutor<TourEntity> {

    @Override
    @EntityGraph(attributePaths = "destination")
    @NonNull
    Page<TourEntity> findAll(Specification<TourEntity> spec, @NonNull Pageable pageable);

    @EntityGraph(attributePaths = "destination")
    Optional<TourEntity> findByIdAndStatus(Long id, String status);

    boolean existsByIdAndStatus(Long id, String status);
}
