package com.tripgoapi.infrastructure.adapter.out.storage;

import com.tripgoapi.application.port.in.UploadedImage;
import com.tripgoapi.application.port.out.FileStoragePort;
import com.tripgoapi.domain.exception.FileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Stores admin uploads on the local filesystem, outside the packaged jar, and exposes them under
 * {@code /uploads/**} (see {@code WebMvcConfig}). Writing into {@code src/main/resources/static}
 * would not work once the app is packaged — those files are read-only classpath entries.
 */
@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageAdapter.class);

    public static final String URL_PREFIX = "/uploads/";

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final String DEFAULT_EXTENSION = "jpg";

    private final Path root;

    public LocalFileStorageAdapter(@Value("${tripgo.upload.dir:uploads}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String store(UploadedImage image, String folder) {
        // The original filename is attacker-controlled: it is used only to pick an extension from
        // a fixed allowlist, never as part of the path.
        String extension = extensionOf(image.filename());
        String storedName = UUID.randomUUID() + "." + extension;

        Path folderPath = root.resolve(folder).normalize();
        try {
            Files.createDirectories(folderPath);
            Files.write(folderPath.resolve(storedName), image.content());
        } catch (IOException ex) {
            throw new FileStorageException("Không lưu được ảnh: " + image.filename(), ex);
        }

        return URL_PREFIX + folder + "/" + storedName;
    }

    @Override
    public void delete(String url) {
        // Seed data and older tours point at external image urls; those are not ours to remove.
        if (url == null || !url.startsWith(URL_PREFIX)) {
            return;
        }

        Path target = root.resolve(url.substring(URL_PREFIX.length())).normalize();
        if (!target.startsWith(root)) {
            // Only reachable if a crafted "/uploads/../.." url made it into the form.
            log.warn("Refusing to delete file outside the upload root: {}", url);
            return;
        }

        try {
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            // Best effort: a stale file on disk must never fail the admin's save.
            log.warn("Could not delete upload {}: {}", url, ex.getMessage());
        }
    }

    private String extensionOf(String filename) {
        if (filename == null) {
            return DEFAULT_EXTENSION;
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return DEFAULT_EXTENSION;
        }

        String extension = filename.substring(dot + 1).toLowerCase(Locale.ROOT);
        return ALLOWED_EXTENSIONS.contains(extension) ? extension : DEFAULT_EXTENSION;
    }

    Path getRoot() {
        return root;
    }
}
