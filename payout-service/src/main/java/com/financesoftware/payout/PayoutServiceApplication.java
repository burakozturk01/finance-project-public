package com.financesoftware.payout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// TODO: Scheduling hooks are meant for production tasks (e.g., periodic payout checks). Disabled for now;
// enable when operationally needed with appropriate cron.
@EnableScheduling
@EnableFeignClients
public class PayoutServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayoutServiceApplication.class, args);
    }
}
