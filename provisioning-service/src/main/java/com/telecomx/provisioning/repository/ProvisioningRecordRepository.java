package com.telecomx.provisioning.repository;

import com.telecomx.provisioning.domain.ProvisioningRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProvisioningRecordRepository extends JpaRepository<ProvisioningRecord, Long> {
    Optional<ProvisioningRecord> findBySubscriptionId(Long subscriptionId);
    Optional<ProvisioningRecord> findByCustomerId(Long customerId);
}
