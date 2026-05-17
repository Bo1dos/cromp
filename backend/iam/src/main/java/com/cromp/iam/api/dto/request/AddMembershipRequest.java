package com.cromp.iam.api.dto.request;

import com.cromp.iam.domain.model.enums.UserRole;

import jakarta.validation.constraints.NotNull;

// TODO:  возможно, нужно огранизацию вернуть
public record AddMembershipRequest(
        @NotNull Long userId,
        @NotNull UserRole role
) {}