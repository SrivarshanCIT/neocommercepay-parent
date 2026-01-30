package com.neocommercepay.common.constants;

public final class KafkaTopics {
    public static final String USER_CREATED = "user.created";
    public static final String USER_UPDATED = "user.updated";
    public static final String USER_DELETED = "user.deleted";

    public static final String PRODUCT_CREATED = "product.created";
    public static final String PRODUCT_UPDATED = "product.updated";
    public static final String PRODUCT_DELETED = "product.deleted";
    public static final String INVENTORY_DEPLETED = "inventory.depleted";

    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_UPDATED = "order.updated";
    public static final String ORDER_CANCELLED = "order.cancelled";

    public static final String PAYMENT_INITIATED = "payment.initiated";
    public static final String PAYMENT_PROCESSING = "payment.processing";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String PAYMENT_REFUNDED = "payment.refunded";

    public static final String ORDER_SERVICE_DLQ = "order-service-dlq";
    public static final String PAYMENT_SERVICE_DLQ = "payment-service-dlq";
    public static final String PRODUCT_SERVICE_DLQ = "product-service-dlq";

    private KafkaTopics() {
    }
}
