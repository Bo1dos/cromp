package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeUserPasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank @Size(min = 8, max = 255) String newPassword
) {}