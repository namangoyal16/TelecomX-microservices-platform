package com.telecomx.customer.controller;

import com.telecomx.customer.dto.SubscribeRequest;
import com.telecomx.customer.dto.SubscriptionResponse;
import com.telecomx.customer.service.SubscriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/subscriptions")
@Tag(name = "Subscriptions", description = "Plan subscription requests")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> subscribe(@PathVariable Long customerId,
                                                            @Valid @RequestBody SubscribeRequest req) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(subscriptionService.subscribe(customerId, req));
    }

    @GetMapping
    public List<SubscriptionResponse> list(@PathVariable Long customerId) {
        return subscriptionService.getForCustomer(customerId);
    }
}
