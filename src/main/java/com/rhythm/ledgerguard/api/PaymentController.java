package com.rhythm.ledgerguard.api;
import com.rhythm.ledgerguard.api.dto.*; import com.rhythm.ledgerguard.security.TenantPrincipal;
import com.rhythm.ledgerguard.service.LedgerGuardService; import jakarta.validation.Valid;
import org.springframework.http.*; import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1")
public class PaymentController {
 private final LedgerGuardService service; public PaymentController(LedgerGuardService s){service=s;}
 @PostMapping("/payments") public ResponseEntity<?> create(@AuthenticationPrincipal TenantPrincipal p,
 @RequestHeader(value="Idempotency-Key",required=false)String key,@Valid @RequestBody CreatePaymentRequest r){
  return ResponseEntity.status(HttpStatus.CREATED).body(service.createPayment(p.tenantId(),key,r));}
 @GetMapping("/payments/{id}") public Object get(@AuthenticationPrincipal TenantPrincipal p,@PathVariable UUID id){return service.getPayment(p.tenantId(),id);}
 @PostMapping("/payments/{id}/refunds") public ResponseEntity<?> refund(@AuthenticationPrincipal TenantPrincipal p,@PathVariable UUID id,
 @RequestHeader(value="Idempotency-Key",required=false)String key,@Valid @RequestBody CreateRefundRequest r){
  return ResponseEntity.status(HttpStatus.CREATED).body(service.refund(p.tenantId(),id,key,r));}
 @GetMapping("/payments/{id}/ledger") public Object ledger(@AuthenticationPrincipal TenantPrincipal p,@PathVariable UUID id){return service.ledger(p.tenantId(),id);}
}
