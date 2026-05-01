package com.cromp.iam.api.service;

import com.cromp.iam.api.dto.request.CreateOrganizationRequest;
import com.cromp.iam.api.dto.request.RenameOrganizationRequest;
import com.cromp.iam.api.dto.response.OrganizationResponse;
import com.cromp.iam.api.dto.response.MembershipResponse;

import java.util.List;

public interface OrganizationFacade {
    OrganizationResponse create(CreateOrganizationRequest request, Long creatorUserId);
    // OrganizationResponse create(CreateOrganizationRequest request);
    OrganizationResponse rename(RenameOrganizationRequest request);
    OrganizationResponse getById(Long organizationId);
    List<MembershipResponse> getMembers(Long organizationId);
}