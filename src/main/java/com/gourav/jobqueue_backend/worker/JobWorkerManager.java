package com.gourav.jobqueue_backend.worker;

import com.gourav.jobqueue_backend.entity.Job;
import com.gourav.jobqueue_backend.processing.JobFailureOutcome;
import com.gourav.jobqueue_backend.processing.JobProcessingService;
import com.gourav.jobqueue_backend.service.JobClaimService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class JobWorkerManager implements SmartLifecycle {

    private final JobClaimService jobClaimService;
    private final JobProcessingService jobProcessingService;
    private final JobExecutionService jobExecutionService;

    private final boolean enabled;
    private final int concurrency;
    private final long pollIntervalMs;
    private final long shutdownWaitSeconds;

    private final String instanceId = UUID.randomUUID().toString();

    private final AtomicInteger threadCounter = new AtomicInteger(1);

    private volatile boolean running = false;

    private ExecutorService executorService;

    public JobWorkerManager(
            JobClaimService jobClaimService,
            JobProcessingService jobProcessingService,
            JobExecutionService jobExecutionService,

            @Value("${jobforge.worker.enabled:true}")
            boolean enabled,

            @Value("${jobforge.worker.concurrency:4}")
            int concurrency,

            @Value("${jobforge.worker.poll-interval-ms:1000}")
            long pollIntervalMs,

            @Value("${jobforge.worker.shutdown-wait-seconds:30}")
            long shutdownWaitSeconds
    ) {

        this.jobClaimService = jobClaimService;
        this.jobProcessingService = jobProcessingService;
        this.jobExecutionService = jobExecutionService;

        this.enabled = enabled;
        this.concurrency = concurrency;
        this.pollIntervalMs = pollIntervalMs;
        this.shutdownWaitSeconds = shutdownWaitSeconds;

        if (concurrency < 1) {
            throw new IllegalArgumentException(
                    "Worker concurrency must be at least 1"
            );
        }

        if (pollIntervalMs < 100) {
            throw new IllegalArgumentException(
                    "Poll interval must be at least 100 ms"
            );
        }
    }

    @Override
    public synchronized void start() {

        if (!enabled) {
            log.info("Job workers are disabled.");
            return;
        }

        if (running) {
            return;
        }

        ThreadFactory threadFactory = task -> {

            Thread thread = new Thread(
                    task,
                    "jobforge-worker-" + threadCounter.getAndIncrement()
            );

            thread.setUncaughtExceptionHandler(
                    (t, ex) -> log.error(
                            "Uncaught worker thread error: {}",
                            t.getName(),
                            ex
                    )
            );

            return thread;
        };

        executorService = Executors.newFixedThreadPool(
                concurrency,
                threadFactory
        );

        running = true;

        for (int i = 1; i <= concurrency; i++) {

            String workerId = instanceId + "-worker-" + i;

            executorService.submit(
                    () -> runWorker(workerId)
            );
        }

        log.info(
                "JobForge worker pool started: concurrency={}",
                concurrency
        );
    }

    private void runWorker(String workerId) {

        log.info("Worker started: {}", workerId);

        while (running && !Thread.currentThread().isInterrupted()) {

            try {

                Optional<Job> claimedJob =
                        jobClaimService.claimNext(workerId);

                if (claimedJob.isEmpty()) {

                    Thread.sleep(pollIntervalMs);

                    continue;
                }

                Job job = claimedJob.get();

                executeJob(job, workerId);

            } catch (InterruptedException ex) {

                Thread.currentThread().interrupt();

                break;

            } catch (Exception ex) {

                log.error(
                        "Worker loop failed: workerId={}",
                        workerId,
                        ex
                );

                try {
                    Thread.sleep(pollIntervalMs);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("Worker stopped: {}", workerId);
    }

    private void executeJob(Job job, String workerId) {

        // Only catch errors from the actual job processor here.
        try {

            jobProcessingService.process(job);

        } catch (Exception processingException) {

            log.error(
                    "Job {} failed on worker {}",
                    job.getId(),
                    workerId,
                    processingException
            );

            try {

                JobFailureOutcome outcome = jobExecutionService.handleFailure(
                        job.getId(),
                        workerId,
                        processingException
                );

                log.info(
                        "Job {} failure handled with outcome: {}",
                        job.getId(),
                        outcome
                );

            } catch (Exception handlingException) {

                log.error(
                        "Could not persist failure handling for job {}. " +
                                "The job may need crash recovery.",
                        job.getId(),
                        handlingException
                );
            }

            return;
        }

        jobExecutionService.markSucceeded(job.getId(), workerId);
    }

    @Override
    public synchronized void stop() {

        running = false;

        if (executorService == null) {
            return;
        }

        executorService.shutdown();

        try {

            boolean terminated = executorService.awaitTermination(
                    shutdownWaitSeconds,
                    TimeUnit.SECONDS
            );

            if (!terminated) {

                log.warn(
                        "Workers did not stop in time. Interrupting remaining tasks."
                );

                executorService.shutdownNow();

                executorService.awaitTermination(
                        5,
                        TimeUnit.SECONDS
                );
            }

        } catch (InterruptedException ex) {

            executorService.shutdownNow();

            Thread.currentThread().interrupt();
        }

        log.info("JobForge worker pool stopped.");
    }

    @Override
    public void stop(Runnable callback) {

        try {
            stop();
        } finally {
            callback.run();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}