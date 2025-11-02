package com.deroahe.gimmescrapes.worker.service;

import com.deroahe.gimmescrapes.commons.dto.ListingDto;
import com.deroahe.gimmescrapes.commons.exception.ScrapingException;
import com.deroahe.gimmescrapes.commons.model.Listing;
import com.deroahe.gimmescrapes.commons.model.Source;
import com.deroahe.gimmescrapes.commons.scraper.RealEstateScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service that coordinates scraping operations using the strategy pattern.
 * Selects the appropriate scraper implementation based on the source.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperService {

    private final List<RealEstateScraper> scrapers;

    /**
     * Scrapes listings from the given source using the appropriate scraper.
     *
     * @param source the source to scrape from
     * @return list of scraped listings as DTOs
     * @throws ScrapingException if scraping fails or no scraper is found
     */
    public List<ListingDto> scrapeListings(Source source) throws ScrapingException {
        log.info("Starting scrape for source: {}", source.getName());

        RealEstateScraper scraper = findScraper(source.getName());
        if (scraper == null) {
            throw new ScrapingException("No scraper found for source: " + source.getName());
        }

        List<ListingDto> listings = scraper.scrape(source);
        log.info("Scraping completed for {}. Retrieved {} listings", source.getName(), listings.size());

        return listings;
    }

    /**
     * Converts ListingDto objects to Listing entities.
     *
     * @param dtos the DTOs to convert
     * @param source the source of the listings
     * @return list of Listing entities
     */
    public List<Listing> convertToEntities(List<ListingDto> dtos, Source source) {
        return dtos.stream()
                .map(dto -> convertToEntity(dto, source))
                .toList();
    }

    /**
     * Converts a single ListingDto to a Listing entity.
     *
     * @param dto the DTO to convert
     * @param source the source of the listing
     * @return Listing entity
     */
    private Listing convertToEntity(ListingDto dto, Source source) {
        LocalDateTime now = LocalDateTime.now();

        return Listing.builder()
                .source(source)
                .externalId(dto.getExternalId())
                .url(dto.getUrl())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .currency(dto.getCurrency())
                .surfaceSqm(dto.getSurfaceSqm())
                .pricePerSqm(dto.getPricePerSqm())
                .rooms(dto.getRooms())
                .bathrooms(dto.getBathrooms())
                .floor(dto.getFloor())
                .totalFloors(dto.getTotalFloors())
                .yearBuilt(dto.getYearBuilt())
                .city(dto.getCity())
                .neighborhood(dto.getNeighborhood())
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .imageUrls(dto.getImageUrls())
                .features(dto.getFeatures())
                .firstScrapedAt(now)
                .lastScrapedAt(now)
                .build();
    }

    /**
     * Finds the appropriate scraper for the given source name.
     *
     * @param sourceName the source name
     * @return the scraper that supports the source, or null if not found
     */
    private RealEstateScraper findScraper(String sourceName) {
        return scrapers.stream()
                .filter(scraper -> scraper.supports(sourceName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all available scraper source names.
     *
     * @return list of source names supported by available scrapers
     */
    public List<String> getAvailableScrapers() {
        return scrapers.stream()
                .map(RealEstateScraper::getSourceName)
                .collect(Collectors.toList());
    }
}
