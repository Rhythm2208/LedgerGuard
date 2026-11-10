package com.rhythm.ledgerguard.api.dto;

import com.rhythm.ledgerguard.domain.Payment;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(UUID id, String tenantId, BigDecimal amount,
    BigDecimal refundedAmount, BigDecimal remainingAmount, String currency,
    String status, Instant createdAt) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getTenantId(), p.getAmount(),
            p.getRefundedAmount(), p.getRemainingAmount(), p.getCurrency(),
            p.getStatus().name(), p.getCreatedAt());
    }
}
