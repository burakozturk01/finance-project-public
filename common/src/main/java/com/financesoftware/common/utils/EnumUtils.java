package com.financesoftware.common.utils;

import com.financesoftware.common.enums.PayoutStatus;
import com.financesoftware.common.enums.TransactionStatus;

/**
 * Utility class for working with enums and providing allowable values for API documentation
 */
public class EnumUtils {

    /**
     * Get all PayoutStatus values as string array for Swagger allowableValues
     */
    public static String[] getPayoutStatusValues() {
        PayoutStatus[] values = PayoutStatus.values();
        String[] stringValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            stringValues[i] = values[i].name();
        }
        return stringValues;
    }

    /**
     * Get all TransactionStatus values as string array for Swagger allowableValues
     */
    public static String[] getTransactionStatusValues() {
        TransactionStatus[] values = TransactionStatus.values();
        String[] stringValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            stringValues[i] = values[i].name();
        }
        return stringValues;
    }

    /**
     * Get PayoutStatus values as comma-separated string for Swagger allowableValues
     */
    public static String getPayoutStatusValuesAsString() {
        return String.join(", ", getPayoutStatusValues());
    }

    /**
     * Get TransactionStatus values as comma-separated string for Swagger allowableValues
     */
    public static String getTransactionStatusValuesAsString() {
        return String.join(", ", getTransactionStatusValues());
    }
}
