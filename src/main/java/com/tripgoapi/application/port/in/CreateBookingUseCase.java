package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.Booking;

public interface CreateBookingUseCase {
    Booking createBooking(CreateBookingCommand command);
}
