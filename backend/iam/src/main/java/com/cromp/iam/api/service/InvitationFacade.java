package com.cromp.iam.api.service;

import com.cromp.iam.api.dto.request.AcceptInvitationRequest;
import com.cromp.iam.api.dto.request.InviteUserRequest;
import com.cromp.iam.api.dto.request.RejectInvitationRequest;
import com.cromp.iam.api.dto.request.RevokeInvitationRequest;
import com.cromp.iam.api.dto.response.InvitationCreateResponse;
import com.cromp.iam.api.dto.response.InvitationResponse;

import java.util.List;
import java.util.UUID;

public interface InvitationFacade {
    InvitationCreateResponse invite(InviteUserRequest request);
    InvitationResponse accept(AcceptInvitationRequest request);
    void reject(RejectInvitationRequest request);
    InvitationResponse revoke(RevokeInvitationRequest request);

    InvitationResponse getById(UUID invitationUuid);
    InvitationResponse acceptByUuid(UUID invitationUuid);
    void rejectByUuid(UUID invitationUuid);
    List<InvitationResponse> getByOrganization(UUID orgUuid);
    List<InvitationResponse> getMyInvitations();
}
