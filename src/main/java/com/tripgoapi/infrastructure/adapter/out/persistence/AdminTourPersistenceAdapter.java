package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.in.AdminTourSearchQuery;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.SaveTourCommand;
import com.tripgoapi.application.port.out.AdminTourRepositoryInterface;
import com.tripgoapi.domain.model.AdminTourDetail;
import com.tripgoapi.domain.model.AdminTourSummary;
import com.tripgoapi.domain.model.TourImage;
import com.tripgoapi.domain.model.TourItineraryDay;
import com.tripgoapi.domain.model.TourStatus;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.CategoryEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.DestinationEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourImageEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourItineraryEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.CategoryJpaRepository;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.DestinationJpaRepository;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.TourImageJpaRepository;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.TourItineraryJpaRepository;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.TourJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminTourPersistenceAdapter implements AdminTourRepositoryInterface {

    private static final String DELETED = TourStatus.DELETED.name();

    private final TourJpaRepository tourJpaRepository;
    private final TourImageJpaRepository tourImageJpaRepository;
    private final TourItineraryJpaRepository tourItineraryJpaRepository;
    private final DestinationJpaRepository destinationJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public PageResult<AdminTourSummary> searchTours(AdminTourSearchQuery query) {
        PageRequest pageRequest = PageRequest.of(
                query.page() - 1, query.size(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TourEntity> page = tourJpaRepository.searchForAdmin(likePattern(query.keyword()), pageRequest);

        return new PageResult<>(
                page.getContent().stream().map(this::toSummary).toList(),
                page.getTotalElements(),
                query.page(),
                query.size()
        );
    }

    @Override
    public Optional<AdminTourDetail> findById(Long id) {
        return tourJpaRepository.findByIdAndStatusNot(id, DELETED).map(this::toDetail);
    }

    @Override
    public boolean existsBySlug(String slug, Long excludeTourId) {
        return excludeTourId == null
                ? tourJpaRepository.existsBySlug(slug)
                : tourJpaRepository.existsBySlugAndIdNot(slug, excludeTourId);
    }

    @Override
    public Long createTour(SaveTourCommand command, String slug, List<TourItineraryDay> itinerary, List<TourImage> images) {
        TourEntity entity = TourEntity.builder()
                .slug(slug)
                // Derived from reviews, not from the admin form — a brand new tour has none yet.
                .ratingAvg(BigDecimal.ZERO)
                .reviewCount(0)
                .createdAt(OffsetDateTime.now())
                .build();
        applyCommand(entity, command);

        TourEntity saved = tourJpaRepository.save(entity);
        replaceChildren(saved, itinerary, images);
        return saved.getId();
    }

    @Override
    public boolean updateTour(Long id, SaveTourCommand command, String slug, List<TourItineraryDay> itinerary, List<TourImage> images) {
        Optional<TourEntity> found = tourJpaRepository.findByIdAndStatusNot(id, DELETED);
        if (found.isEmpty()) {
            return false;
        }

        TourEntity entity = found.get();
        entity.setSlug(slug);
        applyCommand(entity, command);

        tourJpaRepository.save(entity);
        replaceChildren(entity, itinerary, images);
        return true;
    }

    @Override
    public boolean softDeleteTour(Long id) {
        return tourJpaRepository.softDelete(id) > 0;
    }

    @Override
    public List<String> findImageUrls(Long tourId) {
        return tourImageJpaRepository.findByTour_IdOrderByDisplayOrderAsc(tourId).stream()
                .map(TourImageEntity::getImageUrl)
                .toList();
    }

    /**
     * "%" matches every row, which is what an empty search box should do. Lowercased here to pair
     * with the LOWER(...) on both columns in the query.
     */
    private String likePattern(String keyword) {
        return keyword == null || keyword.isBlank()
                ? "%"
                : "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private void applyCommand(TourEntity entity, SaveTourCommand command) {
        entity.setTitle(command.title());
        entity.setDescription(command.description());
        // getReferenceById, not a findById round-trip: only the FK value is needed here.
        entity.setDestination(command.destinationId() == null
                ? null : destinationJpaRepository.getReferenceById(command.destinationId()));
        entity.setCategory(command.categoryId() == null
                ? null : categoryJpaRepository.getReferenceById(command.categoryId()));
        entity.setPrice(command.price());
        entity.setDiscountPrice(command.discountPrice());
        entity.setDurationDays(command.durationDays());
        entity.setMaxGuests(command.maxGuests());
        entity.setFeatured(command.featured());
        entity.setStatus(command.status().name());
    }

    /**
     * Itinerary days and images are owned entirely by the tour form, so a save replaces both sets
     * wholesale rather than diffing them. The flush forces the DELETEs to hit the DB before the
     * new INSERTs, instead of Hibernate's default insert-before-delete flush order.
     */
    private void replaceChildren(TourEntity tour, List<TourItineraryDay> itinerary, List<TourImage> images) {
        tourItineraryJpaRepository.deleteByTour_Id(tour.getId());
        tourImageJpaRepository.deleteByTour_Id(tour.getId());
        tourItineraryJpaRepository.flush();
        tourImageJpaRepository.flush();

        tourItineraryJpaRepository.saveAll(itinerary.stream()
                .map(day -> TourItineraryEntity.builder()
                        .tour(tour)
                        .dayNumber(day.dayNumber())
                        .title(day.title())
                        .description(day.description())
                        .build())
                .toList());

        tourImageJpaRepository.saveAll(images.stream()
                .map(image -> TourImageEntity.builder()
                        .tour(tour)
                        .imageUrl(image.imageUrl())
                        .thumbnail(image.thumbnail())
                        .displayOrder(image.displayOrder())
                        .build())
                .toList());
    }

    private AdminTourSummary toSummary(TourEntity entity) {
        return new AdminTourSummary(
                entity.getId(),
                entity.getTitle(),
                nameOf(entity.getDestination()),
                nameOf(entity.getCategory()),
                entity.getPrice(),
                entity.getDiscountPrice(),
                entity.getDurationDays(),
                entity.getRatingAvg(),
                TourStatus.valueOf(entity.getStatus())
        );
    }

    private AdminTourDetail toDetail(TourEntity entity) {
        List<TourImage> images = tourImageJpaRepository.findByTour_IdOrderByDisplayOrderAsc(entity.getId()).stream()
                .map(i -> new TourImage(i.getImageUrl(), i.isThumbnail(), i.getDisplayOrder()))
                .toList();

        List<TourItineraryDay> itinerary = tourItineraryJpaRepository.findByTour_IdOrderByDayNumberAsc(entity.getId()).stream()
                .map(i -> new TourItineraryDay(i.getDayNumber(), i.getTitle(), i.getDescription()))
                .toList();

        return new AdminTourDetail(
                entity.getId(),
                entity.getTitle(),
                entity.getSlug(),
                entity.getDescription(),
                idOf(entity.getDestination()),
                nameOf(entity.getDestination()),
                idOf(entity.getCategory()),
                nameOf(entity.getCategory()),
                entity.getPrice(),
                entity.getDiscountPrice(),
                entity.getDurationDays(),
                entity.getMaxGuests(),
                entity.isFeatured(),
                TourStatus.valueOf(entity.getStatus()),
                images,
                itinerary
        );
    }

    private Long idOf(DestinationEntity destination) {
        return destination == null ? null : destination.getId();
    }

    private Long idOf(CategoryEntity category) {
        return category == null ? null : category.getId();
    }

    private String nameOf(DestinationEntity destination) {
        return destination == null ? null : destination.getName();
    }

    private String nameOf(CategoryEntity category) {
        return category == null ? null : category.getName();
    }
}
