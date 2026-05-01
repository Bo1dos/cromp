package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record SelectOrganizationRequest(
        @NotNull Long organizationId
) {}