package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.Review;

import java.math.BigDecimal;

public record TourReviewsResult(PageResult<Review> reviews, BigDecimal averageRating) {
}
