package com.gourav.jobqueue_backend.exception;

import com.gourav.jobqueue_backend.entity.Job;

import java.util.UUID;

public class JobNotFoundException extends RuntimeException{
    public JobNotFoundException(UUID id){
        super("job not found with id : " + id);
    }
}
