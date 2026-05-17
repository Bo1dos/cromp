package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.Email;

public record ChangeUserEmailRequest(
        @Email String newEmail
) {}