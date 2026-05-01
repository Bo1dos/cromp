package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record RemoveMembershipRequest(
        @NotNull Long membershipId
) {}