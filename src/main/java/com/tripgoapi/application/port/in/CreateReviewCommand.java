package com.tripgoapi.application.port.in;

public record CreateReviewCommand(Long userId, Long tourId, int rating, String comment) {
}
