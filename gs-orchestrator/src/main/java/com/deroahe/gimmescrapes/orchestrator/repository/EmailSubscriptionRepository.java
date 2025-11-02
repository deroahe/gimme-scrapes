package com.deroahe.gimmescrapes.orchestrator.repository;

import com.deroahe.gimmescrapes.commons.model.EmailSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailSubscriptionRepository extends JpaRepository<EmailSubscription, Long> {

    Optional<EmailSubscription> findByEmail(String email);

    List<EmailSubscription> findByActive(Boolean active);
}
