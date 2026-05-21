package com.cromp.iam.api.service;

import com.cromp.iam.api.dto.request.CreateOrganizationRequest;
import com.cromp.iam.api.dto.request.RenameOrganizationRequest;
import com.cromp.iam.api.dto.response.OrganizationResponse;
import com.cromp.iam.api.dto.response.MembershipResponse;

import java.util.List;
import java.util.UUID;

public interface OrganizationFacade {
    OrganizationResponse create(CreateOrganizationRequest request, Long creatorUserId);
    OrganizationResponse rename(UUID orgUuid, RenameOrganizationRequest request);
    OrganizationResponse getById(UUID orgUuid);
    List<MembershipResponse> getMembers(UUID orgUuid);
}