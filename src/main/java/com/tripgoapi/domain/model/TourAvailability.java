package com.tripgoapi.domain.model;

import java.time.LocalDate;

public record TourAvailability(LocalDate departureDate, int totalSlots, int bookedSlots) {

    public int remainingSlots() {
        return totalSlots - bookedSlots;
    }
}
