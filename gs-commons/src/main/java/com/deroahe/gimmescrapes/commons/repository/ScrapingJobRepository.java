package com.deroahe.gimmescrapes.commons.repository;

import com.deroahe.gimmescrapes.commons.enums.ScrapingJobStatus;
import com.deroahe.gimmescrapes.commons.model.ScrapingJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ScrapingJob entity.
 */
@Repository
public interface ScrapingJobRepository extends JpaRepository<ScrapingJob, Long> {

    /**
     * Finds all scraping jobs for a specific source, ordered by creation date descending.
     *
     * @param sourceId the source ID
     * @param pageable pagination information
     * @return page of scraping jobs
     */
    @Query("SELECT sj FROM ScrapingJob sj WHERE sj.source.id = :sourceId ORDER BY sj.createdAt DESC")
    Page<ScrapingJob> findBySourceId(@Param("sourceId") Long sourceId, Pageable pageable);

    /**
     * Finds all scraping jobs with a specific status.
     *
     * @param status the status to filter by
     * @param pageable pagination information
     * @return page of scraping jobs
     */
    Page<ScrapingJob> findByStatus(ScrapingJobStatus status, Pageable pageable);

    /**
     * Finds all scraping jobs ordered by creation date descending.
     *
     * @param pageable pagination information
     * @return page of scraping jobs
     */
    @Query("SELECT sj FROM ScrapingJob sj ORDER BY sj.createdAt DESC")
    Page<ScrapingJob> findAllOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Counts scraping jobs by status.
     *
     * @param status the status to count
     * @return count of jobs with the given status
     */
    long countByStatus(ScrapingJobStatus status);

    /**
     * Finds the most recent scraping job for a source.
     *
     * @param sourceId the source ID
     * @return list containing the most recent job (limited to 1)
     */
    @Query("SELECT sj FROM ScrapingJob sj WHERE sj.source.id = :sourceId ORDER BY sj.createdAt DESC LIMIT 1")
    List<ScrapingJob> findMostRecentBySourceId(@Param("sourceId") Long sourceId);
}
