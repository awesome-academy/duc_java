package com.tripgoapi.infrastructure.adapter.out.persistence.repository;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.DestinationEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.projection.DestinationCardProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DestinationJpaRepository extends JpaRepository<DestinationEntity, Long> {

    @Query(value = """
            SELECT d.id AS id, d.name AS name, d.slug AS slug, d.image_url AS imageUrl, COUNT(t.id) AS tourCount
            FROM destinations d
            LEFT JOIN tours t ON t.destination_id = d.id AND t.status = 'ACTIVE'
            GROUP BY d.id, d.name, d.slug, d.image_url
            ORDER BY d.name
            """, nativeQuery = true)
    List<DestinationCardProjection> findAllWithTourCount();

    List<DestinationEntity> findAllByOrderByNameAsc();

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
