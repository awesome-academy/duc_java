package com.tripgoapi.infrastructure.adapter.out.persistence.projection;

public interface DestinationCardProjection {
    Long getId();

    String getName();

    String getSlug();

    String getImageUrl();

    Long getTourCount();
}
