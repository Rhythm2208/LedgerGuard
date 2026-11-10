package com.rhythm.ledgerguard.api.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record CreateRefundRequest(@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount){}
