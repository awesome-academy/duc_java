package com.tripgoapi.infrastructure.mapper;

import com.tripgoapi.domain.model.Review;
import com.tripgoapi.infrastructure.adapter.in.web.dto.ReviewResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewWebMapper {

    ReviewResponse toResponse(Review review);

    List<ReviewResponse> toResponseList(List<Review> reviews);
}
