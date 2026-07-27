package com.tripgoapi.infrastructure.mapper;

import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.infrastructure.adapter.in.web.dto.BookingResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingWebMapper {

    BookingResponse toResponse(Booking booking);

    List<BookingResponse> toResponseList(List<Booking> bookings);
}
