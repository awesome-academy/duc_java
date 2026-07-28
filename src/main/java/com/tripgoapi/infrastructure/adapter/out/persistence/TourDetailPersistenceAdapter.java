package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.out.TourDetailRepositoryInterface;
import com.tripgoapi.domain.model.TourDetail;
import com.tripgoapi.domain.model.TourImage;
import com.tripgoapi.domain.model.TourItineraryDay;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourHighlightEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourIncludeEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.TourHighlightJpaRepository;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.TourImageJpaRepository;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.TourIncludeJpaRepository;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.TourItineraryJpaRepository;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.TourJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TourDetailPersistenceAdapter implements TourDetailRepositoryInterface {

    private static final String ACTIVE = "ACTIVE";
    private static final String INCLUDE = "INCLUDE";
    private static final String EXCLUDE = "EXCLUDE";

    private final TourJpaRepository tourJpaRepository;
    private final TourImageJpaRepository tourImageJpaRepository;
    private final TourItineraryJpaRepository tourItineraryJpaRepository;
    private final TourHighlightJpaRepository tourHighlightJpaRepository;
    private final TourIncludeJpaRepository tourIncludeJpaRepository;

    @Override
    public Optional<TourDetail> findById(Long id) {
        return tourJpaRepository.findByIdAndStatus(id, ACTIVE).map(this::toDomain);
    }

    @Override
    public boolean existsActiveTour(Long id) {
        return tourJpaRepository.existsByIdAndStatus(id, ACTIVE);
    }

    @Override
    public Optional<BigDecimal> findRatingAvg(Long id) {
        return tourJpaRepository.findByIdAndStatus(id, ACTIVE).map(TourEntity::getRatingAvg);
    }

    @Override
    public void recalculateRatingStats(Long tourId) {
        tourJpaRepository.recalculateRatingStats(tourId);
    }

    private TourDetail toDomain(TourEntity entity) {
        List<TourImage> images = tourImageJpaRepository.findByTour_IdOrderByDisplayOrderAsc(entity.getId()).stream()
                .map(i -> new TourImage(i.getImageUrl(), i.isThumbnail(), i.getDisplayOrder()))
                .toList();

        List<TourItineraryDay> itinerary = tourItineraryJpaRepository.findByTour_IdOrderByDayNumberAsc(entity.getId()).stream()
                .map(i -> new TourItineraryDay(i.getDayNumber(), i.getTitle(), i.getDescription()))
                .toList();

        List<String> highlights = tourHighlightJpaRepository.findByTour_Id(entity.getId()).stream()
                .map(TourHighlightEntity::getContent)
                .toList();

        List<TourIncludeEntity> includeEntities = tourIncludeJpaRepository.findByTour_Id(entity.getId());
        List<String> includes = includeEntities.stream()
                .filter(i -> INCLUDE.equals(i.getType()))
                .map(TourIncludeEntity::getContent)
                .toList();
        List<String> excludes = includeEntities.stream()
                .filter(i -> EXCLUDE.equals(i.getType()))
                .map(TourIncludeEntity::getContent)
                .toList();

        Long destinationId = entity.getDestination() != null ? entity.getDestination().getId() : null;
        String destinationName = entity.getDestination() != null ? entity.getDestination().getName() : null;

        return new TourDetail(
                entity.getId(),
                entity.getTitle(),
                entity.getSlug(),
                entity.getDescription(),
                destinationId,
                destinationName,
                entity.getDurationDays(),
                entity.getMaxGuests(),
                entity.getPrice(),
                entity.getDiscountPrice(),
                entity.getRatingAvg(),
                entity.getReviewCount(),
                images,
                highlights,
                itinerary,
                includes,
                excludes
        );
    }
}
