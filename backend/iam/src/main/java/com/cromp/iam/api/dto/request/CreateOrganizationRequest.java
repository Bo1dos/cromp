package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateOrganizationRequest(
        @NotBlank @Size(max = 255) String name,
        Map<String, Object> settings
) {}