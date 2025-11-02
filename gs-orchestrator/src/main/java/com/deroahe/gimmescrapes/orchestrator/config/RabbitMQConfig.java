package com.deroahe.gimmescrapes.orchestrator.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String SCRAPE_EXCHANGE = "scrape.exchange";
    public static final String EMAIL_EXCHANGE = "email.exchange";

    // Queue names
    public static final String SCRAPE_QUEUE = "scrape.queue";
    public static final String EMAIL_QUEUE = "email.queue";

    // Dead Letter Queue names
    public static final String SCRAPE_DLQ = "scrape.dlq";
    public static final String EMAIL_DLQ = "email.dlq";

    // Routing keys
    public static final String SCRAPE_ROUTING_KEY = "scrape";
    public static final String EMAIL_ROUTING_KEY = "email";

    // ==================== Scraping Exchange & Queues ====================

    @Bean
    public DirectExchange scrapeExchange() {
        return new DirectExchange(SCRAPE_EXCHANGE, true, false);
    }

    @Bean
    public Queue scrapeQueue() {
        return QueueBuilder.durable(SCRAPE_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", SCRAPE_DLQ)
                .build();
    }

    @Bean
    public Queue scrapeDLQ() {
        return new Queue(SCRAPE_DLQ, true);
    }

    @Bean
    public Binding scrapeBinding(@Qualifier("scrapeQueue") Queue scrapeQueue,
                                 @Qualifier("scrapeExchange") DirectExchange scrapeExchange) {
        return BindingBuilder.bind(scrapeQueue)
                .to(scrapeExchange)
                .with(SCRAPE_ROUTING_KEY);
    }

    // ==================== Email Exchange & Queues ====================

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE, true, false);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", EMAIL_DLQ)
                .build();
    }

    @Bean
    public Queue emailDLQ() {
        return new Queue(EMAIL_DLQ, true);
    }

    @Bean
    public Binding emailBinding(@Qualifier("emailQueue") Queue emailQueue,
                                @Qualifier("emailExchange") DirectExchange emailExchange) {
        return BindingBuilder.bind(emailQueue)
                .to(emailExchange)
                .with(EMAIL_ROUTING_KEY);
    }

    // ==================== Message Converter & RabbitTemplate ====================

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                        MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
