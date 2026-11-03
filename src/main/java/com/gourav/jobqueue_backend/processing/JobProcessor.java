package com.gourav.jobqueue_backend.processing;

import com.gourav.jobqueue_backend.entity.Job;

public interface JobProcessor {

    // Identifies which job type this processor handles
    String getJobType();

    // Validates job-specific payload
    void validate(Job job);

    // Executes the job
    void process(Job job);
}