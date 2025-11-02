package com.deroahe.gimmescrapes.commons.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "listings", indexes = {
    @Index(name = "idx_listings_city", columnList = "city"),
    @Index(name = "idx_listings_price", columnList = "price"),
    @Index(name = "idx_listings_scraped", columnList = "last_scraped_at"),
    @Index(name = "idx_listings_source", columnList = "source_id"),
    @Index(name = "idx_listings_url", columnList = "url")
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    @ToString.Exclude
    private Source source;

    @Column(name = "external_id")
    private String externalId;

    @Column(unique = true, nullable = false)
    private String url;

    @Column
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column
    private String currency = "EUR";

    @Column(name = "surface_sqm", precision = 10, scale = 2)
    private BigDecimal surfaceSqm;

    @Column(name = "price_per_sqm", precision = 10, scale = 2)
    private BigDecimal pricePerSqm;

    @Column
    private Integer rooms;

    @Column
    private Integer bathrooms;

    @Column
    private Integer floor;

    @Column(name = "total_floors")
    private Integer totalFloors;

    @Column(name = "year_built")
    private Integer yearBuilt;

    @Column
    private String city;

    @Column
    private String neighborhood;

    @Column
    private String address;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "image_urls", columnDefinition = "text[]")
    private List<String> imageUrls;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features", columnDefinition = "jsonb")
    private Map<String, Object> features;

    @Column(name = "first_scraped_at")
    private LocalDateTime firstScrapedAt;

    @Column(name = "last_scraped_at")
    private LocalDateTime lastScrapedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (firstScrapedAt == null) {
            firstScrapedAt = LocalDateTime.now();
        }
        lastScrapedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastScrapedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Listing listing = (Listing) o;
        return id != null && Objects.equals(id, listing.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
