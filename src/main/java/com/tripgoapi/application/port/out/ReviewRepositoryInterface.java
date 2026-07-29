package com.tripgoapi.application.port.out;

import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.domain.model.Review;

public interface ReviewRepositoryInterface {
    PageResult<Review> findReviews(Long tourId, int page, int size);

    // (userId, tourId) order, matching BookingRepositoryInterface.existsByUserIdAndTourIdAndStatusIn
    // and CreateReviewCommand's own field order — both ports take two adjacent Long ids, so a
    // consistent order across them is what makes an accidental swap a compile error somewhere
    // else, instead of a silently-wrong runtime result here.
    boolean existsByUserIdAndTourId(Long userId, Long tourId);

    Review save(Long userId, Long tourId, int rating, String comment);
}
