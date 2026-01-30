package com.neocommercepay.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessingEvent {
    private Long paymentId;
    private Long orderId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String correlationId;
}
