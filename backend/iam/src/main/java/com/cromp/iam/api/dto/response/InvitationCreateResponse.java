package com.cromp.iam.api.dto.response;

public record InvitationCreateResponse(
        InvitationResponse invitation,
        String invitationToken
) {}