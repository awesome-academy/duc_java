package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.exception.InvalidTourDataException;

/**
 * @param keptImageUrl the destination's current image, kept when no new file is uploaded
 * @param newImage     freshly uploaded file; when present it replaces {@code keptImageUrl}
 */
public record SaveDestinationCommand(
        String name,
        String description,
        String keptImageUrl,
        UploadedImage newImage
) {

    public SaveDestinationCommand {
        if (name == null || name.isBlank()) {
            throw new InvalidTourDataException("Tên điểm đến không được để trống");
        }
        name = name.trim();
    }
}
