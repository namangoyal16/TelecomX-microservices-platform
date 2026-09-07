package com.telecomx.billing.integration;

import com.telecomx.billing.domain.BillingProfile;
import com.telecomx.billing.domain.Invoice;
import com.telecomx.billing.domain.InvoiceStatus;
import com.telecomx.billing.domain.PaymentStatus;
import com.telecomx.billing.repository.BillingProfileRepository;
import com.telecomx.billing.repository.InvoiceRepository;
import com.telecomx.billing.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The single most important test in this repo: proves that retrying the SAME
 * payment request (same Idempotency-Key, as a client would do after a timeout)
 * against a REAL Postgres database results in exactly ONE payment row and the
 * invoice is charged exactly once - not twice.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BillingIdempotencyIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("billingdb_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:1");
    }

    @Autowired MockMvc mockMvc;
    @Autowired BillingProfileRepository profileRepository;
    @Autowired InvoiceRepository invoiceRepository;
    @Autowired PaymentRepository paymentRepository;

    @Test
    void payingTwiceWithSameIdempotencyKey_resultsInExactlyOnePayment() throws Exception {
        // Arrange: a billing profile + a pending invoice, inserted directly (bypassing
        // the async Kafka path, since we're isolating the idempotency behavior here).
        profileRepository.save(BillingProfile.builder()
                .customerId(500L).msisdn("9123400001").planCode("PLUS_5G")
                .monthlyPrice(new BigDecimal("599.00")).unbilledUsageCharge(BigDecimal.ZERO).build());

        Invoice invoice = invoiceRepository.save(Invoice.builder()
                .customerId(500L).planCharge(new BigDecimal("599.00")).usageCharge(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("599.00")).status(InvoiceStatus.PENDING).build());

        String idempotencyKey = "test-key-" + java.util.UUID.randomUUID();

        String body = "{\"simulateFailure\": false}";

        // Act: fire the exact same request twice with the exact same Idempotency-Key,
        // simulating a client retry after a perceived timeout.
        mockMvc.perform(post("/api/v1/customers/500/invoices/" + invoice.getId() + "/pay")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType("application/json").content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/customers/500/invoices/" + invoice.getId() + "/pay")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType("application/json").content(body))
                .andExpect(status().isOk());

        // Assert: only ONE payment row exists for this invoice, despite two HTTP calls.
        List<com.telecomx.billing.domain.Payment> payments = paymentRepository.findAll().stream()
                .filter(p -> p.getInvoiceId().equals(invoice.getId()))
                .toList();

        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        Invoice reloaded = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }
}
