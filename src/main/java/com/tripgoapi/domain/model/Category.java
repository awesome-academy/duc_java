package com.tripgoapi.domain.model;

/** Tour type ("loại hình"), used to populate the admin tour form dropdown. */
public record Category(Long id, String name, String slug) {
}
