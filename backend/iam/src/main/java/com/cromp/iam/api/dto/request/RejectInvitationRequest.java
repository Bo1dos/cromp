package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejectInvitationRequest(
        @NotBlank String token
) {}