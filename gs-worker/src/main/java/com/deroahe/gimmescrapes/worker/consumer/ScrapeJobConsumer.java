package com.deroahe.gimmescrapes.worker.consumer;

import com.deroahe.gimmescrapes.commons.config.RabbitMQConstants;
import com.deroahe.gimmescrapes.commons.dto.ListingDto;
import com.deroahe.gimmescrapes.commons.dto.ScrapeJobMessage;
import com.deroahe.gimmescrapes.commons.enums.ScrapingJobStatus;
import com.deroahe.gimmescrapes.commons.exception.ScrapingException;
import com.deroahe.gimmescrapes.commons.model.Listing;
import com.deroahe.gimmescrapes.commons.model.ScrapingJob;
import com.deroahe.gimmescrapes.commons.model.Source;
import com.deroahe.gimmescrapes.commons.repository.ScrapingJobRepository;
import com.deroahe.gimmescrapes.commons.repository.SourceRepository;
import com.deroahe.gimmescrapes.worker.service.ListingService;
import com.deroahe.gimmescrapes.worker.service.ListingService.UpsertResult;
import com.deroahe.gimmescrapes.worker.service.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Consumer for scraping job messages from RabbitMQ.
 * Orchestrates the scraping process and tracks job status.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScrapeJobConsumer {

    private final ScraperService scraperService;
    private final ListingService listingService;
    private final SourceRepository sourceRepository;
    private final ScrapingJobRepository scrapingJobRepository;

    /**
     * Listens to the scrape queue and processes scraping jobs.
     *
     * @param message the scrape job message
     */
    @RabbitListener(queues = RabbitMQConstants.SCRAPE_QUEUE)
    public void consumeScrapeJob(ScrapeJobMessage message) {
        log.info("Received scrape job: jobId={}, sourceId={}, sourceName={}, triggeredBy={}",
                message.getJobId(), message.getSourceId(), message.getSourceName(), message.getTriggeredBy());

        ScrapingJob job = null;

        try {
            // Find the source
            Source source = sourceRepository.findById(message.getSourceId())
                    .orElseThrow(() -> new IllegalArgumentException("Source not found: " + message.getSourceId()));

            // Find or create scraping job record
            if (message.getJobId() != null) {
                // Update existing job (triggered manually via API)
                job = scrapingJobRepository.findById(message.getJobId())
                        .orElseThrow(() -> new IllegalArgumentException("Scraping job not found: " + message.getJobId()));
                job.setStatus(ScrapingJobStatus.RUNNING);
                job.setStartedAt(LocalDateTime.now());
                job = scrapingJobRepository.save(job);
                log.info("Updated existing job {} to RUNNING status", job.getId());
            } else {
                // Create new job (for scheduled scrapes)
                job = createScrapingJob(source);
                log.info("Created new scraping job with id {}", job.getId());
            }

            // Perform scraping
            log.info("Starting scrape for source: {} ({})", source.getName(), source.getDisplayName());
            List<ListingDto> scrapedListings = scraperService.scrapeListings(source);

            // Convert DTOs to entities
            List<Listing> listings = scraperService.convertToEntities(scrapedListings, source);

            // Bulk upsert listings
            log.info("Upserting {} listings for source: {}", listings.size(), source.getName());
            UpsertResult result = listingService.bulkUpsert(listings);

            // Update job as completed
            completeJob(job, result);

            // Update source last scrape time
            source.setLastScrapeAt(LocalDateTime.now());
            sourceRepository.save(source);

            log.info("Scrape job completed successfully: jobId={}, source={}, total={}, new={}, updated={}, skipped={}",
                    message.getJobId(), source.getName(), scrapedListings.size(),
                    result.newCount(), result.updatedCount(), result.skippedCount());

        } catch (ScrapingException e) {
            log.error("Scraping failed for jobId={}: {}", message.getJobId(), e.getMessage(), e);
            if (job != null) {
                failJob(job, "Scraping error: " + e.getMessage());
            }
            throw new RuntimeException("Scraping failed", e); // Will trigger retry via RabbitMQ

        } catch (Exception e) {
            log.error("Unexpected error processing scrape job {}: {}", message.getJobId(), e.getMessage(), e);
            if (job != null) {
                failJob(job, "Unexpected error: " + e.getMessage());
            }
            throw new RuntimeException("Unexpected error", e); // Will trigger retry via RabbitMQ
        }
    }

    /**
     * Creates a scraping job record in RUNNING status.
     *
     * @param source the source being scraped
     * @return the created scraping job
     */
    private ScrapingJob createScrapingJob(Source source) {
        ScrapingJob job = ScrapingJob.builder()
                .source(source)
                .status(ScrapingJobStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .itemsScraped(0)
                .itemsNew(0)
                .itemsUpdated(0)
                .build();

        return scrapingJobRepository.save(job);
    }

    /**
     * Marks a job as completed and updates statistics.
     *
     * @param job the scraping job
     * @param result the upsert result
     */
    private void completeJob(ScrapingJob job, UpsertResult result) {
        job.setStatus(ScrapingJobStatus.COMPLETED);
        job.setCompletedAt(LocalDateTime.now());
        job.setItemsScraped(result.getTotalProcessed());
        job.setItemsNew(result.newCount());
        job.setItemsUpdated(result.updatedCount());
        scrapingJobRepository.save(job);
    }

    /**
     * Marks a job as failed and records the error message.
     *
     * @param job the scraping job
     * @param errorMessage the error message
     */
    private void failJob(ScrapingJob job, String errorMessage) {
        job.setStatus(ScrapingJobStatus.FAILED);
        job.setCompletedAt(LocalDateTime.now());
        job.setErrorMessage(errorMessage);
        scrapingJobRepository.save(job);
    }
}
