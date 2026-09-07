package com.telecomx.usage.controller;

import com.telecomx.usage.domain.UsageType;
import com.telecomx.usage.dto.UsageIngestRequest;
import com.telecomx.usage.service.UsageIngestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * Dev/demo-only endpoint that simulates a burst of real-world usage events for a
 * customer, so the async usage -> billing -> notification pipeline can be exercised
 * end-to-end without wiring up an actual telecom switch.
 */
@RestController
@RequestMapping("/api/v1/usage/simulate")
@Tag(name = "Usage Simulator", description = "Dev-only: generate sample usage events")
public class UsageSimulatorController {

    private static final Random RANDOM = new Random();
    private final UsageIngestionService ingestionService;

    public UsageSimulatorController(UsageIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /** Simple JSON wrapper - every other endpoint in this platform returns JSON,
     *  so the simulator does too rather than a raw text body the frontend can't parse. */
    public record SimulationResult(int eventsGenerated, Long customerId, String msisdn) {}

    @PostMapping("/customers/{customerId}")
    public SimulationResult simulate(@PathVariable Long customerId, @RequestParam(defaultValue = "10") int count,
                            @RequestParam String msisdn) {
        List<UsageType> types = List.of(UsageType.CALL, UsageType.SMS, UsageType.DATA);
        for (int i = 0; i < count; i++) {
            UsageType type = types.get(RANDOM.nextInt(types.size()));
            var req = switch (type) {
                case CALL -> new UsageIngestRequest(customerId, msisdn, type, 30 + RANDOM.nextInt(600), null, "98" + RANDOM.nextInt(90000000));
                case SMS -> new UsageIngestRequest(customerId, msisdn, type, null, null, "98" + RANDOM.nextInt(90000000));
                case DATA -> new UsageIngestRequest(customerId, msisdn, type, null, RANDOM.nextDouble() * 500, null);
            };
            ingestionService.ingest(req);
        }
        return new SimulationResult(count, customerId, msisdn);
    }
}
