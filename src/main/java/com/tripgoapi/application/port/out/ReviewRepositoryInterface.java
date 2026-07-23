package com.tripgoapi.application.port.out;

import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.domain.model.Review;

public interface ReviewRepositoryInterface {
    PageResult<Review> findReviews(Long tourId, int page, int size);
}
