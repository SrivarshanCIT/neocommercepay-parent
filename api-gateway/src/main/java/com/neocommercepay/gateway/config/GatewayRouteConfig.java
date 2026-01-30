package com.neocommercepay.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .uri("http://user-service:8081"))
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .uri("http://product-service:8082"))
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .uri("http://order-service:8083"))
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri("http://payment-service:8084"))
                .build();
    }
}
