package com.rhythm.ledgerguard.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments",
       uniqueConstraints = @UniqueConstraint(name = "uk_payment_tenant_idempotency",
           columnNames = {"tenant_id", "idempotency_key"}))
public class Payment {
    @Id
    private UUID id;
    @Column(name = "tenant_id", nullable = false, length = 80)
    private String tenantId;
    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;
    @Column(name = "refunded_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal refundedAmount;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Payment() {}

    public Payment(String tenantId, String idempotencyKey, BigDecimal amount, String currency) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.currency = currency.toUpperCase();
        this.status = PaymentStatus.SUCCEEDED;
        this.refundedAmount = BigDecimal.ZERO.setScale(2);
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public BigDecimal getRefundedAmount() { return refundedAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public BigDecimal getRemainingAmount() { return amount.subtract(refundedAmount); }

    public void applyRefund(BigDecimal refundAmount) {
        this.refundedAmount = this.refundedAmount.add(refundAmount);
        this.status = this.refundedAmount.compareTo(amount) == 0
                ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED;
    }
}
