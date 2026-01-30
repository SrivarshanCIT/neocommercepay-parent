package com.neocommercepay.payment.event;

import com.neocommercepay.common.constants.KafkaTopics;
import com.neocommercepay.common.event.*;
import com.neocommercepay.common.util.CorrelationIdUtil;
import com.neocommercepay.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentInitiated(Payment payment) {
        PaymentInitiatedEvent event = PaymentInitiatedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdUtil.get())
                .build();

        kafkaTemplate.send(KafkaTopics.PAYMENT_INITIATED, event);
        log.info("Published PaymentInitiatedEvent for payment: {}", payment.getId());
    }

    public void publishPaymentProcessing(Payment payment) {
        PaymentProcessingEvent event = PaymentProcessingEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdUtil.get())
                .build();

        kafkaTemplate.send(KafkaTopics.PAYMENT_PROCESSING, event);
        log.info("Published PaymentProcessingEvent for payment: {}", payment.getId());
    }

    public void publishPaymentCompleted(Payment payment, String transactionId) {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .transactionId(transactionId)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdUtil.get())
                .build();

        kafkaTemplate.send(KafkaTopics.PAYMENT_COMPLETED, event);
        log.info("Published PaymentCompletedEvent for payment: {}", payment.getId());
    }

    public void publishPaymentFailed(Payment payment, String reason) {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdUtil.get())
                .build();

        kafkaTemplate.send(KafkaTopics.PAYMENT_FAILED, event);
        log.error("Published PaymentFailedEvent for payment: {}", payment.getId());
    }

    public void publishPaymentRefunded(Payment payment, String reason) {
        PaymentRefundedEvent event = PaymentRefundedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdUtil.get())
                .build();

        kafkaTemplate.send(KafkaTopics.PAYMENT_REFUNDED, event);
        log.info("Published PaymentRefundedEvent for payment: {}", payment.getId());
    }
}
