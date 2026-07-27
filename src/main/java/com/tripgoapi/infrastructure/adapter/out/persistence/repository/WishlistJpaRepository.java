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

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM WishlistEntity w WHERE w.user.id = :userId AND w.tour.id = :tourId")
    void deleteByUserIdAndTourId(@Param("userId") Long userId, @Param("tourId") Long tourId);
}
