package com.financesoftware.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financesoftware.common.enums.TransactionStatus;
import com.financesoftware.transaction.entity.Transaction;
import com.financesoftware.transaction.repository.TransactionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ValidationResultListener {

    private static final Logger logger = LoggerFactory.getLogger(ValidationResultListener.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.VALIDATION_QUEUE_NAME)
    public void handleValidationResult(String message) {
        try {
            logger.info("Received validation result: {}", message);

            Map<String, Object> transactionMap = objectMapper.readValue(message, Map.class);
            String transactionIdStr = (String) transactionMap.get("id");
            UUID transactionId = UUID.fromString(transactionIdStr);
            String status = (String) transactionMap.get("status");

            logger.info("Processing validation result for transaction ID: {} with status: {}", transactionId, status);

            Optional<Transaction> transactionOptional = transactionRepository.findById(transactionId);
            if (transactionOptional.isPresent()) {
                Transaction transaction = transactionOptional.get();
                TransactionStatus oldStatus = transaction.getStatus();
                transaction.setStatus(TransactionStatus.valueOf(status));
                transactionRepository.save(transaction);

                logger.info("Transaction {} status updated from {} to {}", transactionId, oldStatus, status);
            } else {
                logger.warn("Transaction not found with ID: {}", transactionId);
            }
        } catch (Exception e) {
            logger.error("Error processing validation result: {}", e.getMessage(), e);
        }
    }
}
