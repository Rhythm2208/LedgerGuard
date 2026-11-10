package com.rhythm.ledgerguard.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refunds",
       uniqueConstraints = @UniqueConstraint(name = "uk_refund_tenant_idempotency",
           columnNames = {"tenant_id", "idempotency_key"}))
public class Refund {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false, length = 80) private String tenantId;
    @Column(name = "payment_id", nullable = false) private UUID paymentId;
    @Column(name = "idempotency_key", nullable = false, length = 120) private String idempotencyKey;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 3) private String currency;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected Refund() {}
    public Refund(String tenantId, UUID paymentId, String key, BigDecimal amount, String currency) {
        this.id = UUID.randomUUID(); this.tenantId = tenantId; this.paymentId = paymentId;
        this.idempotencyKey = key; this.amount = amount; this.currency = currency;
        this.createdAt = Instant.now();
    }
    public UUID getId() { return id; }
    public UUID getPaymentId() { return paymentId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
}
