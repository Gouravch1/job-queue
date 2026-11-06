package com.gourav.jobqueue_backend.processing;

import com.gourav.jobqueue_backend.processing.exception.JobProcessingException;
import org.springframework.stereotype.Component;

@Component
public class JobFailureClassifier {

    public boolean isRetryable(Throwable exception){
        Throwable current = exception;

        while(current != null){
            if(current instanceof JobProcessingException jobException){
                return jobException.isRetryable();
            }

            current = current.getCause();
        }

        // if exceptions are unknown then it is retryable by default.
        return true;
    }
}
