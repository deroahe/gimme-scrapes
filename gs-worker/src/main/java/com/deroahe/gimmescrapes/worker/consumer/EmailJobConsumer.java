package com.deroahe.gimmescrapes.worker.consumer;

import com.deroahe.gimmescrapes.commons.dto.EmailJobMessage;
import com.deroahe.gimmescrapes.commons.enums.EmailJobStatus;
import com.deroahe.gimmescrapes.commons.model.EmailJob;
import com.deroahe.gimmescrapes.worker.config.RabbitMQConfig;
import com.deroahe.gimmescrapes.worker.repository.EmailJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailJobConsumer {

    private final EmailJobRepository emailJobRepository;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void consumeEmailJob(EmailJobMessage message) {
        log.info("Received email job message: jobId={}, recipientEmail={}, emailType={}",
                message.getJobId(), message.getRecipientEmail(), message.getEmailType());

        try {
            // Find the email job
            EmailJob job = emailJobRepository.findById(message.getJobId())
                    .orElseThrow(() -> new RuntimeException("Email job not found: " + message.getJobId()));

            // TODO: Phase 8 - Actual email sending logic will be implemented here

            // For now, just mark as sent
            job.setStatus(EmailJobStatus.SENT);
            job.setSentAt(LocalDateTime.now());
            emailJobRepository.save(job);

            log.info("Email job completed successfully: jobId={}", message.getJobId());

        } catch (Exception e) {
            log.error("Error processing email job: jobId={}", message.getJobId(), e);

            // Update job status to FAILED
            emailJobRepository.findById(message.getJobId()).ifPresent(job -> {
                job.setStatus(EmailJobStatus.FAILED);
                job.setErrorMessage(e.getMessage());
                emailJobRepository.save(job);
            });

            throw new RuntimeException("Failed to process email job", e);
        }
    }
}
