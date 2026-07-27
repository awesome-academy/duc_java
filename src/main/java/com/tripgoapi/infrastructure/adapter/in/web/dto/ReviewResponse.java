package com.tripgoapi.infrastructure.adapter.in.web.dto;

import java.time.OffsetDateTime;

public record ReviewResponse(Long id, String reviewerName, int rating, String comment, OffsetDateTime createdAt) {
}
