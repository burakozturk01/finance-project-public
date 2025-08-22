package com.financesoftware.ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// TODO: Scheduling is intended for production (e.g., daily payouts). Disabled operationally for now;
// keep manual trigger via REST. When enabling, configure a daily cron on service logic.
@EnableScheduling
@EnableFeignClients
public class LedgerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LedgerServiceApplication.class, args);
    }
}
