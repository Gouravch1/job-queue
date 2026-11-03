package com.gourav.jobqueue_backend.processing;

import com.gourav.jobqueue_backend.entity.Job;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobProcessingService {

    private final JobProcessorRegistry processorRegistry;

    // Validate job before accepting it into the queue
    public void validate(Job job) {

        JobProcessor processor =
                processorRegistry.getProcessor(job.getType());

        processor.validate(job);
    }

    // Execute the selected processor
    public void process(Job job) {

        JobProcessor processor =
                processorRegistry.getProcessor(job.getType());

        processor.validate(job);

        processor.process(job);
    }

    // Return available processor types
    public List<String> getSupportedTypes() {
        return processorRegistry.getSupportedTypes();
    }
}