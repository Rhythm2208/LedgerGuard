package com.rhythm.ledgerguard.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false, length = 80) private String tenantId;
    @Column(name = "payment_id", nullable = false) private UUID paymentId;
    @Column(name = "reference_id", nullable = false) private UUID referenceId;
    @Column(nullable = false, length = 20) private String entryType;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 3) private String currency;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected LedgerEntry() {}
    public UUID getId() { return id; }
    public String getTenantId() { return tenantId; }
    public UUID getPaymentId() { return paymentId; }
    public UUID getReferenceId() { return referenceId; }
    public String getEntryType() { return entryType; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }

    public LedgerEntry(String tenantId, UUID paymentId, UUID referenceId,
                       String entryType, BigDecimal amount, String currency) {
        this.id = UUID.randomUUID(); this.tenantId = tenantId; this.paymentId = paymentId;
        this.referenceId = referenceId; this.entryType = entryType;
        this.amount = amount; this.currency = currency; this.createdAt = Instant.now();
    }
}
