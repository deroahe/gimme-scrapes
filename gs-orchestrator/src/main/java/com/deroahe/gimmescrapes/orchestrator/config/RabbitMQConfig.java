package com.deroahe.gimmescrapes.orchestrator.config;

import com.deroahe.gimmescrapes.commons.config.RabbitMQConstants;
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

    // ==================== Scraping Exchange & Queues ====================

    @Bean
    public DirectExchange scrapeExchange() {
        return new DirectExchange(RabbitMQConstants.SCRAPE_EXCHANGE, true, false);
    }

    @Bean
    public Queue scrapeQueue() {
        return QueueBuilder.durable(RabbitMQConstants.SCRAPE_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.SCRAPE_DLQ)
                .build();
    }

    @Bean
    public Queue scrapeDLQ() {
        return new Queue(RabbitMQConstants.SCRAPE_DLQ, true);
    }

    @Bean
    public Binding scrapeBinding(@Qualifier("scrapeQueue") Queue scrapeQueue,
                                 @Qualifier("scrapeExchange") DirectExchange scrapeExchange) {
        return BindingBuilder.bind(scrapeQueue)
                .to(scrapeExchange)
                .with(RabbitMQConstants.SCRAPE_ROUTING_KEY);
    }

    // ==================== Email Exchange & Queues ====================

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(RabbitMQConstants.EMAIL_EXCHANGE, true, false);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(RabbitMQConstants.EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.EMAIL_DLQ)
                .build();
    }

    @Bean
    public Queue emailDLQ() {
        return new Queue(RabbitMQConstants.EMAIL_DLQ, true);
    }

    @Bean
    public Binding emailBinding(@Qualifier("emailQueue") Queue emailQueue,
                                @Qualifier("emailExchange") DirectExchange emailExchange) {
        return BindingBuilder.bind(emailQueue)
                .to(emailExchange)
                .with(RabbitMQConstants.EMAIL_ROUTING_KEY);
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
