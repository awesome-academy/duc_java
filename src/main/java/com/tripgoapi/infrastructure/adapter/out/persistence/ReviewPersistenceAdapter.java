package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.out.ReviewRepositoryInterface;
import com.tripgoapi.domain.model.Review;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.ReviewEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

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

    private Review toDomain(ReviewEntity entity) {
        String reviewerName = entity.getUser() != null ? entity.getUser().getFullName() : null;
        return new Review(entity.getId(), reviewerName, entity.getRating(), entity.getComment(), entity.getCreatedAt());
    }
}
