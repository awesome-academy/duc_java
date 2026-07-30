package com.tripgoapi.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves admin uploads from the configured directory on disk. They cannot live in
 * {@code src/main/resources/static} because that folder is packaged read-only inside the jar.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String uploadDir;

    public WebMvcConfig(@Value("${tripgo.upload.dir:uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        // Path.toUri() only appends a trailing "/" when the path already exists as a directory on
        // disk at call time (it stats the filesystem). LocalFileStorageAdapter.store() only creates
        // this directory lazily on the first upload, so on a fresh clone/deploy this method can run
        // before the directory exists — toUri() would then omit the "/" and every resolved path
        // would concatenate wrong (e.g. ".../uploadstours/abc.jpg"). Creating it here up front keeps
        // the resource location correct from the very first request.
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create upload directory: " + root, e);
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(root.toUri().toString());
    }
}
