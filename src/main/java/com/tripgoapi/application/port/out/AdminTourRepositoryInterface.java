package com.tripgoapi.application.port.out;

import com.tripgoapi.application.port.in.AdminTourSearchQuery;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.SaveTourCommand;
import com.tripgoapi.domain.model.AdminTourDetail;
import com.tripgoapi.domain.model.AdminTourSummary;
import com.tripgoapi.domain.model.TourImage;
import com.tripgoapi.domain.model.TourItineraryDay;

import java.util.List;
import java.util.Optional;

/**
 * Write side of the tour aggregate, used only by the admin portal. Kept separate from
 * {@link TourRepositoryInterface} / {@link TourDetailRepositoryInterface} so the public read
 * paths — which must never see INACTIVE or DELETED tours — cannot accidentally reach the
 * mutating operations.
 */
public interface AdminTourRepositoryInterface {

    /** ACTIVE + INACTIVE tours only; soft-deleted ones are never returned. */
    PageResult<AdminTourSummary> searchTours(AdminTourSearchQuery query);

    Optional<AdminTourDetail> findById(Long id);

    /** True when another (non-deleted) tour already owns this slug. */
    boolean existsBySlug(String slug, Long excludeTourId);

    /**
     * Inserts the tour together with its itinerary days and images.
     *
     * @return id of the inserted row
     */
    Long createTour(SaveTourCommand command, String slug, List<TourItineraryDay> itinerary, List<TourImage> images);

    /**
     * Updates the tour and fully replaces its itinerary days and images.
     *
     * @return false when the tour does not exist (or is already soft-deleted)
     */
    boolean updateTour(Long id, SaveTourCommand command, String slug, List<TourItineraryDay> itinerary, List<TourImage> images);

    /**
     * Flips status to DELETED.
     *
     * @return false when the tour does not exist or was already deleted
     */
    boolean softDeleteTour(Long id);

    /** Image urls currently attached to the tour — needed to clean up files the admin removed. */
    List<String> findImageUrls(Long tourId);
}
