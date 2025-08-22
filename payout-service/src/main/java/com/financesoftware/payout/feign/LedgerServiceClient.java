package com.financesoftware.payout.feign;

import com.financesoftware.payout.dto.LedgerDTO;
import com.financesoftware.common.enums.TransactionStatus;
import com.financesoftware.common.enums.PayoutStatus;
import com.financesoftware.payout.entity.Payout;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "ledger-service", url = "${LEDGER_SERVICE_URL}")
public interface LedgerServiceClient {

    @GetMapping("/api/ledgers/{id}")
    LedgerDTO getLedgerById(@PathVariable("id") UUID id);

    @PutMapping("/api/ledgers/transactions/set-status")
    String setTransactionStatus(@RequestBody List<UUID> transactionIds, @RequestParam("status") TransactionStatus status);

    @GetMapping("/api/ledgers/payouts-by-status")
    List<Payout> getPayoutsByStatus(@RequestParam("status") PayoutStatus status);
}
