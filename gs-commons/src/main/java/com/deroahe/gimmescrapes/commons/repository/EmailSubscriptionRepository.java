package com.deroahe.gimmescrapes.commons.repository;

import com.deroahe.gimmescrapes.commons.model.EmailSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for EmailSubscription entity.
 */
@Repository
public interface EmailSubscriptionRepository extends JpaRepository<EmailSubscription, Long> {

    /**
     * Finds an email subscription by email address.
     *
     * @param email the email address
     * @return optional containing the subscription if found
     */
    Optional<EmailSubscription> findByEmail(String email);

    /**
     * Finds all active or inactive subscriptions.
     *
     * @param active true for active subscriptions, false for inactive
     * @return list of subscriptions matching the active status
     */
    List<EmailSubscription> findByActive(Boolean active);

    /**
     * Finds all active subscriptions.
     *
     * @return list of active subscriptions
     */
    @Query("SELECT es FROM EmailSubscription es WHERE es.active = true")
    List<EmailSubscription> findAllActive();

    /**
     * Checks if an email subscription exists by email.
     *
     * @param email the email address
     * @return true if a subscription with the email exists
     */
    boolean existsByEmail(String email);
}
