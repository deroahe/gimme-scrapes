package com.deroahe.gimmescrapes.commons.model;

import com.deroahe.gimmescrapes.commons.enums.EmailJobStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "email_jobs")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    @ToString.Exclude
    private EmailSubscription subscription;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailJobStatus status = EmailJobStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

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
        EmailJob emailJob = (EmailJob) o;
        return id != null && Objects.equals(id, emailJob.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
