package com.neocommercepay.product.event;

import com.neocommercepay.common.constants.KafkaTopics;
import com.neocommercepay.common.event.*;
import com.neocommercepay.common.util.CorrelationIdUtil;
import com.neocommercepay.product.document.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishProductCreated(Product product) {
        ProductCreatedEvent event = ProductCreatedEvent.builder()
                .productId(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategoryName())
                .stockQuantity(product.getStockQuantity())
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdUtil.get())
                .build();

        kafkaTemplate.send(KafkaTopics.PRODUCT_CREATED, event);
        log.info("Published ProductCreatedEvent for product: {}", product.getName());
    }

    public void publishProductUpdated(Product product) {
        ProductUpdatedEvent event = ProductUpdatedEvent.builder()
                .productId(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategoryName())
                .stockQuantity(product.getStockQuantity())
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdUtil.get())
                .build();

        kafkaTemplate.send(KafkaTopics.PRODUCT_UPDATED, event);
        log.info("Published ProductUpdatedEvent for product: {}", product.getName());
    }

    public void publishProductDeleted(String productId) {
        ProductDeletedEvent event = ProductDeletedEvent.builder()
                .productId(productId)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdUtil.get())
                .build();

        kafkaTemplate.send(KafkaTopics.PRODUCT_DELETED, event);
        log.info("Published ProductDeletedEvent for product: {}", productId);
    }

    public void publishInventoryDepleted(String productId, Integer currentStock) {
        InventoryDepletedEvent event = InventoryDepletedEvent.builder()
                .productId(productId)
                .productName("Product-" + productId)
                .currentStock(currentStock)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdUtil.get())
                .build();

        kafkaTemplate.send(KafkaTopics.INVENTORY_DEPLETED, event);
        log.warn("Published InventoryDepletedEvent for product: {} (stock: {})", productId, currentStock);
    }
}
