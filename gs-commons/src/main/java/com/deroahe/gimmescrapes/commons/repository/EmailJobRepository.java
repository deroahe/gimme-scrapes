package com.deroahe.gimmescrapes.commons.repository;

import com.deroahe.gimmescrapes.commons.enums.EmailJobStatus;
import com.deroahe.gimmescrapes.commons.model.EmailJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for EmailJob entity.
 */
@Repository
public interface EmailJobRepository extends JpaRepository<EmailJob, Long> {

    /**
     * Finds all email jobs with a specific status.
     *
     * @param status the status to filter by
     * @return list of email jobs with the given status
     */
    List<EmailJob> findByStatus(EmailJobStatus status);

    /**
     * Counts email jobs by status.
     *
     * @param status the status to count
     * @return count of jobs with the given status
     */
    long countByStatus(EmailJobStatus status);
}
