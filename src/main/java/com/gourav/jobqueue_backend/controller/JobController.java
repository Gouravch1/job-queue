package com.gourav.jobqueue_backend.controller;


import com.gourav.jobqueue_backend.dto.Request.CreateJobRequest;
import com.gourav.jobqueue_backend.dto.Response.JobResponse;
import com.gourav.jobqueue_backend.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    // POST /api/v1/jobs
    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody CreateJobRequest request){
          JobResponse createdJob = jobService.createJob(request);
          URI location = URI.create("api/v1/jobs/"+ createdJob.id());
          return  ResponseEntity.created(location).body(createdJob);
    }

    // GET /api/v1/jobs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable UUID id){
        JobResponse job = jobService.getJobById(id);
        return ResponseEntity.ok(job);
    }

    // GET /api/v1/jobs?page=0&size=20
    @GetMapping
    public ResponseEntity<Page<JobResponse>> getAllJobs(@PageableDefault(
            size = 20,
            sort = "createdAt",
            direction = Sort.Direction.DESC
    )Pageable pageable){
        Page<JobResponse> jobs = jobService.getAllJobs(pageable);
        return ResponseEntity.ok(jobs);
    }
}
