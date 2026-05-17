package com.cromp.iam.api.mapper;

import com.cromp.iam.api.dto.response.MembershipResponse;
import com.cromp.iam.domain.model.Membership;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MembershipApiMapper {

    public MembershipResponse toResponse(Membership membership) {
        return toResponse(membership, null, membership.getCreatedAt());
    }

    public MembershipResponse toResponse(Membership membership, String roleName) {
        return toResponse(membership, roleName, membership.getCreatedAt());
    }

    public MembershipResponse toResponse(Membership membership, String roleName, Instant joinedAt) {
        return new MembershipResponse(
                membership.getMembershipUuid(),
                roleName,
                joinedAt,
                membership.getUpdatedAt()
        );
    }
}