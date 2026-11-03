package com.gourav.jobqueue_backend.processing.exception;

public class UnsupportedJobTypeException extends PermanentJobException {

    public UnsupportedJobTypeException(String type) {
        super("Unsupported job type: " + type);
    }
}