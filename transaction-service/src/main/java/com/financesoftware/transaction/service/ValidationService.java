package com.financesoftware.transaction.service;

import com.financesoftware.common.enums.TransactionStatus;
import com.financesoftware.transaction.entity.Transaction;
import com.financesoftware.transaction.feign.MerchantServiceClient;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class ValidationService {

    @Autowired
    private MerchantServiceClient merchantServiceClient;

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ValidationService.class);

    private static final Set<String> VALID_CURRENCIES = new HashSet<>(Arrays.asList("USD", "EUR", "GBP", "TRY"));

    public boolean validateTransaction(Transaction transaction) {
        if (transaction == null) {
            return false;
        }

        return validateMerchantId(transaction.getMerchantId()) &&
                validateAmount(transaction.getAmount()) &&
                validateCurrency(transaction.getCurrency()) &&
                validateCardScheme(transaction.getCardScheme()) &&
                validateStatus(transaction.getStatus()) &&
                validateCreatedAt(transaction.getCreatedAt());
    }

    private boolean validateMerchantId(UUID merchantId) {
        if (merchantId == null) {
            return false;
        }
        try {
            return merchantServiceClient.validateMerchant(merchantId);
        } catch (Exception e) {
            logger.error("Error validating merchant ID: {}", e.getMessage());
            return false;
        }
    }

    private boolean validateAmount(BigDecimal amount) {
        return amount != null &&
                amount.compareTo(BigDecimal.ZERO) > 0 &&
                amount.scale() <= 2;
    }

    private boolean validateCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            return false;
        }
        String trimmedCurrency = currency.trim().toUpperCase();
        return trimmedCurrency.length() == 3 && VALID_CURRENCIES.contains(trimmedCurrency);
    }

    private boolean validateCardScheme(Transaction.CardScheme cardScheme) {
        return cardScheme != null;
    }

    private boolean validateStatus(TransactionStatus status) {
        return status != null;
    }

    private boolean validateCreatedAt(LocalDateTime createdAt) {
        return createdAt != null && !createdAt.isAfter(LocalDateTime.now());
    }
}
