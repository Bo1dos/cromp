package com.cromp.iam.infrastructure.security;

import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.OrganizationContextPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SpringSecurityCurrentActorPort implements CurrentActorPort {

    private final OrganizationContextPort organizationContextPort;

    @Override
    public Optional<Long> currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Long userId) {
            return Optional.of(userId);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Long> currentOrganizationId() {
        return organizationContextPort.currentOrganizationId();
    }

    @Override
    public Optional<Map<String, Object>> currentActorSnapshot() {
        // Пока заглушка, но можно расширить
        return Optional.empty();
    }
}