package com.gourav.jobqueue_backend.processing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RetryPolicy {
    private final long baseDelayMs;
    private final long maxDelayMs;

    public RetryPolicy(
            @Value("${jobforge.retry.base-delay-ms:5000}") long baseDelayMs,
            @Value("${jobforge.retry.max-delay-ms:300000}") long maxDelayMs
    ) {
        if (baseDelayMs <= 0 || maxDelayMs < baseDelayMs) {
            throw new IllegalArgumentException(
                    "Retry delay configuration is invalid"
            );
        }
        this.baseDelayMs = baseDelayMs;
        this.maxDelayMs = maxDelayMs;
    }

    public Duration nextDelay(int attemptCount){
        int attempt = Math.max(1 , attemptCount);
        long exponentialDelay = baseDelayMs;

        for(int i = 1; i < attempt && exponentialDelay < maxDelayMs; i++){
            if(exponentialDelay > maxDelayMs / 2) exponentialDelay = maxDelayMs;
            else exponentialDelay *= 2;
        }

        long cappedDelay = Math.min(exponentialDelay, maxDelayMs);

        // Equal jitter: choose a random delay between 50% and 100%.
        long minimumDelay = Math.max(1, cappedDelay / 2);

        long actualDelay = ThreadLocalRandom.current().nextLong(
                minimumDelay,
                cappedDelay + 1
        );

        return Duration.ofMillis(actualDelay);
    }
}
