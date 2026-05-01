package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record ChangeUserEmailRequest(
        @NotNull Long userId,
        @Email String newEmail
) {}