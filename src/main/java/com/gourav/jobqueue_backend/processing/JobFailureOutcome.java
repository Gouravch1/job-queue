package com.gourav.jobqueue_backend.processing;

public enum JobFailureOutcome {

    RETRY_SCHEDULED,

    SENT_TO_DLQ,

    PERMANENT_FAILURE
}