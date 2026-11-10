package com.rhythm.ledgerguard.repository;

import com.rhythm.ledgerguard.domain.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByTenantIdAndIdempotencyKey(String tenantId, String key);
    Optional<Payment> findByIdAndTenantId(UUID id, String tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = :id and p.tenantId = :tenant")
    Optional<Payment> findForUpdate(@Param("id") UUID id, @Param("tenant") String tenant);
}
