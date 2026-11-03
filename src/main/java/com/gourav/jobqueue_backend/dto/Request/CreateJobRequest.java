package com.gourav.jobqueue_backend.dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;

public record CreateJobRequest(

        @NotBlank(message = "Job type is required")
        @Size(max = 40, message = "Job type cannot exceed 40 characters")
        String type,

        @NotNull(message = "Payload is required")
        Map<String, Object> payload,

        Integer priority,

        @Min(value = 1, message = "Max attempts must be at least 1")
        Integer maxAttempts,

        Instant availableAt,

        @Size(max = 255, message = "Idempotency key cannot exceed 255 characters")
        String idempotencyKey

) {

    public CreateJobRequest {
        if (type != null) {
            type = type.trim();
        }

        if (idempotencyKey != null) {
            idempotencyKey = idempotencyKey.trim();

            if (idempotencyKey.isEmpty()) {
                idempotencyKey = null;
            }
        }
    }
}
