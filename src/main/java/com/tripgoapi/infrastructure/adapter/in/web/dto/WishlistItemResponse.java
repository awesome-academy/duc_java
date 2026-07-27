package com.tripgoapi.infrastructure.adapter.in.web.dto;

import java.time.OffsetDateTime;

public record WishlistItemResponse(
        TourCardResponse tour,
        OffsetDateTime savedAt
) {
}
