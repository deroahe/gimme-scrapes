package com.deroahe.gimmescrapes.worker.repository;

import com.deroahe.gimmescrapes.commons.model.ScrapingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapingJobRepository extends JpaRepository<ScrapingJob, Long> {
}
