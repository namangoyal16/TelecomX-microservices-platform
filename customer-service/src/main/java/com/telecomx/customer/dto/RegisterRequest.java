package com.telecomx.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @NotBlank String phoneNumber,
        @NotBlank @Size(min = 8, message = "password must be at least 8 characters") String password
) {}
