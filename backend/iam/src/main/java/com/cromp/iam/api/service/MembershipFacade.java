package com.cromp.iam.api.service;

import com.cromp.iam.api.dto.request.AddMembershipRequest;
import com.cromp.iam.api.dto.request.ChangeMembershipRoleRequest;
import com.cromp.iam.api.dto.request.RemoveMembershipRequest;
import com.cromp.iam.api.dto.response.MembershipResponse;

import java.util.List;

public interface MembershipFacade {
    MembershipResponse add(AddMembershipRequest request);
    MembershipResponse changeRole(ChangeMembershipRoleRequest request);
    void remove(RemoveMembershipRequest request);

    MembershipResponse getById(Long membershipId);
    List<MembershipResponse> getByOrganization(Long organizationId);
    List<MembershipResponse> getByUser(Long userId);
}