package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.GetTourReviewsUseCase;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.TourReviewsResult;
import com.tripgoapi.application.port.out.ReviewRepositoryInterface;
import com.tripgoapi.application.port.out.TourDetailRepositoryInterface;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.domain.model.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GetTourReviewsService implements GetTourReviewsUseCase {

    private final TourDetailRepositoryInterface tourDetailRepository;
    private final ReviewRepositoryInterface reviewRepository;

    @Override
    @Transactional(readOnly = true)
    public TourReviewsResult getReviews(Long tourId, int page, int size) {
        if (!tourDetailRepository.existsActiveTour(tourId)) {
            throw new TourNotFoundException(tourId);
        }

        BigDecimal averageRating = tourDetailRepository.findRatingAvg(tourId).orElse(BigDecimal.ZERO);
        PageResult<Review> reviews = reviewRepository.findReviews(tourId, page, size);
        return new TourReviewsResult(reviews, averageRating);
    }
}
