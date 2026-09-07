package com.telecomx.customer.repository;

import com.telecomx.customer.domain.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByCode(String code);
    List<Plan> findByActiveTrue();
}
