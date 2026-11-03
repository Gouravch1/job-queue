package com.gourav.jobqueue_backend.processing.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailJobPayload(

        @NotBlank(message = "Recipient email is required")
        @Email(message = "Recipient email is invalid")
        @Size(max = 254)
        String to,

        @NotBlank(message = "Email subject is required")
        @Size(max = 200)
        String subject,

        @NotBlank(message = "Email message is required")
        @Size(max = 10000)
        String message

) {
}