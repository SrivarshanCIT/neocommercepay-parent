package com.neocommercepay.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDepletedEvent {
    private String productId;
    private String productName;
    private Integer currentStock;
    private LocalDateTime timestamp;
    private String correlationId;
}
