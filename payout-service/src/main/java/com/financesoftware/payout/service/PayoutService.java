package com.financesoftware.payout.service;

import com.financesoftware.payout.entity.Payout;
import com.financesoftware.payout.entity.PayoutAttemptHistory;
import com.financesoftware.payout.feign.LedgerServiceClient;
import com.financesoftware.payout.feign.MerchantServiceClient;
import com.financesoftware.payout.repository.PayoutRepository;
import com.financesoftware.payout.repository.PayoutAttemptHistoryRepository;
import com.financesoftware.common.enums.PayoutStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PayoutService {

    private static final Logger logger = LoggerFactory.getLogger(PayoutService.class);

    @Autowired
    private PayoutRepository payoutRepository;

    @Autowired
    private PayoutAttemptHistoryRepository payoutAttemptHistoryRepository;

    @Autowired
    private BankService bankService;

    @Autowired
    private LedgerServiceClient ledgerServiceClient;

    @Autowired
    private MerchantServiceClient merchantServiceClient;

    @Transactional
    public Payout processPayout(Payout payout) {
        logger.info("Starting payout processing for payout ID: {}", payout.getId());

        try {
            // Update payout status to PROCESSING
            payout.setStatus(PayoutStatus.PROCESSING);
            payout.setProcessedAt(LocalDateTime.now());
            payout = payoutRepository.save(payout);

            // Attempt payment through bank service
            boolean paymentSuccessful = bankService.makePayment(payout);

            LocalDateTime attemptTime = LocalDateTime.now();
            PayoutAttemptHistory attemptHistory;

            if (paymentSuccessful) {
                // Payment successful
                payout.setStatus(PayoutStatus.PAID);
                attemptHistory = new PayoutAttemptHistory(payout, attemptTime, PayoutStatus.PAID, "Payment successful");

                logger.info("Payment successful for payout ID: {}", payout.getId());

                // Update transaction statuses to PAID in ledger service
                // Note: We need to get the transaction IDs for this payout from the ledger service
                // For now, we'll skip this step as we don't have a direct way to get transaction IDs from payout
                // In a real implementation, this would be handled differently
                // ledgerServiceClient.setTransactionStatus(getTransactionIds(payout), TransactionStatus.PAID);

            } else {
                // Payment failed
                payout.setStatus(PayoutStatus.FAILED);
                String failureReason = bankService.getFailureReason(payout);
                attemptHistory = new PayoutAttemptHistory(payout, attemptTime, PayoutStatus.FAILED, failureReason);

                logger.warn("Payment failed for payout ID: {} - Reason: {}", payout.getId(), failureReason);
            }

            // Save attempt history
            payoutAttemptHistoryRepository.save(attemptHistory);

            // Save updated payout
            payout = payoutRepository.save(payout);

            logger.info("Payout processing completed for payout ID: {} with status: {}",
                       payout.getId(), payout.getStatus());

            return payout;

        } catch (Exception e) {
            logger.error("Error processing payout ID: {}", payout.getId(), e);

            // Update payout status to FAILED and create attempt history
            payout.setStatus(PayoutStatus.FAILED);
            PayoutAttemptHistory attemptHistory = new PayoutAttemptHistory(
                payout, LocalDateTime.now(), PayoutStatus.FAILED, "Processing error: " + e.getMessage());

            payoutAttemptHistoryRepository.save(attemptHistory);
            payout = payoutRepository.save(payout);

            throw new RuntimeException("Failed to process payout", e);
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

    /**
     * Fetches READY_TO_PAY payouts from Ledger Service and processes them.
     * This provides a REST alternative to the RabbitMQ listener functionality.
     *
     * @return List of processed payouts
     */
    @Transactional
    public List<Payout> fetchAndProcessReadyToPayPayouts() {
        logger.info("Fetching READY_TO_PAY payouts from Ledger Service");

        try {
            // Fetch READY_TO_PAY payouts from Ledger Service
            List<Payout> readyToPayPayouts = ledgerServiceClient.getPayoutsByStatus(PayoutStatus.READY_TO_PAY);

            logger.info("Found {} READY_TO_PAY payouts from Ledger Service", readyToPayPayouts.size());

            // Process each payout (similar to what the listener does)
            for (Payout ledgerPayout : readyToPayPayouts) {
                try {
                    // Check if payout already exists in our database
                    Payout existingPayout = getPayoutById(ledgerPayout.getId());

                    Payout payoutToProcess;
                    if (existingPayout == null) {
                        // Create new payout from ledger data
                        payoutToProcess = new Payout();
                        payoutToProcess.setId(ledgerPayout.getId());
                    } else {
                        // Use existing payout
                        payoutToProcess = existingPayout;
                    }

                    // Update payout with data from ledger service
                    payoutToProcess.setMerchantId(ledgerPayout.getMerchantId());
                    payoutToProcess.setGrossAmount(ledgerPayout.getGrossAmount());
                    payoutToProcess.setCommissionRate(ledgerPayout.getCommissionRate());
                    payoutToProcess.setDebtAmount(ledgerPayout.getDebtAmount());
                    payoutToProcess.setNetAmount(ledgerPayout.getNetAmount());
                    payoutToProcess.setStatus(ledgerPayout.getStatus());
                    payoutToProcess.setProcessedAt(ledgerPayout.getProcessedAt());

                    // Process the payout
                    processPayout(payoutToProcess);

                    logger.info("Successfully processed payout: {}", payoutToProcess.getId());

                } catch (Exception e) {
                    logger.error("Error processing payout {}: {}", ledgerPayout.getId(), e.getMessage(), e);
                    // Continue with other payouts even if one fails
                }
            }

            return readyToPayPayouts;

        } catch (Exception e) {
            logger.error("Error fetching READY_TO_PAY payouts from Ledger Service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch and process READY_TO_PAY payouts", e);
        }
    }
}
