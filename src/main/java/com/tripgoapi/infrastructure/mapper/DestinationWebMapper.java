package com.tripgoapi.infrastructure.mapper;

import com.tripgoapi.domain.model.DestinationCard;
import com.tripgoapi.infrastructure.adapter.in.web.dto.DestinationCardResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DestinationWebMapper {

    DestinationCardResponse toResponse(DestinationCard destinationCard);

    List<DestinationCardResponse> toResponseList(List<DestinationCard> destinationCards);
}
