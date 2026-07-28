package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.out.WishlistRepositoryInterface;
import com.tripgoapi.domain.model.Tour;
import com.tripgoapi.domain.model.WishlistItem;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.WishlistEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.WishlistJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WishlistPersistenceAdapter implements WishlistRepositoryInterface {

    private final WishlistJpaRepository wishlistJpaRepository;

    @Override
    public void add(Long userId, Long tourId) {
        // ON CONFLICT DO NOTHING at the database level: a duplicate add is a no-op, not an
        // exception, so there's no transaction-poisoning or narrow-constraint-name concern to
        // manage here. Transaction boundary is owned by the calling service (AddToWishlistService).
        wishlistJpaRepository.insertIgnoreDuplicate(userId, tourId);
    }

    @Override
    public void remove(Long userId, Long tourId) {
        wishlistJpaRepository.deleteByUserIdAndTourId(userId, tourId);
    }

    @Override
    public PageResult<WishlistItem> findByUserId(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<WishlistEntity> result = wishlistJpaRepository.findByUser_Id(userId, pageRequest);

        return new PageResult<>(
                result.getContent().stream().map(this::toDomain).toList(),
                result.getTotalElements(),
                page,
                size
        );
    }

    private WishlistItem toDomain(WishlistEntity entity) {
        return new WishlistItem(toDomain(entity.getTour()), entity.getCreatedAt());
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
