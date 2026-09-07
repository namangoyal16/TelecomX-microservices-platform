package com.telecomx.customer.controller;

import com.telecomx.customer.domain.Plan;
import com.telecomx.customer.repository.PlanRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
@Tag(name = "Plans", description = "Public plan catalog")
public class PlanController {

    private final PlanRepository planRepository;

    public PlanController(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @GetMapping
    public List<Plan> listActivePlans() {
        return planRepository.findByActiveTrue();
    }
}
