package com.cromp.iam.api.mapper;

import com.cromp.iam.api.dto.response.MembershipResponse;
import com.cromp.iam.api.dto.response.OrganizationResponse;
import com.cromp.iam.domain.model.Membership;
import com.cromp.iam.domain.model.Organization;
import com.cromp.iam.domain.model.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MembershipApiMapper {

    private final OrganizationApiMapper organizationMapper;

    public MembershipApiMapper(OrganizationApiMapper organizationMapper) {
        this.organizationMapper = organizationMapper;
    }

    public MembershipResponse toResponse(Membership membership) {
        return toResponse(membership, null, membership.getCreatedAt());
    }

    public MembershipResponse toResponse(Membership membership, String roleName) {
        return toResponse(membership, roleName, membership.getCreatedAt());
    }

    public MembershipResponse toResponse(Membership membership, String roleName, Instant joinedAt) {
        return toResponse(membership, roleName, null, null, joinedAt);
    }

    public MembershipResponse toResponse(Membership membership, String roleName,
                                          Organization organization, User user, Instant joinedAt) {
        OrganizationResponse orgResponse = organization != null
                ? organizationMapper.toResponse(organization) : null;
        return new MembershipResponse(
                membership.getMembershipUuid(),
                roleName,
                orgResponse,
                user != null ? user.getUserUuid().toString() : null,
                user != null ? user.getDisplayName() : null,
                user != null ? user.getEmail() : null,
                joinedAt,
                membership.getUpdatedAt()
        );
    }
}