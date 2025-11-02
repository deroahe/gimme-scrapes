package com.deroahe.gimmescrapes.commons.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for scraped listing data.
 * Used by scrapers to return raw listing data before persistence.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingDto {

    private String externalId;
    private String url;
    private String title;
    private String description;
    private BigDecimal price;
    private String currency;
    private BigDecimal surfaceSqm;
    private BigDecimal pricePerSqm;
    private Integer rooms;
    private Integer bathrooms;
    private Integer floor;
    private Integer totalFloors;
    private Integer yearBuilt;
    private String city;
    private String neighborhood;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<String> imageUrls;
    private Map<String, Object> features;
}
