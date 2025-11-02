package com.deroahe.gimmescrapes.commons.repository;

import com.deroahe.gimmescrapes.commons.model.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Source entity.
 */
@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {

    /**
     * Finds a source by its name.
     *
     * @param name the source name
     * @return optional containing the source if found
     */
    Optional<Source> findByName(String name);

    /**
     * Finds all enabled sources.
     *
     * @return list of enabled sources
     */
    @Query("SELECT s FROM Source s WHERE s.enabled = true")
    List<Source> findAllEnabled();

    /**
     * Checks if a source exists by name.
     *
     * @param name the source name
     * @return true if a source with the name exists
     */
    boolean existsByName(String name);
}
