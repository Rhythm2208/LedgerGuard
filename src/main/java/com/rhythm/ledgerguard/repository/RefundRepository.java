package com.rhythm.ledgerguard.repository;

import com.rhythm.ledgerguard.domain.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RefundRepository extends JpaRepository<Refund, UUID> {
    Optional<Refund> findByTenantIdAndIdempotencyKey(String tenantId, String key);
    List<Refund> findByTenantIdAndPaymentIdOrderByCreatedAtAsc(String tenantId, UUID paymentId);
}
