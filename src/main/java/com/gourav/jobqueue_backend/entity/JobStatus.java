package com.gourav.jobqueue_backend.entity;

public enum JobStatus {

    QUEUED,
    PROCESSING,
    RETRY_WAIT,
    SUCCEEDED,
    FAILED,
    DLQ,
    CANCELLED

}

