package com.tripgoapi.domain.model;

import java.time.OffsetDateTime;

public record WishlistItem(Tour tour, OffsetDateTime savedAt) {
}
