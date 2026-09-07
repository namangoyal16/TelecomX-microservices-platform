package com.telecomx.provisioning.controller;

import com.telecomx.provisioning.dto.ProvisioningResponse;
import com.telecomx.provisioning.service.ProvisioningService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/provisioning")
@Tag(name = "Provisioning", description = "Subscriber service activation status")
public class ProvisioningController {

    private final ProvisioningService provisioningService;

    public ProvisioningController(ProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @GetMapping("/customers/{customerId}")
    public ProvisioningResponse getStatus(@PathVariable Long customerId) {
        return ProvisioningResponse.from(provisioningService.getByCustomer(customerId));
    }

    @PostMapping("/admin/customers/{customerId}/suspend")
    public void suspend(@PathVariable Long customerId) {
        provisioningService.suspend(customerId);
    }

    @PostMapping("/admin/customers/{customerId}/reactivate")
    public void reactivate(@PathVariable Long customerId) {
        provisioningService.reactivate(customerId);
    }
}
