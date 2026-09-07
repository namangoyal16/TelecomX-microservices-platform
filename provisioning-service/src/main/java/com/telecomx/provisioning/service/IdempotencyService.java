package com.telecomx.provisioning.service;

import com.telecomx.provisioning.domain.IdempotencyRecord;
import com.telecomx.provisioning.repository.IdempotencyRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.util.Optional;

/**
 * Generic idempotency guard used by any operation that must not be double-executed,
 * e.g. a provisioning activation triggered twice by a redelivered Kafka message, or a
 * client retrying a timed-out HTTP request with the same Idempotency-Key header.
 */
@Service
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;

    public IdempotencyService(IdempotencyRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<String> checkExisting(String idempotencyKey, String payload) {
        String hash = sha256(payload);
        return repository.findById(idempotencyKey)
                .filter(r -> r.getStatus().equals("COMPLETED"))
                .map(IdempotencyRecord::getResponseBody);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryBeginProcessing(String idempotencyKey, String payload) {
        if (repository.existsById(idempotencyKey)) {
            return false; // already seen -> caller should not reprocess
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
