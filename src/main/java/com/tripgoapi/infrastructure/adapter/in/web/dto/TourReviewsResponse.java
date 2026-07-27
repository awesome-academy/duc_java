package com.tripgoapi.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record TourReviewsResponse(List<ReviewResponse> data, long total, int page, int size, BigDecimal averageRating) {
}
