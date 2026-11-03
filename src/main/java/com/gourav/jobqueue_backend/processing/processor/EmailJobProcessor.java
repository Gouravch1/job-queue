package com.gourav.jobqueue_backend.processing.processor;

import com.gourav.jobqueue_backend.entity.Job;
import com.gourav.jobqueue_backend.processing.JobProcessor;
import com.gourav.jobqueue_backend.processing.JobTypes;
import com.gourav.jobqueue_backend.processing.payload.EmailJobPayload;
import com.gourav.jobqueue_backend.processing.validation.JobPayloadValidationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailJobProcessor implements JobProcessor {

    private final JobPayloadValidationService payloadValidationService;

    @Override
    public String getJobType() {
        return JobTypes.EMAIL;
    }

    private EmailJobPayload readAndValidate(Job job) {

        EmailJobPayload payload = new EmailJobPayload(

                payloadValidationService.requiredString(job, "to"),

                payloadValidationService.requiredString(job, "subject"),

                payloadValidationService.requiredString(job, "message")
        );

        payloadValidationService.validate(
                job.getId(),
                payload
        );

        return payload;
    }

    @Override
    public void validate(Job job) {
        readAndValidate(job);
    }

    @Override
    public void process(Job job) {

        EmailJobPayload payload = readAndValidate(job);

        log.info(
                "SIMULATED EMAIL PROCESSING: jobId={}, subjectLength={}",
                job.getId(),
                payload.subject().length()
        );

        /*
         * Actual email provider integration will be added later.
         * For now, this processor validates and simulates execution.
         */
    }
}