package com.financesoftware.payout.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PAYOUT_EXCHANGE_NAME = "payout-exchange";
    public static final String PAYOUT_QUEUE_NAME = "payout-queue";
    public static final String PAYOUT_ROUTING_KEY = "payout.readytopay";

    @Bean
    public TopicExchange payoutExchange() {
        return new TopicExchange(PAYOUT_EXCHANGE_NAME);
    }

    @Bean
    public Queue payoutQueue() {
        return new Queue(PAYOUT_QUEUE_NAME, true);
    }

    @Bean
    public Binding bindingPayoutQueue(Queue payoutQueue, TopicExchange payoutExchange) {
        return BindingBuilder.bind(payoutQueue).to(payoutExchange).with(PAYOUT_ROUTING_KEY);
    }
}
