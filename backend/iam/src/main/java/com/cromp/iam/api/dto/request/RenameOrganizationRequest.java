package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameOrganizationRequest(
        @NotBlank @Size(max = 255) String name
) {}