package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.WishlistEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistJpaRepository extends JpaRepository<WishlistEntity, Long> {

    @EntityGraph(attributePaths = {"tour", "tour.destination"})
    Page<WishlistEntity> findByUser_Id(Long userId, Pageable pageable);

    // ON CONFLICT DO NOTHING makes a duplicate add a no-op at the database level — no
    // exists-then-insert race, no exception to catch, no transaction to poison.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO wishlists (user_id, tour_id, created_at)
            VALUES (:userId, :tourId, now())
            ON CONFLICT (user_id, tour_id) DO NOTHING
            """, nativeQuery = true)
    void insertIgnoreDuplicate(@Param("userId") Long userId, @Param("tourId") Long tourId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM WishlistEntity w WHERE w.user.id = :userId AND w.tour.id = :tourId")
    void deleteByUserIdAndTourId(@Param("userId") Long userId, @Param("tourId") Long tourId);
}
