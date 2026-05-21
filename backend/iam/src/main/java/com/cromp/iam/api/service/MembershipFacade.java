package com.cromp.iam.api.service;

import com.cromp.iam.api.dto.request.AddMembershipRequest;
import com.cromp.iam.api.dto.request.ChangeMembershipRoleRequest;
import com.cromp.iam.api.dto.response.MembershipResponse;

import java.util.List;
import java.util.UUID;

public interface MembershipFacade {
    MembershipResponse add(AddMembershipRequest request);
    MembershipResponse changeRole(UUID membershipUuid, ChangeMembershipRoleRequest request);
    void remove(UUID membershipUuid);

    MembershipResponse getById(UUID membershipUuid);
    List<MembershipResponse> getByOrganization(UUID orgUuid);
    List<MembershipResponse> getByUser(UUID userUuid);
}
