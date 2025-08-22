package com.financesoftware.merchant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financesoftware.merchant.entity.Merchant;
import com.financesoftware.merchant.repository.MerchantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionEventListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionEventListener.class);

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleTransactionCreatedEvent(Map<String, Object> transactionMap) {
        try {
            logger.info("Received transaction event: {}", transactionMap);

            String merchantIdStr = (String) transactionMap.get("merchantId");
            UUID merchantId = UUID.fromString(merchantIdStr);

            logger.info("Processing transaction for merchant ID: {}", merchantId);

            Optional<Merchant> merchant = merchantRepository.findById(merchantId);

            boolean isValid = merchant.isPresent();
            String newStatus = isValid ? "VALIDATED" : "FAILED";

            logger.info("Merchant validation result: {} -> Status: {}", isValid, newStatus);

            transactionMap.put("status", newStatus);

            String responseMessage = objectMapper.writeValueAsString(transactionMap);

            logger.info("Sending validation response: {}", responseMessage);

            rabbitTemplate.convertAndSend("transaction-exchange", "transaction.validated", responseMessage);

            logger.info("Validation response sent successfully");
        } catch (Exception e) {
            logger.error("Error processing transaction event: {}", e.getMessage(), e);
        }
    }
}
