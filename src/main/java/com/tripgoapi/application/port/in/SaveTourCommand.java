package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.exception.InvalidTourDataException;
import com.tripgoapi.domain.model.TourItineraryDay;
import com.tripgoapi.domain.model.TourStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Payload of the admin "Thêm / Sửa tour" form, shared by create and update.
 *
 * @param keptImageUrls images already attached to the tour that the admin did not remove, in
 *                      display order; empty on create
 * @param newImages     files uploaded in this submission, appended after {@code keptImageUrls}
 * @param thumbnailUrl  which kept image is the thumbnail; {@code null} (or an unknown url) falls
 *                      back to the first image of the resulting list
 */
public record SaveTourCommand(
        String title,
        Long destinationId,
        Long categoryId,
        BigDecimal price,
        BigDecimal discountPrice,
        Integer durationDays,
        Integer maxGuests,
        String description,
        boolean featured,
        TourStatus status,
        List<TourItineraryDay> itinerary,
        List<String> keptImageUrls,
        List<UploadedImage> newImages,
        String thumbnailUrl
) {

    public SaveTourCommand {
        if (title == null || title.isBlank()) {
            throw new InvalidTourDataException("Tên tour không được để trống");
        }
        if (destinationId == null) {
            throw new InvalidTourDataException("Điểm đến không được để trống");
        }
        if (price == null || price.signum() < 0) {
            throw new InvalidTourDataException("Giá tour phải >= 0");
        }
        if (discountPrice != null && discountPrice.compareTo(price) >= 0) {
            throw new InvalidTourDataException("Giá khuyến mãi phải nhỏ hơn giá gốc");
        }
        if (durationDays == null || durationDays < 1) {
            throw new InvalidTourDataException("Thời lượng phải >= 1 ngày");
        }

        title = title.trim();
        status = status == null ? TourStatus.ACTIVE : status;
        itinerary = defensiveCopy(itinerary);
        keptImageUrls = defensiveCopy(keptImageUrls);
        newImages = defensiveCopy(newImages);
    }

    /**
     * Nulls are dropped rather than copied: Spring's data binder auto-grows indexed form
     * properties, so a form submitted with a gap (image tile 2 removed client-side) arrives as a
     * list containing nulls — and {@code List.copyOf} would throw on them.
     */
    private static <T> List<T> defensiveCopy(List<T> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().filter(Objects::nonNull).toList();
    }
}
