package com.rhythm.ledgerguard.reconciliation;
import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.scheduling.annotation.Scheduled;
import io.micrometer.core.instrument.MeterRegistry; import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
@Component
public class ReconciliationService {
 private static final Logger log=LoggerFactory.getLogger(ReconciliationService.class);
 private final JdbcTemplate jdbc; private final MeterRegistry metrics;
 public ReconciliationService(JdbcTemplate jdbc,MeterRegistry metrics){this.jdbc=jdbc;this.metrics=metrics;}
 @Scheduled(fixedDelayString="${app.reconciliation.delay-ms:60000}")
 public void reconcile(){
  Integer n=jdbc.queryForObject("select count(*) from payments p where p.refunded_amount<0 or p.refunded_amount>p.amount or p.refunded_amount<>coalesce((select sum(r.amount) from refunds r where r.payment_id=p.id and r.tenant_id=p.tenant_id),0)",Integer.class);
  int count=n==null?0:n; metrics.gauge("ledgerguard.reconciliation.mismatches",count);
  if(count>0)log.error("Reconciliation found {} mismatch(es)",count); else log.info("Reconciliation complete: no mismatches");
 }
}
