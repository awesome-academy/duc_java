package com.tripgoapi.infrastructure.adapter.in.web.dto;

public record DestinationCardResponse(
        Long id,
        String name,
        String slug,
        String imageUrl,
        long tourCount
) {
}
