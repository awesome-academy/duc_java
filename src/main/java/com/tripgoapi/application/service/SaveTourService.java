package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.CreateTourUseCase;
import com.tripgoapi.application.port.in.SaveTourCommand;
import com.tripgoapi.application.port.in.UpdateTourUseCase;
import com.tripgoapi.application.port.in.UploadedImage;
import com.tripgoapi.application.port.out.AdminTourRepositoryInterface;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.domain.model.Slug;
import com.tripgoapi.domain.model.TourImage;
import com.tripgoapi.domain.model.TourItineraryDay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaveTourService implements CreateTourUseCase, UpdateTourUseCase {

    private static final String IMAGE_FOLDER = "tours";
    private static final int MAX_SLUG_ATTEMPTS = 50;

    private final AdminTourRepositoryInterface adminTourRepository;
    private final ImageUploader imageUploader;

    @Override
    @Transactional
    public Long createTour(SaveTourCommand command) {
        List<TourImage> images = assembleImages(command);
        return adminTourRepository.createTour(
                command, uniqueSlug(command.title(), null), normalizeItinerary(command.itinerary()), images);
    }

    @Override
    @Transactional
    public void updateTour(Long id, SaveTourCommand command) {
        List<String> urlsBefore = adminTourRepository.findImageUrls(id);
        List<TourImage> images = assembleImages(command);

        if (!adminTourRepository.updateTour(
                id, command, uniqueSlug(command.title(), id), normalizeItinerary(command.itinerary()), images)) {
            throw new TourNotFoundException(id);
        }

        // Files whose rows we just replaced would otherwise linger on disk forever.
        // ImageUploader.deleteAll defers the actual delete until this transaction commits, so a
        // rollback (e.g. a later constraint violation) never destroys images the restored tour
        // still points at.
        List<String> urlsAfter = images.stream().map(TourImage::imageUrl).toList();
        imageUploader.deleteAll(urlsBefore.stream().filter(url -> !urlsAfter.contains(url)).toList());
    }

    /**
     * Kept images first (in the order the admin left them), then this submission's uploads. The
     * thumbnail flag lands on the admin's pick, or on the first image when that pick is gone.
     */
    private List<TourImage> assembleImages(SaveTourCommand command) {
        // LinkedHashSet: the form can echo the same kept url twice (e.g. a double submit), and
        // tour_images has no unique constraint to catch it.
        List<String> urls = new ArrayList<>(new LinkedHashSet<>(
                command.keptImageUrls().stream().filter(url -> url != null && !url.isBlank()).toList()));

        for (UploadedImage image : command.newImages()) {
            if (!image.isEmpty()) {
                urls.add(imageUploader.upload(image, IMAGE_FOLDER));
            }
        }

        if (urls.isEmpty()) {
            return List.of();
        }

        String thumbnailUrl = urls.contains(command.thumbnailUrl()) ? command.thumbnailUrl() : urls.get(0);

        List<TourImage> images = new ArrayList<>(urls.size());
        for (int i = 0; i < urls.size(); i++) {
            images.add(new TourImage(urls.get(i), urls.get(i).equals(thumbnailUrl), i));
        }
        return images;
    }

    /**
     * Drops rows the admin left entirely blank and renumbers the rest 1..n, so removing "Ngày 2"
     * in the middle of the form cannot leave a gap in day_number.
     */
    private List<TourItineraryDay> normalizeItinerary(List<TourItineraryDay> itinerary) {
        List<TourItineraryDay> days = new ArrayList<>();
        for (TourItineraryDay day : itinerary) {
            boolean blank = isBlank(day.title()) && isBlank(day.description());
            if (!blank) {
                days.add(new TourItineraryDay(days.size() + 1, trimToNull(day.title()), trimToNull(day.description())));
            }
        }
        return days;
    }

    /**
     * Slugs are unique in the DB, so two tours named "Đà Nẵng 3N2Đ" would collide. Suffix until
     * free rather than rejecting the save — the admin never types the slug and shouldn't have to
     * care that it exists.
     */
    private String uniqueSlug(String title, Long excludeTourId) {
        String base = Slug.from(title);
        if (base == null) {
            base = "tour";
        }

        String candidate = base;
        for (int suffix = 2; suffix < MAX_SLUG_ATTEMPTS; suffix++) {
            if (!adminTourRepository.existsBySlug(candidate, excludeTourId)) {
                return candidate;
            }
            candidate = base + "-" + suffix;
        }
        // Astronomically unlikely to be reached; a timestamp keeps the save working regardless.
        return base + "-" + System.currentTimeMillis();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
