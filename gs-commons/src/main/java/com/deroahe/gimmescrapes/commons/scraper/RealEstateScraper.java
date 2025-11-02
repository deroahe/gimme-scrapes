package com.deroahe.gimmescrapes.commons.scraper;

import com.deroahe.gimmescrapes.commons.dto.ListingDto;
import com.deroahe.gimmescrapes.commons.exception.ScrapingException;
import com.deroahe.gimmescrapes.commons.model.Source;

import java.util.List;

/**
 * Strategy interface for scraping real estate listings from different sources.
 * Each implementation handles the specific HTML structure and data extraction
 * logic for a particular real estate website.
 */
public interface RealEstateScraper {

    /**
     * Scrapes listings from the given source.
     *
     * @param source the source to scrape from
     * @return list of scraped listings
     * @throws ScrapingException if scraping fails
     */
    List<ListingDto> scrape(Source source) throws ScrapingException;

    /**
     * Checks if this scraper supports the given source name.
     *
     * @param sourceName the source name to check
     * @return true if this scraper supports the source
     */
    boolean supports(String sourceName);

    /**
     * Gets the source name this scraper handles.
     *
     * @return the source name (e.g., "imobiliare.ro", "olx.ro")
     */
    String getSourceName();
}
