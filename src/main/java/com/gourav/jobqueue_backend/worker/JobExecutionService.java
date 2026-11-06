package com.gourav.jobqueue_backend.worker;

import com.gourav.jobqueue_backend.entity.Job;
import com.gourav.jobqueue_backend.exception.JobNotFoundException;
import com.gourav.jobqueue_backend.processing.JobFailureClassifier;
import com.gourav.jobqueue_backend.processing.JobFailureOutcome;
import com.gourav.jobqueue_backend.processing.RetryPolicy;
import com.gourav.jobqueue_backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobExecutionService {

    private final JobRepository jobRepository;
    private final JobFailureClassifier failureClassifier;
    private final RetryPolicy retryPolicy;

    @Transactional
    public void markSucceeded(UUID jobId, String workerId) {

        Job job = findJob(jobId);

        job.markSucceeded(workerId);

        log.info(
                "Job {} marked as SUCCEEDED by worker {}",
                jobId,
                workerId
        );
    }

    @Transactional
    public JobFailureOutcome handleFailure(
            UUID jobId,
            String workerId,
            Exception exception
    ) {

        Job job = findJob(jobId);

        String error = buildErrorMessage(exception);

        // Case 1: Permanent failure. Do not retry.
        if (!failureClassifier.isRetryable(exception)) {

            job.markFailed(workerId, error);

            log.warn(
                    "Job {} permanently failed. Worker: {}. Error: {}",
                    jobId,
                    workerId,
                    error
            );

            return JobFailureOutcome.PERMANENT_FAILURE;
        }

        // Case 2: Maximum attempts exhausted. Move to DLQ.
        if (job.getAttemptCount() >= job.getMaxAttempts()) {

            job.moveToDlq(workerId, error);

            log.error(
                    "Job {} moved to DLQ after {} attempts",
                    jobId,
                    job.getAttemptCount()
            );

            return JobFailureOutcome.SENT_TO_DLQ;
        }

        // Case 3: Retry is available. Calculate the next retry time.
        Instant nextAttemptAt = Instant.now().plus(
                retryPolicy.nextDelay(job.getAttemptCount())
        );

        job.scheduleRetry(workerId, error, nextAttemptAt);

        log.warn(
                "Job {} scheduled for retry. Attempt: {}/{}. Next attempt: {}",
                jobId,
                job.getAttemptCount(),
                job.getMaxAttempts(),
                nextAttemptAt
        );

        return JobFailureOutcome.RETRY_SCHEDULED;
    }

    private Job findJob(UUID jobId) {

        return jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
    }

    private String buildErrorMessage(Exception exception) {

        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            message = "No error message provided";
        }

        String error = exception.getClass().getSimpleName() + ": " + message;

        return error.length() > 4000
                ? error.substring(0, 4000)
                : error;
    }
}