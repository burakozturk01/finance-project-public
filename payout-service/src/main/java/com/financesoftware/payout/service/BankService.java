package com.financesoftware.payout.service;

import com.financesoftware.payout.entity.Payout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service to simulate interactions with a bank API for payment processing
 */
@Service
public class BankService {

    private static final Logger logger = LoggerFactory.getLogger(BankService.class);
    private static final String FAIL_IBAN = "FAIL";

    /**
     * Simulates a payment to the bank
     * @param payout the payout to process
     * @return true if payment is successful, false if it fails
     */
    public boolean makePayment(Payout payout) {
        logger.info("Processing payment for payout ID: {} with merchant ID: {}",
                   payout.getId(), payout.getMerchantId());

        try {
            // Simulate processing time
            Thread.sleep(1000);

            // Check if this is a test failure case
            // In a real implementation, you would get the merchant's IBAN from the merchant service
            // For testing purposes, we'll simulate a failure if merchant ID contains "fail" (case-insensitive)
            String merchantIdStr = payout.getMerchantId().toString().toLowerCase();
            if (merchantIdStr.contains("fail") || FAIL_IBAN.equalsIgnoreCase(merchantIdStr)) {
                logger.warn("Payment failed for payout ID: {} - Test failure triggered", payout.getId());
                return false;
            }

            // Simulate random failures (5% chance)
            if (Math.random() < 0.05) {
                logger.warn("Payment failed for payout ID: {} - Random failure", payout.getId());
                return false;
            }

            logger.info("Payment successful for payout ID: {}", payout.getId());
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Payment processing interrupted for payout ID: {}", payout.getId(), e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during payment processing for payout ID: {}", payout.getId(), e);
            return false;
        }
    }

    /**
     * Gets the reason for payment failure based on the payout
     * @param payout the payout that failed
     * @return the failure reason
     */
    public String getFailureReason(Payout payout) {
        String merchantIdStr = payout.getMerchantId().toString().toLowerCase();
        if (merchantIdStr.contains("fail") || FAIL_IBAN.equalsIgnoreCase(merchantIdStr)) {
            return "Test failure - Invalid IBAN";
        }
        return "Bank processing error";
    }
}
