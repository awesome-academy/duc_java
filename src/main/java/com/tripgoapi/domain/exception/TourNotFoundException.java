package com.tripgoapi.domain.exception;

public class TourNotFoundException extends NotFoundException {

    public TourNotFoundException(Long id) {
        super("Tour not found: id=" + id);
    }
}
