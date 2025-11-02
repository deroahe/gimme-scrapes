package com.deroahe.gimmescrapes.orchestrator.service;

import com.deroahe.gimmescrapes.commons.dto.EmailJobMessage;
import com.deroahe.gimmescrapes.commons.dto.ScrapeJobMessage;
import com.deroahe.gimmescrapes.orchestrator.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessagePublisherService {

    private final RabbitTemplate rabbitTemplate;

    public void publishScrapeJob(ScrapeJobMessage message) {
        log.info("Publishing scrape job message: jobId={}, sourceId={}, sourceName={}",
                message.getJobId(), message.getSourceId(), message.getSourceName());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SCRAPE_EXCHANGE,
                RabbitMQConfig.SCRAPE_ROUTING_KEY,
                message
        );

        log.debug("Scrape job message published successfully");
    }

    public void publishEmailJob(EmailJobMessage message) {
        log.info("Publishing email job message: jobId={}, recipientEmail={}, emailType={}",
                message.getJobId(), message.getRecipientEmail(), message.getEmailType());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                message
        );

        log.debug("Email job message published successfully");
    }
}
