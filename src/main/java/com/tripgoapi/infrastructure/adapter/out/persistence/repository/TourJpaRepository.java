package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
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

    /**
     * Recomputes both derived columns straight from the reviews table in a single statement,
     * rather than incrementing counters in application code. This is immune to the classic
     * increment-based lost-update problem (two concurrent "current count + 1" reads racing each
     * other) — but it is <b>not</b> immune to drift from two concurrent review inserts for the
     * <em>same</em> tour under READ COMMITTED: when this UPDATE blocks on another transaction's
     * row lock and then unblocks via EvalPlanQual, the {@code SET} subqueries still run against
     * the snapshot the blocked statement originally took, so they can miss a review the other
     * transaction just committed. Callers must serialize concurrent inserts per tour themselves —
     * see {@link #lockActiveTourForReview} and {@code CreateReviewService}.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE tours
            SET rating_avg = COALESCE((SELECT ROUND(AVG(rating)::numeric, 2) FROM reviews WHERE tour_id = :tourId), 0),
                review_count = (SELECT COUNT(*) FROM reviews WHERE tour_id = :tourId)
            WHERE id = :tourId
            """, nativeQuery = true)
    void recalculateRatingStats(@Param("tourId") Long tourId);

    /**
     * SELECT ... FOR UPDATE on the tour row, scoped to ACTIVE tours only, taken before a review is
     * inserted. This serializes two concurrent review submissions for the same tour: the second
     * transaction blocks here until the first commits, so by the time it reaches
     * {@link #recalculateRatingStats}, that statement's fresh per-statement snapshot is guaranteed
     * to include the first transaction's already-committed review.
     *
     * @return the tour id if it exists and is ACTIVE (now locked for this transaction), empty
     * otherwise — nothing is locked when the tour doesn't qualify
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t.id FROM TourEntity t WHERE t.id = :tourId AND t.status = 'ACTIVE'")
    Optional<Long> lockActiveTourForReview(@Param("tourId") Long tourId);
}
