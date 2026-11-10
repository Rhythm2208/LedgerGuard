package com.rhythm.ledgerguard.api.dto;

import com.rhythm.ledgerguard.domain.Refund;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RefundResponse(UUID id, UUID paymentId, BigDecimal amount,
                             String currency, Instant createdAt) {
    public static RefundResponse from(Refund r) {
        return new RefundResponse(r.getId(), r.getPaymentId(), r.getAmount(),
                                  r.getCurrency(), r.getCreatedAt());
    }
}
