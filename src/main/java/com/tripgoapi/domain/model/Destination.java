package com.tripgoapi.domain.model;

/**
 * Editable destination. Distinct from {@link DestinationCard}, which is the read-only public
 * projection carrying a tour count instead of the description the admin form edits.
 */
public record Destination(
        Long id,
        String name,
        String slug,
        String description,
        String imageUrl
) {
}
