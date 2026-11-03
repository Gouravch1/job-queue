
package com.gourav.jobqueue_backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.*;

@Getter
@Entity
@Table(name = "jobs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "type", nullable = false, length = 40)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private JobStatus status = JobStatus.QUEUED;

    @Column(name = "priority", nullable = false)
    private int priority = 0;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts = 3;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;

    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;

    @Column(name = "locked_at")
    private Instant lockedAt;

    @Column(name = "locked_by", length = 100)
    private String lockedBy;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Job(
            String type,
            Map<String, Object> payload,
            int priority,
            int maxAttempts,
            Instant availableAt,
            String idempotencyKey
    ) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Job type cannot be empty");
        }

        if (type.length() > 40) {
            throw new IllegalArgumentException("Job type cannot exceed 40 characters");
        }

        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("Max attempts must be greater than zero");
        }

        this.type = type.trim().toUpperCase(Locale.ROOT);
        this.payload = new HashMap<>(
                Objects.requireNonNull(payload, "Job payload cannot be null")
        );
        this.priority = priority;
        this.maxAttempts = maxAttempts;
        this.availableAt = availableAt != null ? availableAt : Instant.now();
        this.idempotencyKey = idempotencyKey;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (availableAt == null) {
            availableAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
