package com.financesoftware.common.enums;

/**
 * Centralized PayoutStatus enum used across all services
 */
public enum PayoutStatus {
    NEW,           // Initial state when payout is created
    READY_TO_PAY,  // Ready for payment processing
    PROCESSING,    // Payment is being processed
    INSUFFICIENT,  // Insufficient funds after debt deduction
    PAID,          // Successfully paid to merchant
    FAILED,        // Payment failed
    CANCELLED      // Manually cancelled
}
