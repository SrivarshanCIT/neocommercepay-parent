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
public class OrderUpdatedEvent {
    private Long orderId;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime timestamp;
    private String correlationId;
}
