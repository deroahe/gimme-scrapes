package com.deroahe.gimmescrapes.commons.config;

/**
 * Common RabbitMQ constants shared across orchestrator and worker modules.
 * Defines exchange names, queue names, routing keys, and DLQ names.
 */
public final class RabbitMQConstants {

    private RabbitMQConstants() {
        // Prevent instantiation
    }

    // ==================== Exchange Names ====================

    public static final String SCRAPE_EXCHANGE = "scrape.exchange";
    public static final String EMAIL_EXCHANGE = "email.exchange";

    // ==================== Queue Names ====================

    public static final String SCRAPE_QUEUE = "scrape.queue";
    public static final String EMAIL_QUEUE = "email.queue";

    // ==================== Dead Letter Queue Names ====================

    public static final String SCRAPE_DLQ = "scrape.dlq";
    public static final String EMAIL_DLQ = "email.dlq";

    // ==================== Routing Keys ====================

    public static final String SCRAPE_ROUTING_KEY = "scrape";
    public static final String EMAIL_ROUTING_KEY = "email";
}
