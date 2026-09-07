package com.telecomx.usage.controller;

import com.telecomx.usage.domain.UsageEvent;
import com.telecomx.usage.dto.UsageIngestRequest;
import com.telecomx.usage.repository.UsageEventRepository;
import com.telecomx.usage.service.UsageIngestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usage")
@Tag(name = "Usage/CDR", description = "Ingest and query call/SMS/data usage events")
public class UsageController {

    private final UsageIngestionService ingestionService;
    private final UsageEventRepository repository;

    public UsageController(UsageIngestionService ingestionService, UsageEventRepository repository) {
        this.ingestionService = ingestionService;
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<UsageEvent> ingest(@Valid @RequestBody UsageIngestRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingestionService.ingest(req));
    }

    @GetMapping("/customers/{customerId}")
    public List<UsageEvent> getForCustomer(@PathVariable Long customerId) {
        return repository.findByCustomerIdOrderByRecordedAtDesc(customerId);
    }
}
