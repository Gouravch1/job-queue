package com.gourav.jobqueue_backend.processing;

import com.gourav.jobqueue_backend.processing.exception.UnsupportedJobTypeException;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class JobProcessorRegistry {

    private final Map<String, JobProcessor> processors;

    public JobProcessorRegistry(List<JobProcessor> discoveredProcessors) {

        Map<String, JobProcessor> registry = new HashMap<>();

        for (JobProcessor processor : discoveredProcessors) {

            String type = processor.getJobType();

            if (type == null || type.isBlank()) {
                throw new IllegalStateException(
                        "Processor job type cannot be empty"
                );
            }

            String normalizedType = type.trim().toUpperCase(Locale.ROOT);

            JobProcessor existing = registry.putIfAbsent(
                    normalizedType,
                    processor
            );

            if (existing != null) {
                throw new IllegalStateException(
                        "Duplicate processor registered for job type: "
                                + normalizedType
                );
            }
        }

        this.processors = Map.copyOf(registry);
    }

    public JobProcessor getProcessor(String jobType) {

        String normalizedType = jobType == null
                ? ""
                : jobType.trim().toUpperCase(Locale.ROOT);

        JobProcessor processor = processors.get(normalizedType);

        if (processor == null) {
            throw new UnsupportedJobTypeException(jobType);
        }

        return processor;
    }

    public List<String> getSupportedTypes() {

        return processors.keySet()
                .stream()
                .sorted()
                .toList();
    }
}