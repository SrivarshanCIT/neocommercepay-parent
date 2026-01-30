package com.neocommercepay.order.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neocommercepay.common.constants.KafkaTopics;
import com.neocommercepay.common.event.PaymentCompletedEvent;
import com.neocommercepay.common.event.PaymentFailedEvent;
import com.neocommercepay.order.entity.Order;
import com.neocommercepay.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = KafkaTopics.PAYMENT_COMPLETED, groupId = "order-service")
    public void consumePaymentCompleted(String message, Acknowledgment acknowledgment) {
        try {
            PaymentCompletedEvent event = objectMapper.readValue(message, PaymentCompletedEvent.class);
            log.info("Received PaymentCompletedEvent for order: {}", event.getOrderId());

            orderService.updateOrderStatus(event.getOrderId(), Order.OrderStatus.PAID);
            acknowledgment.acknowledge();

            log.info("Successfully processed PaymentCompletedEvent for order: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("Error processing PaymentCompletedEvent", e);
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "order-service")
    public void consumePaymentFailed(String message, Acknowledgment acknowledgment) {
        try {
            PaymentFailedEvent event = objectMapper.readValue(message, PaymentFailedEvent.class);
            log.info("Received PaymentFailedEvent for order: {}", event.getOrderId());

            orderService.cancelOrder(event.getOrderId(), "Payment failed: " + event.getReason());
            acknowledgment.acknowledge();

            log.info("Successfully processed PaymentFailedEvent for order: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("Error processing PaymentFailedEvent", e);
            acknowledgment.acknowledge();
        }
    }
}
