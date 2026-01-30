package com.neocommercepay.gateway.filter;

import com.neocommercepay.common.util.CorrelationIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestResponseLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = CorrelationIdUtil.generate();
        CorrelationIdUtil.set(correlationId);

        long startTime = System.currentTimeMillis();

        logger.info("Incoming request: {} {} - Correlation ID: {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI(),
                correlationId);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Request completed: {} {} - Status: {} - Duration: {}ms - Correlation ID: {}",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI(),
                    exchange.getResponse().getStatusCode(),
                    duration,
                    correlationId);
            CorrelationIdUtil.clear();
        }));
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
