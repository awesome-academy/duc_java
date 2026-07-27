package com.tripgoapi.domain.model;

public record User(Long id, String fullName, String email, String phone, Role role) {
}
