package com.deroahe.gimmescrapes.worker.scraper;

import com.deroahe.gimmescrapes.commons.dto.ListingDto;
import com.deroahe.gimmescrapes.commons.exception.ScrapingException;
import com.deroahe.gimmescrapes.commons.model.Source;
import com.deroahe.gimmescrapes.commons.scraper.RealEstateScraper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scraper implementation for olx.ro
 * Scrapes real estate listings from OLX Romania.
 */
@Slf4j
@Component
public class OlxScraper implements RealEstateScraper {

    private static final String SOURCE_NAME = "olx.ro";
    private static final int REQUEST_DELAY_MS = 2000; // 2 seconds between requests
    private static final int TIMEOUT_MS = 10000; // 10 seconds timeout
    private static final int MAX_PAGES = 5; // Scrape first 5 pages for now

    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    };

    private final Random random = new Random();

    @Override
    public List<ListingDto> scrape(Source source) throws ScrapingException {
        log.info("Starting scraping for source: {}", source.getName());
        List<ListingDto> allListings = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try {
            for (int page = 1; page <= MAX_PAGES; page++) {
                log.debug("Scraping page {} of {}", page, MAX_PAGES);

                String searchUrl = buildSearchUrl(source.getBaseUrl(), page);
                Document doc = fetchDocument(searchUrl);

                // OLX uses data-cy attributes for listings
                Elements listingCards = doc.select("[data-cy='l-card'], .offer-wrapper, div[data-id]");

                if (listingCards.isEmpty()) {
                    log.warn("No listings found on page {}. Stopping pagination.", page);
                    break;
                }

                for (Element card : listingCards) {
                    try {
                        ListingDto listing = extractListing(card, source.getBaseUrl());
                        if (listing != null && listing.getUrl() != null) {
                            allListings.add(listing);
                            successCount++;
                        }
                    } catch (Exception e) {
                        errorCount++;
                        log.error("Error extracting listing from card: {}", e.getMessage());
                    }
                }

                // Throttle requests to be respectful
                if (page < MAX_PAGES) {
                    Thread.sleep(REQUEST_DELAY_MS);
                }
            }

            log.info("Scraping completed for {}. Success: {}, Errors: {}, Total: {}",
                    SOURCE_NAME, successCount, errorCount, allListings.size());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScrapingException("Scraping interrupted", e);
        } catch (Exception e) {
            throw new ScrapingException("Failed to scrape " + SOURCE_NAME, e);
        }

        return allListings;
    }

    private Document fetchDocument(String url) throws IOException {
        String userAgent = USER_AGENTS[random.nextInt(USER_AGENTS.length)];
        return Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(TIMEOUT_MS)
                .referrer("https://www.google.com")
                .get();
    }

    private String buildSearchUrl(String baseUrl, int page) {
        // Default search: apartments for sale in Bucharest
        if (page == 1) {
            return baseUrl + "/d/imobiliare/apartamente-garsoniere-de-vanzare/bucuresti/";
        }
        return baseUrl + "/d/imobiliare/apartamente-garsoniere-de-vanzare/bucuresti/?page=" + page;
    }

    private ListingDto extractListing(Element card, String baseUrl) {
        try {
            ListingDto.ListingDtoBuilder builder = ListingDto.builder();

            // Extract URL
            Element linkElement = card.selectFirst("a[href*='/oferta/'], a[href*='/d/oferta/']");
            if (linkElement != null) {
                String relativeUrl = linkElement.attr("href");
                String fullUrl = relativeUrl.startsWith("http") ? relativeUrl : baseUrl + relativeUrl;
                builder.url(fullUrl);

                // Extract external ID from URL or data attribute
                String externalId = card.attr("data-id");
                if (externalId.isEmpty()) {
                    externalId = extractExternalId(relativeUrl);
                }
                builder.externalId(externalId);
            }

            // Extract title
            Element titleElement = card.selectFirst("h6, .title, [data-cy='ad-card-title']");
            if (titleElement != null) {
                builder.title(titleElement.text().trim());
            }

            // Extract price
            Element priceElement = card.selectFirst("p[data-testid='ad-price'], .price, [class*='price']");
            if (priceElement != null) {
                String priceText = priceElement.text();
                BigDecimal price = extractPrice(priceText);
                builder.price(price);
                builder.currency(extractCurrency(priceText));
            }

            // Extract location
            Element locationElement = card.selectFirst("p[data-testid='location-date'], .bottom-cell span, [class*='location']");
            if (locationElement != null) {
                String locationText = locationElement.text();
                extractLocation(locationText, builder);
            }

            // Extract attributes (rooms, surface, etc.)
            Elements attributes = card.select("span[class*='param'], .params span, li");
            for (Element attr : attributes) {
                String text = attr.text().toLowerCase();

                // Extract rooms
                if (text.contains("camera") || text.contains("camere")) {
                    Integer rooms = extractInteger(text);
                    if (rooms != null) {
                        builder.rooms(rooms);
                    }
                }

                // Extract surface
                if (text.contains("m²") || text.contains("mp")) {
                    BigDecimal surface = extractNumber(text);
                    if (surface != null) {
                        builder.surfaceSqm(surface);
                    }
                }
            }

            // Extract images
            Elements images = card.select("img[src], img[data-src]");
            List<String> imageUrls = new ArrayList<>();
            for (Element img : images) {
                String src = img.attr("src");
                if (src.isEmpty()) {
                    src = img.attr("data-src");
                }
                if (!src.isEmpty() && !src.contains("placeholder") && !src.contains("no-image")) {
                    imageUrls.add(src);
                }
            }
            builder.imageUrls(imageUrls);

            // Extract features from attributes
            Map<String, Object> features = new HashMap<>();
            for (Element attr : attributes) {
                String text = attr.text().toLowerCase();
                if (text.contains("balcon")) features.put("balcony", true);
                if (text.contains("parcare")) features.put("parking", true);
                if (text.contains("lift") || text.contains("ascensor")) features.put("elevator", true);
                if (text.contains("centrala")) features.put("central_heating", true);
                if (text.contains("mobilat")) features.put("furnished", true);
            }
            builder.features(features);

            return builder.build();

        } catch (Exception e) {
            log.error("Error extracting listing data: {}", e.getMessage());
            return null;
        }
    }

    private String extractExternalId(String url) {
        Pattern pattern = Pattern.compile("/oferta/[^/]+-ID([A-Za-z0-9]+)\\.html");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // Try alternative pattern
        pattern = Pattern.compile("/d/oferta/([A-Za-z0-9-]+)");
        matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private BigDecimal extractPrice(String priceText) {
        if (priceText == null || priceText.isEmpty()) {
            return null;
        }
        // Remove currency symbols, spaces, and extract number
        String cleanPrice = priceText.replaceAll("[^0-9.,]", "").replace(",", "").replace(".", "");
        try {
            return new BigDecimal(cleanPrice);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String extractCurrency(String priceText) {
        if (priceText == null) {
            return "RON";
        }
        if (priceText.contains("€") || priceText.toLowerCase().contains("eur")) {
            return "EUR";
        }
        if (priceText.contains("lei") || priceText.toLowerCase().contains("ron")) {
            return "RON";
        }
        return "RON"; // OLX Romania defaults to RON
    }

    private BigDecimal extractNumber(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        String cleanNumber = text.replaceAll("[^0-9.,]", "").replace(",", ".");
        try {
            return new BigDecimal(cleanNumber);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer extractInteger(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        String cleanNumber = text.replaceAll("[^0-9]", "");
        try {
            return Integer.parseInt(cleanNumber);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void extractLocation(String locationText, ListingDto.ListingDtoBuilder builder) {
        if (locationText == null || locationText.isEmpty()) {
            return;
        }

        // Location format varies on OLX, often: "Bucuresti, Sector X"
        String[] parts = locationText.split(",");
        if (parts.length > 0) {
            builder.city(parts[0].trim());
        }
        if (parts.length > 1) {
            builder.neighborhood(parts[1].trim());
        }
        builder.address(locationText.trim());
    }

    @Override
    public boolean supports(String sourceName) {
        return SOURCE_NAME.equalsIgnoreCase(sourceName);
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }
}
