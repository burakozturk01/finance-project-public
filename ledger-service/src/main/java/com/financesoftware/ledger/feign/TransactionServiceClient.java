package com.financesoftware.ledger.feign;

import com.financesoftware.ledger.entity.Transaction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "transaction-service", url = "${transaction.service.url}")
public interface TransactionServiceClient {

    @GetMapping("/api/transactions/get")
    Transaction getTransactionById(@RequestParam("id") UUID id);

    @GetMapping("/api/transactions/get-by-status")
    Page<Transaction> getTransactionsByStatus(@RequestParam("status") String status,
                                            @RequestParam("page") int page,
                                            @RequestParam("size") int size);

    @PutMapping("/api/transactions/{id}/status")
    void updateTransactionStatus(@PathVariable("id") UUID id, @RequestParam("status") String status);

    @PostMapping("/api/transactions/pending")
    void setTransactionToPending(@RequestParam("id") String id);

    @PostMapping("/api/transactions/pending-merchant")
    void setAllTransactionsToPendingForMerchant(@RequestParam("merchantId") String merchantId);
}
