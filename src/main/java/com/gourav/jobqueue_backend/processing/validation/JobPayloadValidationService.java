package com.gourav.jobqueue_backend.processing.validation;

import com.gourav.jobqueue_backend.entity.Job;
import com.gourav.jobqueue_backend.processing.exception.InvalidJobPayloadException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JobPayloadValidationService {

    private final Validator validator;

    public String requiredString(Job job, String field) {

        Object value = job.getPayload() == null
                ? null
                : job.getPayload().get(field);

        if (!(value instanceof String text) || text.isBlank()) {
            throw new InvalidJobPayloadException(
                    job.getId(),
                    field + " must be a non-empty string"
            );
        }

        return text.trim();
    }

    public Map<String, Object> requiredObject(
            Job job,
            String field
    ) {

        Object value = job.getPayload() == null
                ? null
                : job.getPayload().get(field);

        if (!(value instanceof Map<?, ?> rawMap)) {
            throw new InvalidJobPayloadException(
                    job.getId(),
                    field + " must be a JSON object"
            );
        }

        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {

            if (!(entry.getKey() instanceof String key)) {
                throw new InvalidJobPayloadException(
                        job.getId(),
                        field + " contains an invalid key"
                );
            }

            result.put(key, entry.getValue());
        }

        return result;
    }

    public <T> void validate(UUID jobId, T payload) {

        Set<ConstraintViolation<T>> violations =
                validator.validate(payload);

        if (!violations.isEmpty()) {

            String details = violations.stream()
                    .sorted(
                            Comparator.comparing(
                                    violation ->
                                            violation.getPropertyPath().toString()
                            )
                    )
                    .map(
                            violation ->
                                    violation.getPropertyPath()
                                            + ": "
                                            + violation.getMessage()
                    )
                    .collect(Collectors.joining("; "));

            throw new InvalidJobPayloadException(
                    jobId,
                    details
            );
        }
    }
}