package com.tripgoapi.domain.exception;

import java.time.LocalDate;

public class TourDepartureNotFoundException extends NotFoundException {

    public TourDepartureNotFoundException(Long tourId, LocalDate departureDate) {
        super("No departure found for tour id=" + tourId + " on date=" + departureDate);
    }
}
