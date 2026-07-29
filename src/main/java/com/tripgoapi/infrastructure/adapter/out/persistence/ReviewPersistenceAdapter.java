package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.out.ReviewRepositoryInterface;
import com.tripgoapi.domain.exception.ReviewAlreadyExistsException;
import com.tripgoapi.domain.model.Review;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.ReviewEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.ReviewJpaRepository;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class ReviewPersistenceAdapter implements ReviewRepositoryInterface {

    private final ReviewJpaRepository reviewJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public PageResult<Review> findReviews(Long tourId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewEntity> result = reviewJpaRepository.findByTour_Id(tourId, pageRequest);

        return new PageResult<>(
                result.getContent().stream().map(this::toDomain).toList(),
                result.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public boolean existsByUserIdAndTourId(Long userId, Long tourId) {
        return reviewJpaRepository.existsByUser_IdAndTour_Id(userId, tourId);
    }

    @Override
    public Review save(Long userId, Long tourId, int rating, String comment) {
        ReviewEntity entity = ReviewEntity.builder()
                .tour(TourEntity.builder().id(tourId).build())
                // A managed reference, not a hand-built stub: getFullName() lazy-loads within this
                // same transaction, so toDomain can be reused below instead of hardcoding
                // reviewerName to null on the just-created review.
                .user(userJpaRepository.getReferenceById(userId))
                .rating(rating)
                .comment(comment)
                .createdAt(OffsetDateTime.now())
                .build();

        try {
            // saveAndFlush forces the UNIQUE(tour_id, user_id) constraint check here, closing the
            // existsByUserIdAndTourId-then-save race between two concurrent review submissions
            // from the same user (see UserPersistenceAdapter.createUser for the same pattern).
            return toDomain(reviewJpaRepository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException ex) {
            throw new ReviewAlreadyExistsException(tourId);
        }
    }

    private Review toDomain(ReviewEntity entity) {
        String reviewerName = entity.getUser() != null ? entity.getUser().getFullName() : null;
        return new Review(entity.getId(), reviewerName, entity.getRating(), entity.getComment(), entity.getCreatedAt());
    }
}
