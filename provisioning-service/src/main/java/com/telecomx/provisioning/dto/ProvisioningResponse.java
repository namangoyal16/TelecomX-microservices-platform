package com.telecomx.provisioning.dto;

import com.telecomx.provisioning.domain.ProvisioningRecord;

public record ProvisioningResponse(Long customerId, String msisdn, String planCode, String status) {
    public static ProvisioningResponse from(ProvisioningRecord r) {
        return new ProvisioningResponse(r.getCustomerId(), r.getMsisdn(), r.getPlanCode(), r.getStatus().name());
    }
}
