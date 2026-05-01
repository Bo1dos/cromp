package com.cromp.iam.api.mapper;

import com.cromp.iam.api.dto.response.OrganizationResponse;
import com.cromp.iam.domain.model.Organization;
import org.springframework.stereotype.Component;

@Component
public class OrganizationApiMapper {

    public OrganizationResponse toResponse(Organization organization) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getOrgUuid(),
                organization.getName(),
                organization.getSettings(),
                organization.getCreatedAt(),
                organization.getUpdatedAt(),
                organization.getDeletedAt()
        );
    }
}