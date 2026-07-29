package com.tripgoapi.application.port.out;

import com.tripgoapi.application.port.in.UploadedImage;

/**
 * Where uploaded images live. Behind a port so swapping local disk for S3/Cloudinary later is an
 * adapter change only.
 */
public interface FileStoragePort {

    /**
     * @param folder logical sub-folder, e.g. {@code "tours"}
     * @return public url of the stored file, e.g. {@code /uploads/tours/<uuid>.jpg}
     * @throws com.tripgoapi.domain.exception.FileStorageException when the file cannot be written
     */
    String store(UploadedImage image, String folder);

    /**
     * Best-effort removal of a previously stored file. Urls this adapter did not produce are
     * ignored, so external image urls coming from the seed data are never touched.
     */
    void delete(String url);
}
