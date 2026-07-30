package com.tripgoapi.domain.model;

import java.text.Normalizer;
import java.util.Locale;

/**
 * URL slug derived from a Vietnamese title. Lives in the domain because slugs are part of the
 * tour/destination identity exposed by the public API, not a presentation concern of the admin UI.
 */
public final class Slug {

    private static final int MAX_LENGTH = 200;

    private Slug() {
    }

    /**
     * "Đà Nẵng - Hội An 3N2Đ" -> "da-nang-hoi-an-3n2d".
     *
     * @return the slugified text, or {@code null} when {@code text} has no slug-able characters
     * (e.g. blank, or punctuation only) — callers decide on the fallback.
     */
    public static String from(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        // Đ/đ have no combining-mark decomposition, so NFD alone leaves them untouched.
        String replaced = text.replace('Đ', 'D').replace('đ', 'd');

        String ascii = Normalizer.normalize(replaced, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        String slug = ascii.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        if (slug.isEmpty()) {
            return null;
        }
        return slug.length() > MAX_LENGTH ? slug.substring(0, MAX_LENGTH).replaceAll("-+$", "") : slug;
    }
}
