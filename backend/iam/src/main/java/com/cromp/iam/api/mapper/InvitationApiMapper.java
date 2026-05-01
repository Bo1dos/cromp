package com.cromp.iam.api.mapper;

import com.cromp.iam.api.dto.response.InvitationResponse;
import com.cromp.iam.domain.model.Invitation;
import org.springframework.stereotype.Component;

@Component
public class InvitationApiMapper {

    public InvitationResponse toResponse(Invitation invitation) {
        return toResponse(invitation, null, null);
    }

    public InvitationResponse toResponse(Invitation invitation, String roleName) {
        return toResponse(invitation, roleName, null);
    }

    public InvitationResponse toResponse(Invitation invitation, String roleName, String invitationToken) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getOrganizationId(),
                invitation.getEmail(),
                invitation.getRoleId(),
                roleName,
                invitation.getInvitedBy(),
                invitation.getExpiresAt(),
                invitation.getCreatedAt(),
                invitation.getAcceptedAt(),
                invitation.getStatus().name(),
                invitationToken
        );
    }
}