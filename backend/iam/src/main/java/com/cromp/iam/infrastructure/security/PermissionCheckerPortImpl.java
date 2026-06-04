package com.cromp.iam.infrastructure.security;

import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.iam.application.port.OrganizationContextPort;
import com.cromp.iam.domain.model.Membership;
import com.cromp.iam.domain.model.Role;
import com.cromp.iam.domain.model.RolePermission;
import com.cromp.iam.domain.model.enums.UserRole;
import com.cromp.iam.domain.repository.MembershipRepositoryPort;
import com.cromp.iam.domain.repository.RolePermissionRepositoryPort;
import com.cromp.iam.domain.repository.RoleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("permissionCheckerPort")
@RequiredArgsConstructor
public class PermissionCheckerPortImpl implements PermissionCheckerPort {

    private final MembershipRepositoryPort membershipRepository;
    private final RolePermissionRepositoryPort rolePermissionRepository;
    private final RoleRepositoryPort roleRepository;
    private final OrganizationContextPort organizationContextPort;

    @Override
    public boolean hasPermission(Object principal, String permission) {
        if (principal instanceof Long userId) {
            return organizationContextPort.currentOrganizationId()
                    .map(organizationId -> hasPermission(userId, organizationId, permission))
                    .orElse(false);
        }
        return false;
    }

    @Override
    public boolean hasPermission(Long userId, 
                                Long organizationId, 
                                String permission) {

        Optional<Membership> membershipOpt = membershipRepository.findByUserIdAndOrganizationId(userId, organizationId);
        if (membershipOpt.isEmpty()) {
            return false;
        }
        Membership membership = membershipOpt.get();

        Optional<Role> roleOpt = roleRepository.findById(membership.getRoleId());
        if (roleOpt.isPresent() && roleOpt.get().getName() == UserRole.OWNER) {
            return true; 
        }

        return rolePermissionRepository.findByRoleId(membership.getRoleId()).stream()
                .map(RolePermission::getPermission)
                .anyMatch(p -> p.equals(permission));
    }

    @Override
    public boolean isMember(Long userId, Long organizationId) {
        return membershipRepository.findByUserIdAndOrganizationId(userId, organizationId).isPresent();
    }
}
