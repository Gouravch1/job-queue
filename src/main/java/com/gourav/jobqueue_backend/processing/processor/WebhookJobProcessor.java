package com.gourav.jobqueue_backend.processing.processor;

import com.gourav.jobqueue_backend.entity.Job;
import com.gourav.jobqueue_backend.processing.JobProcessor;
import com.gourav.jobqueue_backend.processing.JobTypes;
import com.gourav.jobqueue_backend.processing.exception.InvalidJobPayloadException;
import com.gourav.jobqueue_backend.processing.payload.WebhookJobPayload;
import com.gourav.jobqueue_backend.processing.validation.JobPayloadValidationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookJobProcessor implements JobProcessor {

    private final JobPayloadValidationService payloadValidationService;

    @Override
    public String getJobType() {
        return JobTypes.WEBHOOK;
    }

    private WebhookJobPayload readAndValidate(Job job) {

        String url = payloadValidationService.requiredString(
                job,
                "url"
        );

        String method = payloadValidationService.requiredString(
                job,
                "method"
        ).toUpperCase(Locale.ROOT);

        Map<String, Object> body =
                payloadValidationService.requiredObject(
                        job,
                        "body"
                );

        WebhookJobPayload payload = new WebhookJobPayload(
                url,
                method,
                body
        );

        payloadValidationService.validate(
                job.getId(),
                payload
        );

        validateUrl(job, payload.url());

        return payload;
    }

    private void validateUrl(Job job, String url) {

        URI uri;

        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException ex) {

            throw new InvalidJobPayloadException(
                    job.getId(),
                    "Webhook URL is malformed"
            );
        }

        String scheme = uri.getScheme();

        boolean validScheme =
                "http".equalsIgnoreCase(scheme)
                        || "https".equalsIgnoreCase(scheme);

        if (!validScheme
                || uri.getHost() == null
                || uri.getUserInfo() != null) {

            throw new InvalidJobPayloadException(
                    job.getId(),
                    "Webhook URL must be a valid HTTP or HTTPS URL without embedded credentials"
            );
        }
    }

    @Override
    public void validate(Job job) {
        readAndValidate(job);
    }

    @Override
    public void process(Job job) {

        WebhookJobPayload payload = readAndValidate(job);

        log.info(
                "SIMULATED WEBHOOK PROCESSING: jobId={}, method={}",
                job.getId(),
                payload.method()
        );

        /*
         * Actual HTTP request execution will be implemented later.
         * URL validation and processor selection are ready.
         */
    }
}