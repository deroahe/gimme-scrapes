package com.deroahe.gimmescrapes.worker.service;

import com.deroahe.gimmescrapes.commons.model.Listing;
import com.deroahe.gimmescrapes.commons.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing listings.
 * Handles bulk upsert operations and duplicate detection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;

    /**
     * Performs a bulk upsert operation on listings.
     * For each listing, checks if it exists by URL and either updates or inserts it.
     *
     * @param listings the listings to upsert
     * @return statistics about the upsert operation
     */
    @Transactional
    public UpsertResult bulkUpsert(List<Listing> listings) {
        log.info("Starting bulk upsert for {} listings", listings.size());

        int newCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;

        List<Listing> savedListings = new ArrayList<>();

        for (Listing listing : listings) {
            try {
                if (listing.getUrl() == null || listing.getUrl().isEmpty()) {
                    log.warn("Skipping listing with empty URL");
                    skippedCount++;
                    continue;
                }

                Optional<Listing> existingOpt = listingRepository.findByUrl(listing.getUrl());

                if (existingOpt.isPresent()) {
                    // Update existing listing
                    Listing existing = existingOpt.get();
                    boolean wasUpdated = updateListing(existing, listing);

                    if (wasUpdated) {
                        Listing saved = listingRepository.save(existing);
                        savedListings.add(saved);
                        updatedCount++;
                        log.debug("Updated listing: {}", listing.getUrl());
                    } else {
                        skippedCount++;
                        log.debug("No changes detected for listing: {}", listing.getUrl());
                    }
                } else {
                    // Insert new listing
                    Listing saved = listingRepository.save(listing);
                    savedListings.add(saved);
                    newCount++;
                    log.debug("Inserted new listing: {}", listing.getUrl());
                }

            } catch (Exception e) {
                log.error("Error upserting listing {}: {}", listing.getUrl(), e.getMessage());
                skippedCount++;
            }
        }

        log.info("Bulk upsert completed. New: {}, Updated: {}, Skipped: {}",
                newCount, updatedCount, skippedCount);

        return new UpsertResult(newCount, updatedCount, skippedCount, savedListings);
    }

    /**
     * Updates an existing listing with new data.
     * Only updates fields if they have changed.
     *
     * @param existing the existing listing
     * @param newData the new listing data
     * @return true if any fields were updated
     */
    private boolean updateListing(Listing existing, Listing newData) {
        boolean updated = false;

        // Update price if changed
        if (newData.getPrice() != null && !newData.getPrice().equals(existing.getPrice())) {
            existing.setPrice(newData.getPrice());
            updated = true;
        }

        // Update title if changed
        if (newData.getTitle() != null && !newData.getTitle().equals(existing.getTitle())) {
            existing.setTitle(newData.getTitle());
            updated = true;
        }

        // Update description if changed
        if (newData.getDescription() != null && !newData.getDescription().equals(existing.getDescription())) {
            existing.setDescription(newData.getDescription());
            updated = true;
        }

        // Update surface if changed
        if (newData.getSurfaceSqm() != null && !newData.getSurfaceSqm().equals(existing.getSurfaceSqm())) {
            existing.setSurfaceSqm(newData.getSurfaceSqm());
            updated = true;
        }

        // Update rooms if changed
        if (newData.getRooms() != null && !newData.getRooms().equals(existing.getRooms())) {
            existing.setRooms(newData.getRooms());
            updated = true;
        }

        // Update bathrooms if changed
        if (newData.getBathrooms() != null && !newData.getBathrooms().equals(existing.getBathrooms())) {
            existing.setBathrooms(newData.getBathrooms());
            updated = true;
        }

        // Update floor if changed
        if (newData.getFloor() != null && !newData.getFloor().equals(existing.getFloor())) {
            existing.setFloor(newData.getFloor());
            updated = true;
        }

        // Update total floors if changed
        if (newData.getTotalFloors() != null && !newData.getTotalFloors().equals(existing.getTotalFloors())) {
            existing.setTotalFloors(newData.getTotalFloors());
            updated = true;
        }

        // Update year built if changed
        if (newData.getYearBuilt() != null && !newData.getYearBuilt().equals(existing.getYearBuilt())) {
            existing.setYearBuilt(newData.getYearBuilt());
            updated = true;
        }

        // Update location fields if changed
        if (newData.getCity() != null && !newData.getCity().equals(existing.getCity())) {
            existing.setCity(newData.getCity());
            updated = true;
        }

        if (newData.getNeighborhood() != null && !newData.getNeighborhood().equals(existing.getNeighborhood())) {
            existing.setNeighborhood(newData.getNeighborhood());
            updated = true;
        }

        if (newData.getAddress() != null && !newData.getAddress().equals(existing.getAddress())) {
            existing.setAddress(newData.getAddress());
            updated = true;
        }

        // Update coordinates if changed
        if (newData.getLatitude() != null && !newData.getLatitude().equals(existing.getLatitude())) {
            existing.setLatitude(newData.getLatitude());
            updated = true;
        }

        if (newData.getLongitude() != null && !newData.getLongitude().equals(existing.getLongitude())) {
            existing.setLongitude(newData.getLongitude());
            updated = true;
        }

        // Update image URLs if changed
        if (newData.getImageUrls() != null && !newData.getImageUrls().equals(existing.getImageUrls())) {
            existing.setImageUrls(newData.getImageUrls());
            updated = true;
        }

        // Update features if changed
        if (newData.getFeatures() != null && !newData.getFeatures().equals(existing.getFeatures())) {
            existing.setFeatures(newData.getFeatures());
            updated = true;
        }

        // Always update lastScrapedAt
        existing.setLastScrapedAt(LocalDateTime.now());

        return updated;
    }

    /**
     * Result of a bulk upsert operation.
     */
    public record UpsertResult(
            int newCount,
            int updatedCount,
            int skippedCount,
            List<Listing> savedListings
    ) {
        public int getTotalProcessed() {
            return newCount + updatedCount + skippedCount;
        }
    }
}
