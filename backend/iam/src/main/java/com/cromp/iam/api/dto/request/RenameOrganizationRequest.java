package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RenameOrganizationRequest(
        @NotNull Long organizationId,
        @NotBlank @Size(max = 255) String name
) {}