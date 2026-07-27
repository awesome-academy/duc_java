package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingJpaRepository extends JpaRepository<BookingEntity, Long> {

    @Query("SELECT b FROM BookingEntity b JOIN FETCH b.departure WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<BookingEntity> findByUserIdWithDeparture(@Param("userId") Long userId);

    @Query("SELECT b FROM BookingEntity b JOIN FETCH b.departure WHERE b.idempotencyKey = :idempotencyKey")
    Optional<BookingEntity> findByIdempotencyKeyWithDeparture(@Param("idempotencyKey") String idempotencyKey);
}
