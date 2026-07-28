package com.tripgoapi.infrastructure.mapper;

import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.infrastructure.adapter.in.web.dto.BookingResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.TourSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingWebMapper {

    @Mapping(target = "tour", source = ".")
    BookingResponse toResponse(Booking booking);

    List<BookingResponse> toResponseList(List<Booking> bookings);

    default TourSummaryResponse toTourSummary(Booking booking) {
        return new TourSummaryResponse(booking.tourId(), booking.tourTitle(), booking.tourSlug(), booking.tourDurationDays());
    }
}
