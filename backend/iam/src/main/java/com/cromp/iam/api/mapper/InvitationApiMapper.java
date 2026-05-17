package com.cromp.iam.api.mapper;

import com.cromp.iam.api.dto.response.InvitationResponse;
import com.cromp.iam.domain.model.Invitation;
import org.springframework.stereotype.Component;

@Component
public class InvitationApiMapper {

    public InvitationResponse toResponse(Invitation invitation) {
        return toResponse(invitation, null);
    }

    public InvitationResponse toResponse(Invitation invitation, String roleName) {
        return new InvitationResponse(
                invitation.getInvitationUuid(),
                invitation.getEmail(),
                roleName,
                invitation.getExpiresAt(),
                invitation.getCreatedAt(),
                invitation.getAcceptedAt(),
                invitation.getStatus().name()
        );
    }
}