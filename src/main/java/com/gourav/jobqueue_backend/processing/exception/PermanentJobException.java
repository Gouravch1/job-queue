package com.gourav.jobqueue_backend.processing.exception;

public class PermanentJobException extends JobProcessingException {

    public PermanentJobException(String message) {
        super(message, false, null);
    }
    public PermanentJobException(String message, Throwable cause) {
        super(message, false , cause);
    }
}