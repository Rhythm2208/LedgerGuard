package com.rhythm.ledgerguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class LedgerGuardApplication {
    public static void main(String[] args) {
        SpringApplication.run(LedgerGuardApplication.class, args);
    }
}
