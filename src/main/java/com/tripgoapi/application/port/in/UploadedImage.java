package com.tripgoapi.application.port.in;

/**
 * A freshly uploaded image, decoupled from Servlet's {@code MultipartFile} so the application
 * layer stays framework-free.
 */
public record UploadedImage(String filename, String contentType, byte[] content) {

    public boolean isEmpty() {
        return content == null || content.length == 0;
    }
}
