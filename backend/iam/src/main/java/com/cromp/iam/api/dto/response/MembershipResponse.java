package com.cromp.iam.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Членство пользователя в организации")
public record MembershipResponse(
        @Schema(description = "UUID членства")
        UUID membershipUuid,
        @Schema(description = "Название роли")
        String roleName,
        @Schema(description = "Организация")
        OrganizationResponse organization,
        @Schema(description = "UUID пользователя")
        String userUuid,
        @Schema(description = "Имя пользователя")
        String userName,
        @Schema(description = "Email пользователя")
        String userEmail,
        @Schema(description = "Дата вступления")
        Instant joinedAt,
        @Schema(description = "Дата обновления")
        Instant updatedAt
) {}