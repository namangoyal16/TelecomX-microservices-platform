package com.telecomx.customer.dto;

public record AuthResponse(String token, long expiresInMs, Long customerId, String role) {}
