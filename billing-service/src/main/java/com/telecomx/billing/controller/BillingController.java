package com.telecomx.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecomx.billing.domain.Invoice;
import com.telecomx.billing.domain.Payment;
import com.telecomx.billing.dto.*;
import com.telecomx.billing.service.BillingService;
import com.telecomx.billing.service.IdempotencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Billing", description = "Invoices and payments")
public class BillingController {

    private final BillingService billingService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public BillingController(BillingService billingService, IdempotencyService idempotencyService, ObjectMapper objectMapper) {
        this.billingService = billingService;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/customers/{customerId}/invoices")
    public List<InvoiceResponse> getInvoices(@PathVariable Long customerId) {
        return billingService.getInvoicesForCustomer(customerId).stream().map(InvoiceResponse::from).toList();
    }

    @PostMapping("/customers/{customerId}/invoices/generate")
    @Operation(summary = "Generate an invoice from plan charge + accrued unbilled usage")
    public ResponseEntity<InvoiceResponse> generateInvoice(@PathVariable Long customerId) {
        Invoice invoice = billingService.generateInvoice(customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(InvoiceResponse.from(invoice));
    }

    @PostMapping("/customers/{customerId}/invoices/{invoiceId}/pay")
    @Operation(summary = "Pay an invoice. Idempotency-Key header required to safely retry on timeout.")
    public ResponseEntity<PaymentResponse> pay(
            @PathVariable Long customerId,
            @PathVariable Long invoiceId,
            @RequestHeader("Idempotency-Key") @Parameter(description = "Client-generated UUID; retry-safe") String idempotencyKey,
            @Valid @RequestBody(required = false) PaymentRequest req) throws Exception {

        boolean simulateFailure = req != null && req.simulateFailure();
        String payloadForHash = customerId + ":" + invoiceId + ":" + simulateFailure;

        // 1. Have we already fully processed this exact idempotency key? Return the
        //    cached result instead of re-running the payment logic.
        Optional<String> cached = idempotencyService.getCompletedResponse(idempotencyKey);
        if (cached.isPresent()) {
            PaymentResponse response = objectMapper.readValue(cached.get(), PaymentResponse.class);
            return respond(response);
        }

        // 2. First time seeing this key (or a stale IN_PROGRESS row from a crashed
        //    attempt) -> claim it before doing any real work.
        boolean claimed = idempotencyService.tryBeginProcessing(idempotencyKey, payloadForHash);
        if (!claimed) {
            // Another request with the same key is already in flight (race) - ask the
            // client to retry shortly rather than double-processing.
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Payment payment = billingService.payInvoice(customerId, invoiceId, simulateFailure);
        PaymentResponse response = PaymentResponse.from(payment);
        idempotencyService.complete(idempotencyKey, objectMapper.writeValueAsString(response));

        return respond(response);
    }

    private ResponseEntity<PaymentResponse> respond(PaymentResponse response) {
        HttpStatus status = "SUCCESS".equals(response.status()) ? HttpStatus.OK : HttpStatus.PAYMENT_REQUIRED;
        return ResponseEntity.status(status).body(response);
    }
}
