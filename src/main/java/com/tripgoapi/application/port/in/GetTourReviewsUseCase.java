package com.tripgoapi.application.port.in;

public interface GetTourReviewsUseCase {
    TourReviewsResult getReviews(Long tourId, int page, int size);
}
