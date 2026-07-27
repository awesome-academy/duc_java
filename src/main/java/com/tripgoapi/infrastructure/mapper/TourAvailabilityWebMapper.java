package com.tripgoapi.infrastructure.mapper;

import com.tripgoapi.domain.model.TourAvailability;
import com.tripgoapi.infrastructure.adapter.in.web.dto.TourAvailabilityResponse;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TourAvailabilityWebMapper {

    @Mapping(target = "remainingSlots", expression = "java(tourAvailability.remainingSlots())")
    TourAvailabilityResponse toResponse(TourAvailability tourAvailability);

    List<TourAvailabilityResponse> toResponseList(List<TourAvailability> tourAvailabilities);
}
