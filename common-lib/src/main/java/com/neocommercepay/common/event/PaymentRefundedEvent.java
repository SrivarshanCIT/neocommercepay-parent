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
public class PaymentRefundedEvent {
    private Long paymentId;
    private Long orderId;
    private BigDecimal amount;
    private String reason;
    private LocalDateTime timestamp;
    private String correlationId;
}
