package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 255) String password,
        @NotBlank @Size(max = 255) String organizationName,
        @Size(max = 150) String firstName,
        @Size(max = 150) String lastName,
        @Size(max = 150) String middleName,
        @Size(max = 255) String displayName
) {}