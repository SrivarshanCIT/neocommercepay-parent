package com.neocommercepay.common.constants;

public final class SecurityConstants {
    public static final String JWT_SECRET_KEY = "JWT_SECRET";
    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final long JWT_EXPIRATION_MS = 86400000L;
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    private SecurityConstants() {
    }
}
