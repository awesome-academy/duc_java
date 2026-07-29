package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.BookingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BookingJpaRepository extends JpaRepository<BookingEntity, Long> {

    @Query("SELECT b FROM BookingEntity b JOIN FETCH b.departure JOIN FETCH b.tour "
            + "WHERE b.user.id = :userId AND (:status IS NULL OR b.status = :status) ORDER BY b.createdAt DESC")
    List<BookingEntity> findByUserIdWithDeparture(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT b FROM BookingEntity b JOIN FETCH b.departure JOIN FETCH b.tour "
            + "WHERE b.user.id = :userId AND b.idempotencyKey = :idempotencyKey")
    Optional<BookingEntity> findByUserIdAndIdempotencyKeyWithDeparture(
            @Param("userId") Long userId, @Param("idempotencyKey") String idempotencyKey);

    @Query("SELECT b FROM BookingEntity b JOIN FETCH b.departure JOIN FETCH b.tour WHERE b.id = :id")
    Optional<BookingEntity> findByIdWithDeparture(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE BookingEntity b SET b.status = 'CANCELLED' "
            + "WHERE b.id = :id AND b.status IN ('PENDING', 'CONFIRMED')")
    int cancelIfCancellable(@Param("id") Long id);

    // Long derived name intentional: mirrors what used to be existsByUser_IdAndTour_IdAndStatusIn,
    // extended with the departure-date-passed condition review eligibility needs (see
    // BookingRepositoryInterface#existsReviewEligibleBooking for why).
    boolean existsByUser_IdAndTour_IdAndStatusInAndDeparture_DepartureDateLessThanEqual(
            Long userId, Long tourId, List<String> statuses, LocalDate onOrBeforeDate);
    boolean existsByUser_IdAndTour_IdAndStatusIn(Long userId, Long tourId, List<String> statuses);

    // --- Admin portal ---

    /** Every customer's bookings, newest first — the admin list is not scoped to one user. */
    @Query(value = "SELECT b FROM BookingEntity b JOIN FETCH b.departure JOIN FETCH b.tour "
            + "WHERE (:status IS NULL OR b.status = :status) ORDER BY b.createdAt DESC",
            countQuery = "SELECT COUNT(b) FROM BookingEntity b WHERE (:status IS NULL OR b.status = :status)")
    Page<BookingEntity> searchForAdmin(@Param("status") String status, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE BookingEntity b SET b.status = 'CONFIRMED' WHERE b.id = :id AND b.status = 'PENDING'")
    int confirmIfPending(@Param("id") Long id);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM BookingEntity b WHERE b.status IN ('CONFIRMED', 'COMPLETED')")
    BigDecimal sumRealizedRevenue();
}
