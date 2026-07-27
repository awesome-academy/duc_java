package com.tripgoapi.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContactInfo(
        @NotBlank(message = "contact.name không được để trống")
        @Size(max = 255, message = "contact.name tối đa 255 ký tự")
        String name,

        @NotBlank(message = "contact.email không được để trống")
        @Email(message = "contact.email không hợp lệ")
        @Size(max = 255, message = "contact.email tối đa 255 ký tự")
        String email,

        @NotBlank(message = "contact.phone không được để trống")
        @Pattern(regexp = "^(?=.*\\d)[0-9+()\\-\\s]{8,20}$", message = "contact.phone không hợp lệ")
        String phone
) {
}
