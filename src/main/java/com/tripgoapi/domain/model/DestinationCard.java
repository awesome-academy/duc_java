package com.tripgoapi.domain.model;

public record DestinationCard(
        Long id,
        String name,
        String slug,
        String imageUrl,
        long tourCount
) {
}
