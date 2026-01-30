package com.neocommercepay.payment.service;

import com.neocommercepay.payment.entity.Transaction;
import com.neocommercepay.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Transaction logTransaction(Long paymentId, Transaction.TransactionType type,
                                    BigDecimal amount, String transactionId) {
        Transaction transaction = Transaction.builder()
                .paymentId(paymentId)
                .transactionType(type)
                .amount(amount)
                .transactionId(transactionId)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction logged: {} for payment: {}", type, paymentId);
        return saved;
    }

    public List<Transaction> getTransactionsByPaymentId(Long paymentId) {
        return transactionRepository.findByPaymentId(paymentId);
    }
}
