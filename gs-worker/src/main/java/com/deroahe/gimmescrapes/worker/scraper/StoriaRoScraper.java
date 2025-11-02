package com.deroahe.gimmescrapes.worker.scraper;

import com.deroahe.gimmescrapes.commons.dto.ListingDto;
import com.deroahe.gimmescrapes.commons.exception.ScrapingException;
import com.deroahe.gimmescrapes.commons.model.Source;
import com.deroahe.gimmescrapes.commons.scraper.RealEstateScraper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Scraper implementation for storia.ro
 * Scrapes real estate listings from Storia (formerly OLX Imobiliare).
 * Uses JSON data embedded in __NEXT_DATA__ script tag.
 */
@Slf4j
@Component
public class StoriaRoScraper implements RealEstateScraper {

    private static final String SOURCE_NAME = "storia.ro";
    private static final int REQUEST_DELAY_MS = 2000; // 2 seconds between requests
    private static final int TIMEOUT_MS = 10000; // 10 seconds timeout
    private static final int MAX_PAGES = 30;

    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    };

    private final Random random = new Random();
    private final ObjectMapper objectMapper = new ObjectMapper();

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

                // Extract JSON from __NEXT_DATA__ script tag
                JsonNode jsonData = extractNextDataJson(doc);
                if (jsonData == null) {
                    log.warn("Could not find __NEXT_DATA__ script tag on page {}. Stopping.", page);
                    break;
                }

                // Navigate to items array
                JsonNode items = jsonData.at("/props/pageProps/data/searchAds/items");
                if (items.isMissingNode() || !items.isArray()) {
                    log.warn("No listings found in JSON data on page {}. Stopping pagination.", page);
                    break;
                }

                if (items.size() == 0) {
                    log.warn("Empty items array on page {}. Stopping pagination.", page);
                    break;
                }

                log.debug("Found {} listings on page {}", items.size(), page);

                for (JsonNode item : items) {
                    try {
                        ListingDto listing = extractListingFromJson(item, source.getBaseUrl());
                        if (listing != null && listing.getUrl() != null) {
                            allListings.add(listing);
                            successCount++;
                        }
                    } catch (Exception e) {
                        errorCount++;
                        log.error("Error extracting listing from JSON: {}", e.getMessage());
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
        // Default search: apartments for sale in Cluj-Napoca, Marasti
        if (page == 1) {
            return baseUrl + "/ro/rezultate/vanzare/apartament/cluj/cluj--napoca/marasti";
        }
        return baseUrl + "/ro/rezultate/vanzare/apartament/cluj/cluj--napoca/marasti?page=" + page;
    }

    /**
     * Extracts JSON data from the __NEXT_DATA__ script tag.
     */
    private JsonNode extractNextDataJson(Document doc) {
        try {
            Element scriptTag = doc.selectFirst("script#__NEXT_DATA__");
            if (scriptTag == null) {
                log.error("Could not find script tag with id '__NEXT_DATA__'");
                return null;
            }

            String jsonText = scriptTag.html();
            if (jsonText.isEmpty()) {
                log.error("__NEXT_DATA__ script tag is empty");
                return null;
            }

            return objectMapper.readTree(jsonText);
        } catch (Exception e) {
            log.error("Error parsing __NEXT_DATA__ JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts listing data from a JSON item node.
     */
    private ListingDto extractListingFromJson(JsonNode item, String baseUrl) {
        try {
            ListingDto.ListingDtoBuilder builder = ListingDto.builder();

            // Extract ID and URL
            long id = item.path("id").asLong(0);
            String slug = item.path("slug").asText("");

            if (id > 0) {
                builder.externalId(String.valueOf(id));
            }

            if (!slug.isEmpty()) {
                String fullUrl = baseUrl + "/ro/oferta/" + slug;
                builder.url(fullUrl);
            }

            // Extract title
            String title = item.path("title").asText("");
            if (!title.isEmpty()) {
                builder.title(title);
            }

            // Extract price
            JsonNode totalPrice = item.path("totalPrice");
            if (!totalPrice.isMissingNode()) {
                BigDecimal price = new BigDecimal(totalPrice.path("value").asDouble(0));
                String currency = totalPrice.path("currency").asText("EUR");
                builder.price(price);
                builder.currency(currency);
            }

            // Extract surface area
            double area = item.path("areaInSquareMeters").asDouble(0);
            if (area > 0) {
                builder.surfaceSqm(BigDecimal.valueOf(area));
            }

            // Extract price per square meter
            JsonNode pricePerSqmNode = item.path("pricePerSquareMeter");
            if (!pricePerSqmNode.isMissingNode()) {
                double pricePerSqmValue = pricePerSqmNode.path("value").asDouble(0);
                if (pricePerSqmValue > 0) {
                    builder.pricePerSqm(BigDecimal.valueOf(pricePerSqmValue));
                }
            }

            // Extract rooms - convert from enum to number
            String roomsEnum = item.path("roomsNumber").asText("");
            Integer rooms = convertRoomsEnum(roomsEnum);
            if (rooms != null) {
                builder.rooms(rooms);
            }

            // Extract floor - convert from enum to number
            String floorEnum = item.path("floorNumber").asText("");
            Integer floor = convertFloorEnum(floorEnum);
            if (floor != null) {
                builder.floor(floor);
            }

            // Extract location
            JsonNode location = item.path("location");
            if (!location.isMissingNode()) {
                extractLocation(location, builder);
            }

            // Extract images
            JsonNode imagesNode = item.path("images");
            if (imagesNode.isArray()) {
                List<String> imageUrls = new ArrayList<>();
                for (JsonNode imageNode : imagesNode) {
                    String largeUrl = imageNode.path("large").asText("");
                    if (!largeUrl.isEmpty()) {
                        imageUrls.add(largeUrl);
                    }
                }
                builder.imageUrls(imageUrls);
            }

            // Extract features
            Map<String, Object> features = new HashMap<>();

            // Check if it's a promoted listing
            boolean isPromoted = item.path("isPromoted").asBoolean(false);
            if (isPromoted) {
                features.put("promoted", true);
            }

            // Note: Additional features would need to be extracted from the detailed listing page
            // as the search results don't include all amenities

            builder.features(features);

            return builder.build();

        } catch (Exception e) {
            log.error("Error extracting listing data from JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Converts Storia room enum to integer.
     * ONE -> 1, TWO -> 2, THREE -> 3, FOUR -> 4, etc.
     */
    private Integer convertRoomsEnum(String roomsEnum) {
        if (roomsEnum == null || roomsEnum.isEmpty()) {
            return null;
        }

        return switch (roomsEnum) {
            case "ONE" -> 1;
            case "TWO" -> 2;
            case "THREE" -> 3;
            case "FOUR" -> 4;
            case "FIVE" -> 5;
            case "SIX" -> 6;
            case "SEVEN" -> 7;
            case "EIGHT" -> 8;
            case "NINE" -> 9;
            case "TEN_OR_MORE" -> 10;
            default -> {
                log.warn("Unknown roomsNumber enum: {}", roomsEnum);
                yield null;
            }
        };
    }

    /**
     * Converts Storia floor enum to integer.
     * GROUND -> 0, FIRST -> 1, etc.
     */
    private Integer convertFloorEnum(String floorEnum) {
        if (floorEnum == null || floorEnum.isEmpty()) {
            return null;
        }

        return switch (floorEnum) {
            case "BASEMENT" -> -1;
            case "CELLAR" -> -1;
            case "GROUND" -> 0;
            case "FIRST" -> 1;
            case "SECOND" -> 2;
            case "THIRD" -> 3;
            case "FOURTH" -> 4;
            case "FIFTH" -> 5;
            case "SIXTH" -> 6;
            case "SEVENTH" -> 7;
            case "EIGHTH" -> 8;
            case "NINTH" -> 9;
            case "TENTH" -> 10;
            case "ABOVE_TENTH" -> 11;
            default -> {
                log.warn("Unknown floorNumber enum: {}", floorEnum);
                yield null;
            }
        };
    }

    /**
     * Extracts location information from JSON.
     */
    private void extractLocation(JsonNode location, ListingDto.ListingDtoBuilder builder) {
        // Extract city
        JsonNode city = location.at("/address/city/name");
        if (!city.isMissingNode()) {
            builder.city(city.asText());
        }

        // Extract neighborhood from reverseGeocoding
        JsonNode locations = location.at("/reverseGeocoding/locations");
        if (locations.isArray()) {
            for (JsonNode loc : locations) {
                String level = loc.path("locationLevel").asText("");
                if ("district".equals(level)) {
                    String neighborhood = loc.path("name").asText("");
                    if (!neighborhood.isEmpty()) {
                        builder.neighborhood(neighborhood);
                    }
                    break;
                }
            }

            // Build full address from location names
            StringBuilder address = new StringBuilder();
            for (JsonNode loc : locations) {
                String name = loc.path("name").asText("");
                if (!name.isEmpty()) {
                    if (address.length() > 0) {
                        address.append(", ");
                    }
                    address.append(name);
                }
            }
            if (address.length() > 0) {
                builder.address(address.toString());
            }
        }
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
