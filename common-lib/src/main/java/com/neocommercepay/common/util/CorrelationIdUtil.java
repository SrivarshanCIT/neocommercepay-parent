package com.neocommercepay.common.util;

import com.neocommercepay.common.constants.SecurityConstants;
import org.slf4j.MDC;

import java.util.UUID;

public final class CorrelationIdUtil {

    private CorrelationIdUtil() {
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static void set(String correlationId) {
        MDC.put(SecurityConstants.CORRELATION_ID_HEADER, correlationId);
    }

    public static String get() {
        String correlationId = MDC.get(SecurityConstants.CORRELATION_ID_HEADER);
        if (correlationId == null) {
            correlationId = generate();
            set(correlationId);
        }
        return correlationId;
    }

    public static void clear() {
        MDC.remove(SecurityConstants.CORRELATION_ID_HEADER);
    }
}
