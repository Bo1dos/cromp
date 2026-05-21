package com.cromp.iam.application.service;

import com.cromp.iam.api.dto.request.CreateOrganizationRequest;
import com.cromp.iam.api.dto.request.RenameOrganizationRequest;
import com.cromp.iam.api.dto.response.MembershipResponse;
import com.cromp.iam.api.dto.response.OrganizationResponse;
import com.cromp.iam.api.mapper.MembershipApiMapper;
import com.cromp.iam.api.mapper.OrganizationApiMapper;
import com.cromp.iam.api.service.OrganizationFacade;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.iam.domain.model.Membership;
import com.cromp.iam.domain.model.Organization;
import com.cromp.iam.domain.model.Role;
import com.cromp.iam.domain.model.enums.UserRole;
import com.cromp.iam.domain.model.exceptions.DomainException;
import com.cromp.iam.domain.repository.MembershipRepositoryPort;
import com.cromp.iam.domain.repository.OrganizationRepositoryPort;
import com.cromp.iam.domain.repository.RoleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationApplicationService implements OrganizationFacade {

    private final OrganizationRepositoryPort organizationRepository;
    private final MembershipRepositoryPort membershipRepository;
    private final RoleRepositoryPort roleRepository;
    private final OrganizationApiMapper organizationMapper;
    private final MembershipApiMapper membershipMapper;
    private final CurrentActorPort currentActorPort;
    private final PermissionCheckerPort permissionCheckerPort;

    @Override
    public OrganizationResponse create(CreateOrganizationRequest request, Long creatorUserId) {
        Organization org = Organization.create(request.name(), request.settings());
        org = organizationRepository.save(org);

        Role ownerRole = roleRepository.findByName(UserRole.OWNER)
                .orElseThrow(() -> new IllegalStateException("OWNER role not found"));
        Membership membership = Membership.join(creatorUserId, org.getId(), ownerRole.getId());
        membershipRepository.save(membership);

        return organizationMapper.toResponse(org);
    }

    @Override
    public OrganizationResponse rename(UUID orgUuid, RenameOrganizationRequest request) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Organization organization = organizationRepository.findByOrgUuid(orgUuid)
                .orElseThrow(() -> new DomainException("Organization not found"));
        ensureCurrentOrganizationMatches(organization.getId());
        if (!permissionCheckerPort.hasPermission(currentUserId, organization.getId(), "org:update")) {
            throw new SecurityException("No permission to update this organization");
        }
        organization.rename(request.name());
        return organizationMapper.toResponse(organizationRepository.save(organization));
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getById(UUID orgUuid) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Organization organization = organizationRepository.findByOrgUuid(orgUuid)
                .orElseThrow(() -> new DomainException("Organization not found"));
        if (!permissionCheckerPort.isMember(currentUserId, organization.getId())) {
            throw new SecurityException("Not a member of this organization");
        }
        return organizationMapper.toResponse(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipResponse> getMembers(UUID orgUuid) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Organization organization = organizationRepository.findByOrgUuid(orgUuid)
                .orElseThrow(() -> new DomainException("Organization not found"));
        if (!permissionCheckerPort.isMember(currentUserId, organization.getId())) {
            throw new SecurityException("Not a member of this organization");
        }
        return membershipRepository.findByOrganizationId(organization.getId()).stream()
                .map(membershipMapper::toResponse)
                .toList();
    }

    private void ensureCurrentOrganizationMatches(Long organizationId) {
        Long currentOrganizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("No organization selected"));
        if (!currentOrganizationId.equals(organizationId)) {
            throw new SecurityException("Organization does not match current context");
        }
    }
}
