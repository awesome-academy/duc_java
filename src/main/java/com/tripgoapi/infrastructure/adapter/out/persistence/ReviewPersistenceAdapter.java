package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.out.ReviewRepositoryInterface;
import com.tripgoapi.domain.exception.ReviewAlreadyExistsException;
import com.tripgoapi.domain.model.Review;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.ReviewEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.ReviewJpaRepository;
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
    public boolean existsByTourIdAndUserId(Long tourId, Long userId) {
        return reviewJpaRepository.existsByTour_IdAndUser_Id(tourId, userId);
    }

    @Override
    public Review save(Long tourId, Long userId, int rating, String comment) {
        ReviewEntity entity = ReviewEntity.builder()
                .tour(TourEntity.builder().id(tourId).build())
                .user(UserEntity.builder().id(userId).build())
                .rating(rating)
                .comment(comment)
                .createdAt(OffsetDateTime.now())
                .build();

        try {
            // saveAndFlush forces the UNIQUE(tour_id, user_id) constraint check here, closing the
            // existsByTourIdAndUserId-then-save race between two concurrent review submissions
            // from the same user (see UserPersistenceAdapter.createUser for the same pattern).
            ReviewEntity saved = reviewJpaRepository.saveAndFlush(entity);
            // saved.getUser() is a stub entity holding only the id set above, so reviewerName is
            // left null here rather than silently reading it off the stub.
            return new Review(saved.getId(), null, saved.getRating(), saved.getComment(), saved.getCreatedAt());
        } catch (DataIntegrityViolationException ex) {
            throw new ReviewAlreadyExistsException(tourId);
        }
    }

    private Review toDomain(ReviewEntity entity) {
        String reviewerName = entity.getUser() != null ? entity.getUser().getFullName() : null;
        return new Review(entity.getId(), reviewerName, entity.getRating(), entity.getComment(), entity.getCreatedAt());
    }
}
