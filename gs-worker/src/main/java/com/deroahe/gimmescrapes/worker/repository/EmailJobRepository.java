package com.deroahe.gimmescrapes.worker.repository;

import com.deroahe.gimmescrapes.commons.model.EmailJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailJobRepository extends JpaRepository<EmailJob, Long> {
}
