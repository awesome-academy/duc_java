package com.tripgoapi.infrastructure.adapter.in.web.dto;

public record UserResponse(Long id, String fullName, String email, String phone, String role) {
}
