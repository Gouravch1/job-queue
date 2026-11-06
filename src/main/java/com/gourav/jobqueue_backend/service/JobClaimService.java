package com.gourav.jobqueue_backend.service;

import com.gourav.jobqueue_backend.entity.Job;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class JobClaimService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String CLAIM_SQL = """
            SELECT id
            FROM jobs
            WHERE status IN ('QUEUED', 'RETRY_WAIT')
              AND available_at <= CURRENT_TIMESTAMP
            ORDER BY
                priority DESC,
                available_at ASC,
                created_at ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """;

    @Transactional
    public Optional<Job> claimNext(String workerId) {

        //  Find and lock one available job
        List<?> results = entityManager
                .createNativeQuery(CLAIM_SQL)
                .getResultList();

        if (results.isEmpty()) {
            return Optional.empty();
        }

        // Extract UUID
        Object rawId = results.get(0);

        UUID jobId = rawId instanceof UUID uuid
                ? uuid
                : UUID.fromString(rawId.toString());

        // Load the locked entity
        Job job = entityManager.find(Job.class, jobId);

        if (job == null) {
            return Optional.empty();
        }

        // Mark the job as PROCESSING
        job.claim(workerId);

        log.info(
                "Job claimed: jobId={}, workerId={}",
                jobId,
                workerId
        );

        return Optional.of(job);
    }
}