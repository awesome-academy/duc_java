package com.tripgoapi.infrastructure.mapper;

import com.tripgoapi.domain.model.WishlistItem;
import com.tripgoapi.infrastructure.adapter.in.web.dto.WishlistItemResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = TourWebMapper.class)
public interface WishlistWebMapper {

    WishlistItemResponse toResponse(WishlistItem item);

    List<WishlistItemResponse> toResponseList(List<WishlistItem> items);
}
