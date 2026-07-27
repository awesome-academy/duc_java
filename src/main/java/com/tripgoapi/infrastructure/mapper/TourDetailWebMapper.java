package com.tripgoapi.infrastructure.mapper;

import com.tripgoapi.domain.model.TourDetail;
import com.tripgoapi.domain.model.TourImage;
import com.tripgoapi.domain.model.TourItineraryDay;
import com.tripgoapi.infrastructure.adapter.in.web.dto.TourDetailResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.TourImageResponse;
import com.tripgoapi.infrastructure.adapter.in.web.dto.TourItineraryDayResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TourDetailWebMapper {

    TourDetailResponse toResponse(TourDetail tourDetail);

    TourImageResponse toResponse(TourImage tourImage);

    TourItineraryDayResponse toResponse(TourItineraryDay tourItineraryDay);
}
