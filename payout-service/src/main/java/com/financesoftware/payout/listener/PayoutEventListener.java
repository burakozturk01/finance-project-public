package com.financesoftware.payout.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financesoftware.payout.config.RabbitMQConfig;
import com.financesoftware.payout.dto.PayoutDTO;
import com.financesoftware.payout.entity.Payout;
import com.financesoftware.payout.service.PayoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PayoutEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PayoutEventListener.class);

    private final ObjectMapper objectMapper;
    private final PayoutService payoutService;

    public PayoutEventListener(ObjectMapper objectMapper, PayoutService payoutService) {
        this.objectMapper = objectMapper;
        this.payoutService = payoutService;
    }

    @RabbitListener(queues = RabbitMQConfig.PAYOUT_QUEUE_NAME)
    public void receiveReadyToPayPayout(String message) {
        try {
            PayoutDTO payoutDTO = objectMapper.readValue(message, PayoutDTO.class);
            logger.info("Received READY_TO_PAY payout: {}", payoutDTO.getId());

            // Load existing payout by ID if present, else map from DTO
            Payout payout = payoutService.getPayoutById(payoutDTO.getId());
            if (payout == null) {
                payout = new Payout();
                payout.setId(payoutDTO.getId());
            }
            payout.setMerchantId(payoutDTO.getMerchantId());
            payout.setGrossAmount(payoutDTO.getGrossAmount());
            payout.setCommissionRate(payoutDTO.getCommissionRate());
            payout.setDebtAmount(payoutDTO.getDebtAmount());
            payout.setNetAmount(payoutDTO.getNetAmount());
            payout.setStatus(payoutDTO.getStatus());
            // createdAt is managed by JPA (@CreationTimestamp)
            payout.setProcessedAt(payoutDTO.getProcessedAt());

            payoutService.processPayout(payout);
        } catch (Exception e) {
            logger.error("Error processing received payout message: {}", e.getMessage(), e);
        }
    }
}
