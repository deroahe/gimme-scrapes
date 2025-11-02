package com.deroahe.gimmescrapes.orchestrator.repository;

import com.deroahe.gimmescrapes.commons.enums.ScrapingJobStatus;
import com.deroahe.gimmescrapes.commons.model.ScrapingJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapingJobRepository extends JpaRepository<ScrapingJob, Long> {

    List<ScrapingJob> findByStatus(ScrapingJobStatus status);

    Page<ScrapingJob> findBySourceId(Long sourceId, Pageable pageable);

    Page<ScrapingJob> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
