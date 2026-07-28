package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Recomputes both derived columns straight from the reviews table in a single statement,
    // rather than incrementing counters in application code — immune to drift regardless of how
    // many reviews already exist or how many inserts race concurrently.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE tours
            SET rating_avg = COALESCE((SELECT ROUND(AVG(rating)::numeric, 2) FROM reviews WHERE tour_id = :tourId), 0),
                review_count = (SELECT COUNT(*) FROM reviews WHERE tour_id = :tourId)
            WHERE id = :tourId
            """, nativeQuery = true)
    void recalculateRatingStats(@Param("tourId") Long tourId);
}
