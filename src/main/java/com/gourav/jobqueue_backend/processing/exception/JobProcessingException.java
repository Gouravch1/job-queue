package com.gourav.jobqueue_backend.processing.exception;

public abstract class JobProcessingException extends RuntimeException {

    private final boolean retryable;

    protected JobProcessingException(
            String message,
            boolean retryable
    ) {
        super(message);
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}