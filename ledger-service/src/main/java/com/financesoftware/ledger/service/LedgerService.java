package com.financesoftware.ledger.service;

import com.financesoftware.ledger.dto.MerchantDTO;
import com.financesoftware.ledger.entity.Payout;
import com.financesoftware.ledger.entity.PayoutTransaction;
import com.financesoftware.ledger.entity.Transaction;
import com.financesoftware.common.enums.TransactionStatus;
import com.financesoftware.common.enums.PayoutStatus;
import com.financesoftware.ledger.feign.MerchantServiceClient;
import com.financesoftware.ledger.feign.TransactionServiceClient;
import com.financesoftware.ledger.repository.PayoutRepository;
import com.financesoftware.ledger.repository.PayoutTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LedgerService {

    @Autowired
    private PayoutRepository payoutRepository;

    @Autowired
    private PayoutTransactionRepository payoutTransactionRepository;

    @Autowired
    private TransactionServiceClient transactionServiceClient;

    @Autowired
    private MerchantServiceClient merchantServiceClient;

    @Autowired
    private PayoutEventSender payoutEventSender;

    // TODO: Do this asyncronously via RabbitMQ
    // TODO: In production, schedule this to run once daily (e.g., 00:00) using @Scheduled(cron = "0 0 0 * * *").
    // For now, this remains manual and is exposed via REST in LedgerController.
    @Transactional
    public void processPayouts() {
        int page = 0;
        int size = 1000;
        boolean hasMoreTransactions = true;

        while (hasMoreTransactions) {
            // Fetch VALIDATED transactions with pagination
            Page<Transaction> transactionPage = transactionServiceClient.getTransactionsByStatus(
                TransactionStatus.VALIDATED.name(), page, size);

            List<Transaction> transactions = transactionPage.getContent();

            if (transactions.isEmpty()) {
                hasMoreTransactions = false;
                continue;
            }

            // Filter out already processed transactions
            List<Transaction> unprocessedTransactions = transactions.stream()
                .filter(transaction -> !isTransactionProcessed(transaction.getId()))
                .collect(Collectors.toList());

            if (!unprocessedTransactions.isEmpty()) {
                // Group transactions by merchant
                Map<UUID, List<Transaction>> transactionsByMerchant = unprocessedTransactions.stream()
                    .collect(Collectors.groupingBy(Transaction::getMerchantId));

                // Process each merchant's transactions
                for (Map.Entry<UUID, List<Transaction>> entry : transactionsByMerchant.entrySet()) {
                    UUID merchantId = entry.getKey();
                    List<Transaction> merchantTransactions = entry.getValue();

                    processPayoutForMerchant(merchantId, merchantTransactions);
                }
            }

            hasMoreTransactions = transactionPage.hasNext();
            page++;
        }
    }

    private boolean isTransactionProcessed(UUID transactionId) {
        return payoutTransactionRepository.findByTransactionId(transactionId).isPresent();
    }

    @Transactional
    private void processPayoutForMerchant(UUID merchantId, List<Transaction> transactions) {
        // Fetch merchant details
        MerchantDTO merchant = merchantServiceClient.getMerchantById(merchantId);

        // Calculate gross amount
        BigDecimal grossAmount = transactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate commission
        BigDecimal commissionAmount = grossAmount
            .multiply(merchant.getCommissionPercentage())
            .divide(BigDecimal.valueOf(100));

        // Calculate net amount after commission and debt
        BigDecimal netAmount = grossAmount
            .subtract(commissionAmount)
            .subtract(merchant.getDebt());

        // Create payout
        Payout payout = new Payout();
        payout.setMerchantId(merchantId);
        payout.setGrossAmount(grossAmount);
        payout.setCommissionRate(merchant.getCommissionPercentage());
        payout.setDebtAmount(merchant.getDebt());
        payout.setNetAmount(netAmount);

        // Determine payout status
        // Note: Debt management is now handled by the PayoutService after successful payment
        if (netAmount.compareTo(BigDecimal.ZERO) > 0) {
            payout.setStatus(PayoutStatus.READY_TO_PAY);
        } else {
            payout.setStatus(PayoutStatus.INSUFFICIENT);
        }

        payout.setProcessedAt(LocalDateTime.now());

        // Save payout
        Payout savedPayout = payoutRepository.save(payout);

        // If payout is READY_TO_PAY, send it to the Payout Service via RabbitMQ
        if (savedPayout.getStatus() == PayoutStatus.READY_TO_PAY) {
            payoutEventSender.sendReadyToPayPayout(savedPayout);
        }

        // Create payout-transaction relationships
        for (Transaction transaction : transactions) {
            PayoutTransaction payoutTransaction = new PayoutTransaction();
            payoutTransaction.setPayoutId(savedPayout.getId());
            payoutTransaction.setTransactionId(transaction.getId());
            payoutTransactionRepository.save(payoutTransaction);

            // Update transaction status to PENDING using existing endpoint
            transactionServiceClient.setTransactionToPending(transaction.getId().toString());
        }
    }

    public List<Payout> getPayoutsByStatus(PayoutStatus status) {
        if (status == null) {
            return payoutRepository.findAll();
        }
        return payoutRepository.findByStatus(status);
    }

    public List<Payout> getPayoutsByMerchantAndStatus(UUID merchantId, PayoutStatus status) {
        if (status == null) {
            return payoutRepository.findByMerchantId(merchantId);
        }
        return payoutRepository.findByMerchantIdAndStatus(merchantId, status);
    }

    public Payout getPayoutById(UUID payoutId) {
        return payoutRepository.findById(payoutId).orElse(null);
    }

    public List<Transaction> getTransactionsForPayout(UUID payoutId) {
        List<PayoutTransaction> payoutTransactions = payoutTransactionRepository.findByPayoutId(payoutId);
        return payoutTransactions.stream()
            .map(pt -> {
                // Fetch transaction details from transaction service
                return transactionServiceClient.getTransactionById(pt.getTransactionId());
            })
            .collect(Collectors.toList());
    }

    public List<Payout> getPayouts() {
        return payoutRepository.findAll();
    }

    public List<Payout> getPayoutsByMerchant(UUID merchantId) {
        return payoutRepository.findByMerchantId(merchantId);
    }

    @Transactional
    public int setTransactionStatus(List<UUID> transactionIds, TransactionStatus status) {
        int updatedCount = 0;
        for (UUID transactionId : transactionIds) {
            try {
                // Update transaction status using the transaction service client
                transactionServiceClient.updateTransactionStatus(transactionId, status.name());
                updatedCount++;
            } catch (Exception e) {
                // Log error but continue with other transactions
                System.err.println("Failed to update transaction " + transactionId + " to status " + status + ": " + e.getMessage());
            }
        }
        return updatedCount;
    }
}
