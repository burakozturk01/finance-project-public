package com.financesoftware.transaction.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.financesoftware.transaction.entity.Transaction;

@Service
public class RabbitMQSender {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQSender.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendTransactionCreatedEvent(Transaction transaction) {
        try {
            logger.info("Starting to send transaction created event for transaction ID: {}", transaction.getId());
            logger.info("RabbitMQ Exchange: {}, Routing Key: {}", RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.CREATED_ROUTING_KEY);
            logger.info("Transaction details: merchantId={}, amount={}, currency={}, status={}",
                       transaction.getMerchantId(), transaction.getAmount(), transaction.getCurrency(), transaction.getStatus());

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.CREATED_ROUTING_KEY, transaction);

            logger.info("Transaction created event sent successfully for transaction ID: {}", transaction.getId());
        } catch (Exception e) {
            logger.error("Error sending transaction created event for transaction ID: {}: {}", transaction.getId(), e.getMessage(), e);
            logger.error("Full stack trace:", e);
        }
    }
}
