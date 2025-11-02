package com.deroahe.gimmescrapes.orchestrator.controller;

import com.deroahe.gimmescrapes.commons.dto.ScrapeJobMessage;
import com.deroahe.gimmescrapes.commons.enums.ScrapingJobStatus;
import com.deroahe.gimmescrapes.commons.enums.TriggerType;
import com.deroahe.gimmescrapes.commons.model.ScrapingJob;
import com.deroahe.gimmescrapes.commons.model.Source;
import com.deroahe.gimmescrapes.commons.repository.ScrapingJobRepository;
import com.deroahe.gimmescrapes.commons.repository.SourceRepository;
import com.deroahe.gimmescrapes.orchestrator.service.MessagePublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final SourceRepository sourceRepository;
    private final ScrapingJobRepository scrapingJobRepository;
    private final MessagePublisherService messagePublisherService;

    @PostMapping("/scrape/{sourceName}")
    public ResponseEntity<?> triggerTestScrape(@PathVariable("sourceName") String sourceName) {
        log.info("Test scrape triggered for source: {}", sourceName);

        // Find source
        Source source = sourceRepository.findByName(sourceName)
                .orElseThrow(() -> new RuntimeException("Source not found: " + sourceName));

        // Create scraping job
        ScrapingJob job = ScrapingJob.builder()
                .source(source)
                .status(ScrapingJobStatus.PENDING)
                .build();
        job = scrapingJobRepository.save(job);

        // Publish message
        ScrapeJobMessage message = ScrapeJobMessage.builder()
                .jobId(job.getId())
                .sourceId(source.getId())
                .sourceName(source.getName())
                .triggeredBy(TriggerType.MANUAL)
                .timestamp(LocalDateTime.now())
                .build();

        messagePublisherService.publishScrapeJob(message);

        return ResponseEntity.ok(Map.of(
                "message", "Scrape job triggered successfully",
                "jobId", job.getId(),
                "sourceName", sourceName
        ));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<?> getJobStatus(@PathVariable Long jobId) {
        ScrapingJob job = scrapingJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        return ResponseEntity.ok(Map.of(
                "jobId", job.getId(),
                "status", job.getStatus(),
                "startedAt", job.getStartedAt(),
                "completedAt", job.getCompletedAt(),
                "itemsScraped", job.getItemsScraped(),
                "errorMessage", job.getErrorMessage() != null ? job.getErrorMessage() : ""
        ));
    }
}
