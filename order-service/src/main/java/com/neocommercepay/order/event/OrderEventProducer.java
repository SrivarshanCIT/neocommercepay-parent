package com.neocommercepay.order.event;

import com.neocommercepay.common.constants.KafkaTopics;
import com.neocommercepay.common.event.OrderCancelledEvent;
import com.neocommercepay.common.event.OrderCreatedEvent;
import com.neocommercepay.common.event.OrderUpdatedEvent;
import com.neocommercepay.common.util.CorrelationIdUtil;
import com.neocommercepay.order.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .items(order.getItems().stream()
                        .map(item -> OrderCreatedEvent.OrderItem.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build())
                        .collect(Collectors.toList()))
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdUtil.get())
                .build();

        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, event);
        log.info("Published OrderCreatedEvent for order: {}", order.getId());
    }

    public void publishOrderUpdated(Order order, Order.OrderStatus oldStatus, Order.OrderStatus newStatus) {
        OrderUpdatedEvent event = OrderUpdatedEvent.builder()
                .orderId(order.getId())
                .oldStatus(oldStatus.name())
                .newStatus(newStatus.name())
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdUtil.get())
                .build();

        kafkaTemplate.send(KafkaTopics.ORDER_UPDATED, event);
        log.info("Published OrderUpdatedEvent for order: {}", order.getId());
    }

    public void publishOrderCancelled(Long orderId, Long userId, String reason) {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdUtil.get())
                .build();

        kafkaTemplate.send(KafkaTopics.ORDER_CANCELLED, event);
        log.info("Published OrderCancelledEvent for order: {}", orderId);
    }
}
