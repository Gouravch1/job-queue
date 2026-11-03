package com.gourav.jobqueue_backend.processing.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record WebhookJobPayload(

        @NotBlank(message = "Webhook URL is required")
        @Size(max = 2048)
        String url,

        @NotBlank(message = "HTTP method is required")
        @Pattern(
                regexp = "(?i)GET|POST|PUT|PATCH|DELETE",
                message = "HTTP method must be GET, POST, PUT, PATCH or DELETE"
        )
        String method,

        @NotNull(message = "Webhook body is required")
        Map<String, Object> body

) {
}