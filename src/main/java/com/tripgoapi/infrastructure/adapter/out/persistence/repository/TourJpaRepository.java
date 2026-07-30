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


    // --- Admin portal ---

    /**
     * Admin tour list: every non-deleted tour, searchable by title or destination name.
     *
     * <p>{@code pattern} is always bound to a non-null, already-lowercased LIKE pattern ("%" when
     * the search box is empty) rather than using an {@code :keyword IS NULL} guard. Postgres cannot
     * infer a type for a null String parameter, so the guarded form makes the driver bind it as
     * bytea and the query dies on {@code lower(bytea) does not exist}.
     *
     * <p>The join on destination is LEFT, and its name coalesced to "", so a tour with no
     * destination still shows up in the unfiltered list and can still match on its title.
     *
     * <p>{@code pattern} has its own {@code %}/{@code _}/{@code \} escaped by the caller before
     * being wrapped in wildcards, so {@code ESCAPE '\'} here treats them as literals instead of
     * LIKE metacharacters.
     */
    @EntityGraph(attributePaths = {"destination", "category"})
    @Query("""
            SELECT t FROM TourEntity t LEFT JOIN t.destination d
            WHERE t.status <> 'DELETED'
              AND (LOWER(t.title) LIKE :pattern ESCAPE '\\'
                   OR LOWER(COALESCE(d.name, '')) LIKE :pattern ESCAPE '\\')
            """)
    Page<TourEntity> searchForAdmin(@Param("pattern") String pattern, Pageable pageable);

    @EntityGraph(attributePaths = {"destination", "category"})
    Optional<TourEntity> findByIdAndStatusNot(Long id, String status);

    // Slug uniqueness is enforced across every row, DELETED ones included: the unique index does
    // not know about soft deletes, so reusing a deleted tour's slug would fail at insert time.
    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    long countByDestination_Id(Long destinationId);

    long countByStatus(String status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TourEntity t SET t.status = 'DELETED' WHERE t.id = :id AND t.status <> 'DELETED'")
    int softDelete(@Param("id") Long id);

    /**
     * Recomputes both derived columns straight from the reviews table in a single statement,
     * rather than incrementing counters in application code.
     *
     * <p>This is <b>not</b> immune to drift under READ COMMITTED on its own: when this UPDATE is
     * blocked by a concurrent one on the same row and then unblocked via Postgres's EvalPlanQual,
     * the subqueries in the SET clause still run against the transaction's original snapshot, so a
     * review committed just before could be missed. Callers must serialize per-tour themselves via
     * {@link #lockActiveTourForReview} (see {@code CreateReviewService}) — do not remove that lock
     * on the assumption this statement is safe by itself.
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
