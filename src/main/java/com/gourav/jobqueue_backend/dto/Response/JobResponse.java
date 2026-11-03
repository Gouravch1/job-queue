package com.gourav.jobqueue_backend.dto.Response;

import com.gourav.jobqueue_backend.entity.Job;
import com.gourav.jobqueue_backend.entity.JobStatus;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record JobResponse(

        UUID id,
        String type,
        Map<String, Object> payload,
        JobStatus status,
        int priority,
        int attemptCount,
        int maxAttempts,
        Instant availableAt,
        String idempotencyKey,
        Instant lockedAt,
        String lockedBy,
        String lastError,
        Instant createdAt,
        Instant updatedAt

) {

    public static JobResponse from(Job job) {

        return new JobResponse(
                job.getId(),
                job.getType(),
                new LinkedHashMap<>(job.getPayload()),
                job.getStatus(),
                job.getPriority(),
                job.getAttemptCount(),
                job.getMaxAttempts(),
                job.getAvailableAt(),
                job.getIdempotencyKey(),
                job.getLockedAt(),
                job.getLockedBy(),
                job.getLastError(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}
