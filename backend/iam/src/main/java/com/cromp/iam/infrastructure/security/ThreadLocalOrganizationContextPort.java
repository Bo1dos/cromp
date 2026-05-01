package com.cromp.iam.infrastructure.security;

import com.cromp.iam.application.port.OrganizationContextPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ThreadLocalOrganizationContextPort implements OrganizationContextPort {

    private final ThreadLocal<Long> organizationIdHolder = new ThreadLocal<>();

    @Override
    public Optional<Long> currentOrganizationId() {
        return Optional.ofNullable(organizationIdHolder.get());
    }

    @Override
    public void setCurrentOrganizationId(Long organizationId) {
        organizationIdHolder.set(organizationId);
    }

    @Override
    public void clear() {
        organizationIdHolder.remove();
    }
}