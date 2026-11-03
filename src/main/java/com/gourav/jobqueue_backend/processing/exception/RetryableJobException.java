package com.gourav.jobqueue_backend.processing.exception;

public class RetryableJobException extends JobProcessingException {

    public RetryableJobException(String message) {
        super(message, true);
    }
}