package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.out.WishlistRepositoryInterface;
import com.tripgoapi.domain.model.Tour;
import com.tripgoapi.domain.model.WishlistItem;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.WishlistEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.WishlistJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class WishlistPersistenceAdapter implements WishlistRepositoryInterface {

    private final WishlistJpaRepository wishlistJpaRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void add(Long userId, Long tourId) {
        WishlistEntity entity = WishlistEntity.builder()
                .user(UserEntity.builder().id(userId).build())
                .tour(TourEntity.builder().id(tourId).build())
                .createdAt(OffsetDateTime.now())
                .build();

        try {
            // saveAndFlush forces the (user_id, tour_id) unique constraint check to happen here,
            // same pattern as UserPersistenceAdapter#createUser: a duplicate add races safely
            // instead of relying on an exists-then-insert check that two concurrent requests
            // could both pass.
            //
            // REQUIRES_NEW: on Postgres a failed statement poisons the whole transaction until
            // rollback — catching the exception in Java doesn't undo that. Running the insert in
            // its own nested transaction means a duplicate only rolls back this insert, not
            // whatever the caller's transaction (e.g. AddToWishlistService) does around it.
            wishlistJpaRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException ex) {
            // Already in the wishlist — addToWishlist is idempotent, not an error.
        }
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
