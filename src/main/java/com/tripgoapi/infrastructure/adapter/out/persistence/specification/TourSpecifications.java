package com.tripgoapi.infrastructure.adapter.out.persistence.specification;

import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class TourSpecifications {

    private TourSpecifications() {
    }

    public static Specification<TourEntity> hasStatus(String status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<TourEntity> keyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern);
    }

    public static Specification<TourEntity> hasDestinationSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("destination").get("slug"), slug);
    }

    public static Specification<TourEntity> hasCategorySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("category").get("slug"), slug);
    }

    public static Specification<TourEntity> minPrice(BigDecimal minPrice) {
        if (minPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<TourEntity> maxPrice(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<TourEntity> hasDuration(Integer durationDays) {
        if (durationDays == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("durationDays"), durationDays);
    }

    public static Specification<TourEntity> minRating(BigDecimal minRating) {
        if (minRating == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("ratingAvg"), minRating);
    }

    public static Specification<TourEntity> isFeatured(Boolean featured) {
        if (featured == null || !featured) {
            return null;
        }
        return (root, query, cb) -> cb.isTrue(root.get("featured"));
    }
}
