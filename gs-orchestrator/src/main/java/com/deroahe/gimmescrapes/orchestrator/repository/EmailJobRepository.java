package com.deroahe.gimmescrapes.orchestrator.repository;

import com.deroahe.gimmescrapes.commons.enums.EmailJobStatus;
import com.deroahe.gimmescrapes.commons.model.EmailJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailJobRepository extends JpaRepository<EmailJob, Long> {

    List<EmailJob> findByStatus(EmailJobStatus status);
}
