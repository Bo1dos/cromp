package com.cromp.iam.application.port;

import java.util.Optional;

public interface OrganizationContextPort {
    Optional<Long> currentOrganizationId();
    void setCurrentOrganizationId(Long organizationId);
    void clear();
}