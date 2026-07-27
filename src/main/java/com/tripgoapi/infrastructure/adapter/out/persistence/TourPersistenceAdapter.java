package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.TourSearchQuery;
import com.tripgoapi.application.port.out.TourRepositoryInterface;
import com.tripgoapi.domain.model.Tour;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.TourJpaRepository;
import com.tripgoapi.infrastructure.adapter.out.persistence.specification.TourSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class TourPersistenceAdapter implements TourRepositoryInterface {

    private final TourJpaRepository tourJpaRepository;

    @Override
    public PageResult<Tour> searchTours(TourSearchQuery query) {
        List<Specification<TourEntity>> specs = Stream.of(
                        TourSpecifications.hasStatus("ACTIVE"),
                        TourSpecifications.keyword(query.keyword()),
                        TourSpecifications.hasDestinationSlug(query.destinationSlug()),
                        TourSpecifications.hasCategorySlug(query.categorySlug()),
                        TourSpecifications.minPrice(query.minPrice()),
                        TourSpecifications.maxPrice(query.maxPrice()),
                        TourSpecifications.hasDuration(query.duration()),
                        TourSpecifications.minRating(query.minRating()),
                        TourSpecifications.isFeatured(query.featured())
                )
                .filter(Objects::nonNull)
                .toList();
        Specification<TourEntity> spec = Specification.allOf(specs);

        PageRequest pageRequest = PageRequest.of(query.page() - 1, query.size(), toSort(query.sort()));
        Page<TourEntity> page = tourJpaRepository.findAll(spec, pageRequest);

        return new PageResult<>(
                page.getContent().stream().map(this::toDomain).toList(),
                page.getTotalElements(),
                query.page(),
                query.size()
        );
    }

    private Sort toSort(com.tripgoapi.application.port.in.TourSortOption sort) {
        return switch (sort) {
            case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price");
            case RATING_DESC -> Sort.by(Sort.Direction.DESC, "ratingAvg");
        };
    }

    private Tour toDomain(TourEntity entity) {
        Long destinationId = entity.getDestination() != null ? entity.getDestination().getId() : null;
        String destinationName = entity.getDestination() != null ? entity.getDestination().getName() : null;

        return new Tour(
                entity.getId(),
                entity.getTitle(),
                entity.getSlug(),
                destinationId,
                destinationName,
                entity.getPrice(),
                entity.getDiscountPrice(),
                entity.getDurationDays(),
                entity.getRatingAvg(),
                entity.getReviewCount(),
                entity.isFeatured()
        );
    }
}
