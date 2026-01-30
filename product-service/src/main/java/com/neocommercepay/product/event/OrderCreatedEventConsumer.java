package com.neocommercepay.product.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neocommercepay.common.constants.KafkaTopics;
import com.neocommercepay.common.event.OrderCreatedEvent;
import com.neocommercepay.product.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedEventConsumer {

    private final InventoryService inventoryService;
    private final RetryTemplate retryTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "product-service")
    public void consumeOrderCreated(String message, Acknowledgment acknowledgment) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
            log.info("Received OrderCreatedEvent for order: {}", event.getOrderId());

            retryTemplate.execute(context -> {
                event.getItems().forEach(item -> {
                    inventoryService.decrementStock(item.getProductId(), item.getQuantity());
                });
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
