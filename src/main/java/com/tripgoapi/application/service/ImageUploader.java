package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.UploadedImage;
import com.tripgoapi.application.port.out.FileStoragePort;
import com.tripgoapi.domain.exception.InvalidTourDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Shared upload guard for the admin forms: enforces the image allowlist and size cap before any
 * bytes reach storage. Content-type is checked here rather than in the web adapter so the rule
 * holds for every driving adapter, not just the Thymeleaf one.
 */
@Service
@RequiredArgsConstructor
public class ImageUploader {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/pjpeg", "image/png", "image/webp", "image/gif");

    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final FileStoragePort fileStorage;

    /**
     * @return public url of the stored image
     * @throws InvalidTourDataException if the file is not an allowed image type or is too large
     */
    public String upload(UploadedImage image, String folder) {
        String contentType = image.contentType() == null
                ? ""
                : image.contentType().toLowerCase(Locale.ROOT).trim();

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidTourDataException(
                    "Chỉ chấp nhận ảnh JPG, PNG, WEBP hoặc GIF (file: " + image.filename() + ")");
        }
        if (image.content().length > MAX_BYTES) {
            throw new InvalidTourDataException(
                    "Ảnh vượt quá 5MB (file: " + image.filename() + ")");
        }

        return fileStorage.store(image, folder);
    }

    /**
     * Best-effort cleanup of files that are no longer referenced by any row.
     *
     * <p>Deferred to run after the enclosing transaction commits, when one is active: the
     * filesystem is not transactional, so deleting inline would destroy the files even if the
     * transaction later rolls back — leaving a restored row pointing at images that no longer
     * exist. Outside a transaction (or once it has already committed), the delete runs immediately.
     */
    public void deleteAll(List<String> urls) {
        if (urls.isEmpty()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            urls.forEach(fileStorage::delete);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                urls.forEach(fileStorage::delete);
            }
        });
    }
}
