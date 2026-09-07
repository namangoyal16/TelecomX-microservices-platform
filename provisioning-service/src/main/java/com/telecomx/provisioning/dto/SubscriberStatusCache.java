package com.telecomx.provisioning.dto;

public record SubscriberStatusCache(Long customerId, String msisdn, String planCode, String status) {}
