package com.neocommercepay.payment.service;

import com.neocommercepay.payment.entity.AuditLog;
import com.neocommercepay.payment.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void logAction(String action, Long paymentId, String details) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .paymentId(paymentId)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit log created: {} for payment: {}", action, paymentId);
    }

    public List<AuditLog> getAuditLogsByPaymentId(Long paymentId) {
        return auditLogRepository.findByPaymentId(paymentId);
    }
}
