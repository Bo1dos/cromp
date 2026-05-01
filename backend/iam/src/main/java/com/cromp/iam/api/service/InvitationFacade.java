package com.cromp.iam.api.service;

import com.cromp.iam.api.dto.request.AcceptInvitationRequest;
import com.cromp.iam.api.dto.request.InviteUserRequest;
import com.cromp.iam.api.dto.request.RejectInvitationRequest;
import com.cromp.iam.api.dto.request.RevokeInvitationRequest;
import com.cromp.iam.api.dto.response.InvitationResponse;

import java.util.List;

public interface InvitationFacade {
    InvitationResponse invite(InviteUserRequest request);
    InvitationResponse accept(AcceptInvitationRequest request);
    InvitationResponse reject(RejectInvitationRequest request);
    InvitationResponse revoke(RevokeInvitationRequest request);

    InvitationResponse getById(Long invitationId);
    InvitationResponse getByRawToken(String rawToken);
    InvitationResponse getByTokenHash(String tokenHash);
    List<InvitationResponse> getByOrganization(Long organizationId);
}