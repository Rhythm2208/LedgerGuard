package com.rhythm.ledgerguard.api;
import com.rhythm.ledgerguard.service.DomainException; import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.Map;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(DomainException.class) ResponseEntity<?> handle(DomainException e){
  return ResponseEntity.status(e.getStatus()).body(Map.of("timestamp",Instant.now().toString(),"status",e.getStatus().value(),"message",e.getMessage()));
 }
}
