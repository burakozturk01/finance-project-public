package com.financesoftware.ledger.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financesoftware.ledger.config.RabbitMQConfig;
import com.financesoftware.ledger.entity.Payout;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PayoutEventSender {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public PayoutEventSender(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendReadyToPayPayout(Payout payout) {
        try {
            String payoutJson = objectMapper.writeValueAsString(payout);
            rabbitTemplate.convertAndSend(RabbitMQConfig.PAYOUT_EXCHANGE_NAME, RabbitMQConfig.PAYOUT_ROUTING_KEY, payoutJson);
            System.out.println("Sent READY_TO_PAY payout: " + payout.getId());
        } catch (JsonProcessingException e) {
            System.err.println("Error converting payout to JSON: " + e.getMessage());
        }
    }
}
