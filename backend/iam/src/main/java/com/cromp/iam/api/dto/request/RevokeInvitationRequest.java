package com.cromp.iam.api.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record RevokeInvitationRequest(
        @NotNull UUID invitationUuid
) {}