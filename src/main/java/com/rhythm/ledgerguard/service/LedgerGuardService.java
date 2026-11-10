package com.rhythm.ledgerguard.service;
import com.rhythm.ledgerguard.api.dto.*; import com.rhythm.ledgerguard.domain.*;
import com.rhythm.ledgerguard.repository.*; import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.dao.DataIntegrityViolationException; import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.util.*; import java.math.BigDecimal;
@Service
public class LedgerGuardService {
 private final PaymentRepository payments; private final RefundRepository refunds; private final LedgerEntryRepository ledger; private final MeterRegistry metrics;
 public LedgerGuardService(PaymentRepository p,RefundRepository r,LedgerEntryRepository l,MeterRegistry m){payments=p;refunds=r;ledger=l;metrics=m;}
 @Transactional public Payment createPayment(String tenant,String key,CreatePaymentRequest r){
  key(key); var existing=payments.findByTenantIdAndIdempotencyKey(tenant,key);
  if(existing.isPresent()){Payment p=existing.get();if(p.getAmount().compareTo(r.amount())!=0||!p.getCurrency().equalsIgnoreCase(r.currency()))throw error(HttpStatus.CONFLICT,"Idempotency key reused with different payload");metrics.counter("ledgerguard.idempotency.replay").increment();return p;}
  Payment p=new Payment(tenant,key,r.amount(),r.currency());
  try{payments.saveAndFlush(p);ledger.save(new LedgerEntry(tenant,p.getId(),p.getId(),"PAYMENT_CAPTURED",p.getAmount(),p.getCurrency()));metrics.counter("ledgerguard.payments.created").increment();return p;}
  catch(DataIntegrityViolationException e){throw error(HttpStatus.CONFLICT,"Concurrent duplicate key; retry using the same key");}
 }
 @Transactional(readOnly=true) public Payment getPayment(String tenant,UUID id){return payments.findByIdAndTenantId(id,tenant).orElseThrow(()->error(HttpStatus.NOT_FOUND,"Payment not found"));}
 @Transactional public Refund refund(String tenant,UUID id,String key,CreateRefundRequest r){
  key(key);var prior=refunds.findByTenantIdAndIdempotencyKey(tenant,key);
  if(prior.isPresent()){Refund x=prior.get();if(!x.getPaymentId().equals(id)||x.getAmount().compareTo(r.amount())!=0)throw error(HttpStatus.CONFLICT,"Refund key reused with different payload");return x;}
  Payment p=payments.findForUpdate(id,tenant).orElseThrow(()->error(HttpStatus.NOT_FOUND,"Payment not found"));
  if(r.amount().compareTo(p.getRemainingAmount())>0){metrics.counter("ledgerguard.refunds.rejected").increment();throw error(HttpStatus.CONFLICT,"Refund exceeds remaining amount");}
  p.applyRefund(r.amount());Refund x=refunds.saveAndFlush(new Refund(tenant,id,key,r.amount(),p.getCurrency()));
  ledger.save(new LedgerEntry(tenant,id,x.getId(),"REFUND_ISSUED",r.amount(),p.getCurrency()));metrics.counter("ledgerguard.refunds.created").increment();return x;
 }
 @Transactional(readOnly=true) public List<LedgerEntry> ledger(String tenant,UUID id){getPayment(tenant,id);return ledger.findByTenantIdAndPaymentIdOrderByCreatedAtAsc(tenant,id);}
 private void key(String k){if(k==null||k.isBlank()||k.length()>120)throw error(HttpStatus.BAD_REQUEST,"Idempotency-Key required (max 120 chars)");}
 private DomainException error(HttpStatus s,String m){return new DomainException(s,m);}
}
