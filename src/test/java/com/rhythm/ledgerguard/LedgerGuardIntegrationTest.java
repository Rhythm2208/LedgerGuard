package com.rhythm.ledgerguard;
import com.rhythm.ledgerguard.api.dto.*; import com.rhythm.ledgerguard.domain.Payment;
import com.rhythm.ledgerguard.service.*; import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource; import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container; import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal; import java.util.concurrent.*; import static org.junit.jupiter.api.Assertions.*;
@Testcontainers @SpringBootTest
class LedgerGuardIntegrationTest {
 @Container static final PostgreSQLContainer<?> db=new PostgreSQLContainer<>("postgres:16-alpine");
 @DynamicPropertySource static void props(DynamicPropertyRegistry r){r.add("spring.datasource.url",db::getJdbcUrl);r.add("spring.datasource.username",db::getUsername);r.add("spring.datasource.password",db::getPassword);}
 @Autowired LedgerGuardService service; @Autowired JdbcTemplate jdbc;
 @BeforeEach void clean(){jdbc.execute("TRUNCATE ledger_entries,refunds,payments CASCADE");}
 @Test void duplicatePaymentRequestReturnsOriginal(){
  var req=new CreatePaymentRequest(new BigDecimal("100.00"),"INR");
  Payment a=service.createPayment("tenant-a","key-1",req), b=service.createPayment("tenant-a","key-1",req);
  assertEquals(a.getId(),b.getId()); assertEquals(1,jdbc.queryForObject("select count(*) from payments",Integer.class));
 }
 @Test void idempotencyKeyCannotBeReusedForDifferentPayload(){
  service.createPayment("tenant-a","key-2",new CreatePaymentRequest(new BigDecimal("100"),"INR"));
  assertThrows(DomainException.class,()->service.createPayment("tenant-a","key-2",new CreatePaymentRequest(new BigDecimal("101"),"INR")));
 }
 @Test void concurrentRefundsCannotOverRefund() throws Exception {
  Payment p=service.createPayment("tenant-a","pay-3",new CreatePaymentRequest(new BigDecimal("100"),"INR"));
  ExecutorService pool=Executors.newFixedThreadPool(2); CountDownLatch ready=new CountDownLatch(2),go=new CountDownLatch(1);
  Callable<Boolean> one=()->attempt(p,"refund-1",ready,go),two=()->attempt(p,"refund-2",ready,go);
  Future<Boolean> f1=pool.submit(one),f2=pool.submit(two); assertTrue(ready.await(5,TimeUnit.SECONDS));go.countDown();
  boolean a=f1.get(15,TimeUnit.SECONDS),b=f2.get(15,TimeUnit.SECONDS);pool.shutdownNow();
  assertNotEquals(a,b); assertEquals(new BigDecimal("100.00"),jdbc.queryForObject("select refunded_amount from payments where id=?",BigDecimal.class,p.getId()));
  assertEquals(1,jdbc.queryForObject("select count(*) from refunds",Integer.class));
 }
 private boolean attempt(Payment p,String key,CountDownLatch ready,CountDownLatch go){
  ready.countDown();try{go.await();service.refund("tenant-a",p.getId(),key,new CreateRefundRequest(new BigDecimal("100")));return true;}
  catch(DomainException e){return false;}catch(InterruptedException e){Thread.currentThread().interrupt();return false;}
 }
}
