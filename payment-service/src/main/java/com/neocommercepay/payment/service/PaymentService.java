package com.neocommercepay.payment.service;

import com.neocommercepay.common.exception.BusinessException;
import com.neocommercepay.common.exception.NotFoundException;
import com.neocommercepay.payment.entity.Payment;
import com.neocommercepay.payment.entity.Transaction;
import com.neocommercepay.payment.event.PaymentEventProducer;
import com.neocommercepay.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionService transactionService;
    private final AuditService auditService;
    private final PaymentEventProducer paymentEventProducer;
    private final Random random = new Random();

    @Transactional
    public Payment initiatePayment(Long orderId, BigDecimal amount, String idempotencyKey) {
        if (paymentRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new BusinessException("Duplicate payment request detected");
        }

        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .status(Payment.PaymentStatus.INITIATED)
                .idempotencyKey(idempotencyKey)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        auditService.logAction("PAYMENT_INITIATED", savedPayment.getId(),
                "Payment initiated for order: " + orderId);
        paymentEventProducer.publishPaymentInitiated(savedPayment);

        log.info("Payment initiated: {}", savedPayment.getId());
        return savedPayment;
    }

    @Transactional
    public Payment processPayment(Long paymentId) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != Payment.PaymentStatus.INITIATED) {
            throw new BusinessException("Payment cannot be processed in current status: " + payment.getStatus());
        }

        payment.setStatus(Payment.PaymentStatus.PROCESSING);
        paymentRepository.save(payment);
        paymentEventProducer.publishPaymentProcessing(payment);

        auditService.logAction("PAYMENT_PROCESSING", payment.getId(),
                "Payment processing started");

        boolean success = mockPaymentProcessor(payment);

        if (success) {
            String transactionId = UUID.randomUUID().toString();
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setTransactionId(transactionId);
            Payment completedPayment = paymentRepository.save(payment);

            transactionService.logTransaction(payment.getId(), Transaction.TransactionType.CHARGE,
                    payment.getAmount(), transactionId);

            auditService.logAction("PAYMENT_COMPLETED", payment.getId(),
                    "Payment completed successfully. Transaction ID: " + transactionId);

            paymentEventProducer.publishPaymentCompleted(completedPayment, transactionId);
            log.info("Payment completed: {}", payment.getId());

            return completedPayment;
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            Payment failedPayment = paymentRepository.save(payment);

            auditService.logAction("PAYMENT_FAILED", payment.getId(),
                    "Payment processing failed");

            paymentEventProducer.publishPaymentFailed(failedPayment, "Payment processor declined");
            log.error("Payment failed: {}", payment.getId());

            return failedPayment;
        }
    }

    @Transactional
    public Payment refundPayment(Long paymentId, String reason) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new BusinessException("Only completed payments can be refunded");
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        Payment refundedPayment = paymentRepository.save(payment);

        String refundTransactionId = UUID.randomUUID().toString();
        transactionService.logTransaction(payment.getId(), Transaction.TransactionType.REFUND,
                payment.getAmount(), refundTransactionId);

        auditService.logAction("PAYMENT_REFUNDED", payment.getId(),
                "Payment refunded. Reason: " + reason);

        paymentEventProducer.publishPaymentRefunded(refundedPayment, reason);
        log.info("Payment refunded: {}", payment.getId());

        return refundedPayment;
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found with id: " + id));
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found for order: " + orderId));
    }

    private boolean mockPaymentProcessor(Payment payment) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return random.nextDouble() > 0.1;
    }
}
