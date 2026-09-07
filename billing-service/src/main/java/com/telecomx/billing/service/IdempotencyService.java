package com.telecomx.billing.service;

import com.telecomx.billing.domain.IdempotencyRecord;
import com.telecomx.billing.repository.IdempotencyRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.util.Optional;

/**
 * Guards billing's two most dangerous double-execution scenarios:
 *  1. A client retries a timed-out POST /invoices/{id}/pay request -> must not
 *     charge the customer twice.
 *  2. Kafka redelivers a usage.recorded / subscriber.provisioned message -> must
 *     not double-count usage charges or re-create a billing profile.
 */
@Service
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;

    public IdempotencyService(IdempotencyRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<String> getCompletedResponse(String idempotencyKey) {
        return repository.findById(idempotencyKey)
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .map(IdempotencyRecord::getResponseBody);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryBeginProcessing(String idempotencyKey, String payload) {
        if (repository.existsById(idempotencyKey)) {
            return false;
        }
        repository.save(IdempotencyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .requestHash(sha256(payload))
                .status("IN_PROGRESS")
                .build());
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(String idempotencyKey, String responseBody) {
        repository.findById(idempotencyKey).ifPresent(r -> {
            r.setStatus("COMPLETED");
            r.setResponseBody(responseBody);
            repository.save(r);
        });
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
