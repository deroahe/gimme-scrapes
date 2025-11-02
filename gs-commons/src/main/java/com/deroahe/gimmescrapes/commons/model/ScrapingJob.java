package com.deroahe.gimmescrapes.commons.model;

import com.deroahe.gimmescrapes.commons.enums.ScrapingJobStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "scraping_jobs", indexes = {
    @Index(name = "idx_scraping_jobs_source", columnList = "source_id"),
    @Index(name = "idx_scraping_jobs_status", columnList = "status")
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapingJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    @ToString.Exclude
    private Source source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScrapingJobStatus status = ScrapingJobStatus.PENDING;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "items_scraped")
    private Integer itemsScraped = 0;

    @Column(name = "items_new")
    private Integer itemsNew = 0;

    @Column(name = "items_updated")
    private Integer itemsUpdated = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ScrapingJob that = (ScrapingJob) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
