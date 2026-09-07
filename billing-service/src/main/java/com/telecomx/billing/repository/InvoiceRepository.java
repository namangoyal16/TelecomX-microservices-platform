package com.telecomx.billing.repository;

import com.telecomx.billing.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByCustomerIdOrderByGeneratedAtDesc(Long customerId);
}
