package com.cromp.iam.infrastructure.security;

import com.cromp.iam.application.port.OrganizationLookupPort;
import com.cromp.iam.domain.repository.OrganizationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrganizationLookupPortImpl implements OrganizationLookupPort {

    private final OrganizationRepositoryPort organizationRepository;

    @Override
    public Optional<Long> resolveOrganizationId(UUID orgUuid) {
        return organizationRepository.findByOrgUuid(orgUuid)
                .map(org -> org.getId());
    }
}
