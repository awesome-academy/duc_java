package com.tripgoapi.domain.model;

import java.time.OffsetDateTime;

public record Review(Long id, String reviewerName, int rating, String comment, OffsetDateTime createdAt) {
}
