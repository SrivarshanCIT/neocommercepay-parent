package com.neocommercepay.payment.controller;

import com.neocommercepay.common.dto.payment.PaymentResponse;
import com.neocommercepay.payment.entity.Payment;
import com.neocommercepay.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Payment processing and management endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment", description = "Initiate payment for an order")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestParam Long orderId,
            @RequestParam BigDecimal amount,
            @RequestParam String idempotencyKey) {
        Payment payment = paymentService.initiatePayment(orderId, amount, idempotencyKey);
        PaymentResponse response = mapToResponse(payment);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/process")
    @Operation(summary = "Process payment", description = "Process an initiated payment")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable Long id) {
        Payment payment = paymentService.processPayment(id);
        PaymentResponse response = mapToResponse(payment);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get payment status", description = "Retrieve payment status by ID")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);
        PaymentResponse response = mapToResponse(payment);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund payment", description = "Refund a completed payment")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        Payment payment = paymentService.refundPayment(id, reason);
        PaymentResponse response = mapToResponse(payment);
        return ResponseEntity.ok(response);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
