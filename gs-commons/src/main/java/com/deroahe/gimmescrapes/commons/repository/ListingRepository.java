package com.deroahe.gimmescrapes.commons.repository;

import com.deroahe.gimmescrapes.commons.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Listing entity.
 * Provides CRUD operations and custom queries for listings.
 */
@Repository
public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    /**
     * Finds a listing by its URL.
     *
     * @param url the URL to search for
     * @return optional containing the listing if found
     */
    Optional<Listing> findByUrl(String url);

    /**
     * Finds a listing by source and external ID.
     *
     * @param sourceId the source ID
     * @param externalId the external ID from the source website
     * @return optional containing the listing if found
     */
    @Query("SELECT l FROM Listing l WHERE l.source.id = :sourceId AND l.externalId = :externalId")
    Optional<Listing> findBySourceAndExternalId(@Param("sourceId") Long sourceId,
                                                  @Param("externalId") String externalId);

    /**
     * Checks if a listing exists by URL.
     *
     * @param url the URL to check
     * @return true if a listing with the URL exists
     */
    boolean existsByUrl(String url);

    /**
     * Counts listings by source ID.
     *
     * @param sourceId the source ID
     * @return count of listings from the source
     */
    @Query("SELECT COUNT(l) FROM Listing l WHERE l.source.id = :sourceId")
    long countBySourceId(@Param("sourceId") Long sourceId);

    /**
     * Deletes all listings from a specific source.
     * Use with caution!
     *
     * @param sourceId the source ID
     */
    @Modifying
    @Query("DELETE FROM Listing l WHERE l.source.id = :sourceId")
    void deleteBySourceId(@Param("sourceId") Long sourceId);
}
