package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record UpdateUserProfileRequest(
        @NotNull Long userId,
        @Size(max = 150) String firstName,
        @Size(max = 150) String lastName,
        @Size(max = 150) String middleName,
        @Size(max = 255) String displayName,
        Map<String, Object> profile
) {}