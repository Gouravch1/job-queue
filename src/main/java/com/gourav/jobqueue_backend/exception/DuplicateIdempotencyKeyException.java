package com.gourav.jobqueue_backend.exception;

public class DuplicateIdempotencyKeyException extends RuntimeException{
    public DuplicateIdempotencyKeyException(String key){
        super("Idempotency key already exists : " + key);
    }
}
