package com.neocommercepay.payment.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neocommercepay.common.constants.KafkaTopics;
import com.neocommercepay.common.event.OrderCreatedEvent;
import com.neocommercepay.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedEventConsumer {

    private final PaymentService paymentService;
    private final RetryTemplate retryTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "payment-service")
    public void consumeOrderCreated(String message, Acknowledgment acknowledgment) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
            log.info("Received OrderCreatedEvent for order: {}", event.getOrderId());

            retryTemplate.execute(context -> {
                String idempotencyKey = UUID.randomUUID().toString();
                paymentService.initiatePayment(event.getOrderId(), event.getTotalAmount(), idempotencyKey);
                return null;
            });

            acknowledgment.acknowledge();
            log.info("Successfully processed OrderCreatedEvent for order: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent", e);
            acknowledgment.acknowledge();
        }
    }
}
