package com.gourav.jobqueue_backend.processing.exception;

import java.util.UUID;

public class InvalidJobPayloadException extends PermanentJobException {

    public InvalidJobPayloadException(UUID jobId, String details) {
        super(
                "Invalid payload for job " + jobId + " : " + details
        );
    }
}