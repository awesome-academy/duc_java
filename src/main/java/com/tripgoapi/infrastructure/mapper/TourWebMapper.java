package com.tripgoapi.infrastructure.mapper;

import com.tripgoapi.domain.model.Tour;
import com.tripgoapi.infrastructure.adapter.in.web.dto.TourCardResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TourWebMapper {

    TourCardResponse toResponse(Tour tour);

    List<TourCardResponse> toResponseList(List<Tour> tours);
}
