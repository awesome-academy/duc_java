package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
