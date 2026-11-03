package com.gourav.jobqueue_backend.service;

import com.gourav.jobqueue_backend.dto.Request.CreateJobRequest;
import com.gourav.jobqueue_backend.dto.Response.JobResponse;
import com.gourav.jobqueue_backend.entity.Job;
import com.gourav.jobqueue_backend.exception.DuplicateIdempotencyKeyException;
import com.gourav.jobqueue_backend.exception.JobNotFoundException;
import com.gourav.jobqueue_backend.processing.JobProcessingService;
import com.gourav.jobqueue_backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobService {
    private final JobRepository jobRepository;
    private final JobProcessingService jobProcessingService;

    private static final int DEFAULT_PRIORITY = 0;
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final int MAX_PAGE_SIZE = 100;

    // Create a new job
    @Transactional
    public JobResponse createJob(CreateJobRequest request){
        String idempotencyKey = request.idempotencyKey();
        if(idempotencyKey != null && jobRepository.existsByIdempotencyKey(idempotencyKey)){
            throw new DuplicateIdempotencyKeyException(idempotencyKey);
        }

        int priority = request.priority() != null ? request.priority() : DEFAULT_PRIORITY;
        int maxAttempts = request.maxAttempts() != null ? request.maxAttempts() : DEFAULT_MAX_ATTEMPTS;

        Job job = new Job(
                request.type(),
                request.payload(),
                priority,
                maxAttempts,
                request.availableAt(),
                idempotencyKey
        );
         jobProcessingService.validate(job);
        Job savedJob = jobRepository.saveAndFlush(job);
        return JobResponse.from(savedJob);
    }

    // Retrive a job by UUID
    public JobResponse getJobById(UUID id){
        Job job = jobRepository.findById(id).orElseThrow( () -> new JobNotFoundException(id));
        return JobResponse.from(job);
    }

    // Retrieve jobs with pagination
    public Page<JobResponse> getAllJobs(Pageable pageable) {

        int pageSize = Math.max(
                1,
                Math.min(pageable.getPageSize(), MAX_PAGE_SIZE)
        );

        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageSize,
                pageable.getSort()
        );

        Page<Job> jobs = jobRepository.findAll(safePageable);

        return jobs.map(JobResponse::from);
    }
}
