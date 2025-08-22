package com.financesoftware.common.enums;

/**
 * Centralized TransactionStatus enum used across all services
 */
public enum TransactionStatus {
    RECEIVED,   // Initial state when transaction is received
    PENDING,    // Under validation
    VALIDATED,  // Validated and ready for payout
    PAID,       // Successfully paid out
    FAILED      // Payment failed
}
