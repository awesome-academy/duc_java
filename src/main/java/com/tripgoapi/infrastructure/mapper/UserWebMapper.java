package com.tripgoapi.infrastructure.mapper;

import com.tripgoapi.domain.model.User;
import com.tripgoapi.infrastructure.adapter.in.web.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserWebMapper {

    UserResponse toResponse(User user);
}
