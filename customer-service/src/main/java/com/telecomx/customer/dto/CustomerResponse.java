package com.telecomx.customer.dto;

public record CustomerResponse(Long id, String fullName, String email, String phoneNumber, boolean kycVerified) {}
