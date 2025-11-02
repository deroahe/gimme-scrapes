package com.deroahe.gimmescrapes.worker.consumer;

import com.deroahe.gimmescrapes.commons.dto.ScrapeJobMessage;
import com.deroahe.gimmescrapes.commons.enums.ScrapingJobStatus;
import com.deroahe.gimmescrapes.commons.model.ScrapingJob;
import com.deroahe.gimmescrapes.worker.config.RabbitMQConfig;
import com.deroahe.gimmescrapes.worker.repository.ScrapingJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScrapeJobConsumer {

    private final ScrapingJobRepository scrapingJobRepository;

    @RabbitListener(queues = RabbitMQConfig.SCRAPE_QUEUE)
    public void consumeScrapeJob(ScrapeJobMessage message) {
        log.info("Received scrape job message: jobId={}, sourceId={}, sourceName={}",
                message.getJobId(), message.getSourceId(), message.getSourceName());

        try {
            // Update job status to RUNNING
            ScrapingJob job = scrapingJobRepository.findById(message.getJobId())
                    .orElseThrow(() -> new RuntimeException("Scraping job not found: " + message.getJobId()));

            job.setStatus(ScrapingJobStatus.RUNNING);
            job.setStartedAt(LocalDateTime.now());
            scrapingJobRepository.save(job);

            log.info("Updated scraping job status to RUNNING: jobId={}", message.getJobId());

            // TODO: Phase 4 - Actual scraping logic will be implemented here

            // For now, just mark as completed
            job.setStatus(ScrapingJobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            scrapingJobRepository.save(job);

            log.info("Scraping job completed successfully: jobId={}", message.getJobId());

        } catch (Exception e) {
            log.error("Error processing scrape job: jobId={}", message.getJobId(), e);

            // Update job status to FAILED
            scrapingJobRepository.findById(message.getJobId()).ifPresent(job -> {
                job.setStatus(ScrapingJobStatus.FAILED);
                job.setCompletedAt(LocalDateTime.now());
                job.setErrorMessage(e.getMessage());
                scrapingJobRepository.save(job);
            });

            throw new RuntimeException("Failed to process scrape job", e);
        }
    }
}
