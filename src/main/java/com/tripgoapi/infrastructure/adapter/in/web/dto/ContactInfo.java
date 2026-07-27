package com.tripgoapi.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ContactInfo(
        @NotBlank(message = "contact.name không được để trống")
        String name,

        @NotBlank(message = "contact.email không được để trống")
        @Email(message = "contact.email không hợp lệ")
        String email,

        @NotBlank(message = "contact.phone không được để trống")
        @Pattern(regexp = "^(?=.*\\d)[0-9+()\\-\\s]{8,20}$", message = "contact.phone không hợp lệ")
        String phone
) {
}
