package com.rhythm.ledgerguard.repository;

import com.rhythm.ledgerguard.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    List<LedgerEntry> findByTenantIdAndPaymentIdOrderByCreatedAtAsc(String tenantId, UUID paymentId);
}
