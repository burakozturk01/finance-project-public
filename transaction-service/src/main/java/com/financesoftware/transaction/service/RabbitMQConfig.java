package com.financesoftware.transaction.service;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RabbitMQConfig {

    public static final String VALIDATION_QUEUE_NAME = "transaction.validation.response";
    public static final String CREATED_QUEUE_NAME = "transaction.created.queue";
    public static final String EXCHANGE_NAME = "transaction-exchange";
    public static final String VALIDATION_ROUTING_KEY = "transaction.validated";
    public static final String CREATED_ROUTING_KEY = "transaction.created";

    @Bean
    Queue validationQueue() {
        return new Queue(VALIDATION_QUEUE_NAME, false);
    }

    @Bean
    Queue createdQueue() {
        return new Queue(CREATED_QUEUE_NAME, true);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    Binding validationBinding(@org.springframework.beans.factory.annotation.Qualifier("validationQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(VALIDATION_ROUTING_KEY);
    }

    @Bean
    Binding createdBinding(@org.springframework.beans.factory.annotation.Qualifier("createdQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(CREATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
