package com.telecomx.customer.dto;

import jakarta.validation.constraints.NotBlank;

public record SubscribeRequest(@NotBlank String planCode) {}
