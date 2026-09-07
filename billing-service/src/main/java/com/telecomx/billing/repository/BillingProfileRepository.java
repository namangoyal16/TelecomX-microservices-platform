package com.telecomx.billing.repository;

import com.telecomx.billing.domain.BillingProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface BillingProfileRepository extends JpaRepository<BillingProfile, Long> {

    // Pessimistic lock: usage.recorded events for the same customer can arrive
    // concurrently (multiple partitions/consumers); locking the row while we
    // increment unbilled_usage_charge prevents lost updates.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BillingProfile b where b.customerId = :customerId")
    Optional<BillingProfile> findWithLockByCustomerId(@Param("customerId") Long customerId);

    Optional<BillingProfile> findByCustomerId(Long customerId);
}
