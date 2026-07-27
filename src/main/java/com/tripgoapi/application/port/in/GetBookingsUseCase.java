package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.Booking;

import java.util.List;

public interface GetBookingsUseCase {
    List<Booking> getBookingsForUser(Long userId);
}
