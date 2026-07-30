package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.Review;

public interface CreateReviewUseCase {
    Review createReview(CreateReviewCommand command);
}
