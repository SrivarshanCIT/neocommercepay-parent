package com.neocommercepay.product.service;

import com.neocommercepay.common.exception.BusinessException;
import com.neocommercepay.common.exception.NotFoundException;
import com.neocommercepay.product.document.Inventory;
import com.neocommercepay.product.event.ProductEventProducer;
import com.neocommercepay.product.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductEventProducer productEventProducer;

    public Inventory createInventory(String productId, Integer quantity) {
        Inventory inventory = Inventory.builder()
                .productId(productId)
                .quantity(quantity)
                .reservedQuantity(0)
                .availableQuantity(quantity)
                .lastUpdated(LocalDateTime.now())
                .build();

        return inventoryRepository.save(inventory);
    }

    public Inventory getInventoryByProductId(String productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Inventory not found for product: " + productId));
    }

    public void decrementStock(String productId, Integer quantity) {
        Inventory inventory = getInventoryByProductId(productId);

        if (inventory.getAvailableQuantity() < quantity) {
            throw new BusinessException("Insufficient stock for product: " + productId);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setLastUpdated(LocalDateTime.now());

        inventoryRepository.save(inventory);

        if (inventory.getAvailableQuantity() <= 5) {
            productEventProducer.publishInventoryDepleted(productId, inventory.getAvailableQuantity());
        }

        log.info("Stock decremented for product {}: -{}", productId, quantity);
    }

    public void incrementStock(String productId, Integer quantity) {
        Inventory inventory = getInventoryByProductId(productId);

        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventory.setLastUpdated(LocalDateTime.now());

        inventoryRepository.save(inventory);
        log.info("Stock incremented for product {}: +{}", productId, quantity);
    }
}
