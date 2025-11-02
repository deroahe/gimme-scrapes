package com.deroahe.gimmescrapes.worker.repository;

import com.deroahe.gimmescrapes.commons.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    Optional<Listing> findByUrl(String url);

    boolean existsByUrl(String url);
}
