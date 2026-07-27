package com.tripgoapi.application.port.in;

public record RegisterUserCommand(String fullName, String email, String password, String phone) {
}
