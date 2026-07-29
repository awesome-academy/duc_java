package com.tripgoapi.infrastructure.adapter.in.admin;

import com.tripgoapi.application.port.in.UploadedImage;
import com.tripgoapi.domain.exception.FileStorageException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Converts Servlet uploads into the framework-free {@link UploadedImage} the application layer
 * speaks. Reading the bytes here — at the adapter boundary — keeps {@code MultipartFile} out of
 * the inner layers entirely.
 */
final class MultipartFiles {

    private MultipartFiles() {
    }

    /** @return {@code null} for an empty file input, which browsers submit even when unused */
    static UploadedImage toUploadedImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            return new UploadedImage(file.getOriginalFilename(), file.getContentType(), file.getBytes());
        } catch (IOException ex) {
            throw new FileStorageException("Không đọc được file tải lên: " + file.getOriginalFilename(), ex);
        }
    }

    static List<UploadedImage> toUploadedImages(List<MultipartFile> files) {
        if (files == null) {
            return List.of();
        }
        return files.stream()
                .map(MultipartFiles::toUploadedImage)
                .filter(image -> image != null)
                .toList();
    }
}
