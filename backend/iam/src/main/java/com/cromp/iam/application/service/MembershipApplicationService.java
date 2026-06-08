package com.cromp.iam.application.service;

import com.cromp.iam.api.dto.request.AddMembershipRequest;
import com.cromp.iam.api.dto.request.ChangeMembershipRoleRequest;
import com.cromp.iam.api.dto.response.MembershipResponse;
import com.cromp.iam.api.mapper.MembershipApiMapper;
import com.cromp.iam.api.service.MembershipFacade;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.iam.domain.model.Membership;
import com.cromp.iam.domain.model.Organization;
import com.cromp.iam.domain.model.Role;
import com.cromp.iam.domain.model.User;
import com.cromp.iam.domain.model.exceptions.DomainException;
import com.cromp.common.event.integration.publisher.DomainEventPublisher;
import com.cromp.common.event.domain.membership.MemberAddedEvent;
import com.cromp.common.event.domain.membership.MemberRemovedEvent;
import com.cromp.common.event.domain.membership.MemberRoleChangedEvent;
import com.cromp.iam.domain.repository.MembershipRepositoryPort;
import com.cromp.iam.domain.repository.OrganizationRepositoryPort;
import com.cromp.iam.domain.repository.RoleRepositoryPort;
import com.cromp.iam.domain.repository.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipApplicationService implements MembershipFacade {

    private final MembershipRepositoryPort membershipRepository;
    private final UserRepositoryPort userRepository;
    private final OrganizationRepositoryPort organizationRepository;
    private final RoleRepositoryPort roleRepository;
    private final MembershipApiMapper membershipMapper;
    private final CurrentActorPort currentActorPort;
    private final PermissionCheckerPort permissionCheckerPort;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public MembershipResponse add(AddMembershipRequest request) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("No organization selected"));

        if (!permissionCheckerPort.hasPermission(currentUserId, organizationId, "org:invite")) {
            throw new SecurityException("No permission to invite users to this organization");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new DomainException("User not found"));
        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new DomainException("Role not found"));

        Membership membership = Membership.join(user.getId(), organizationId, role.getId());
        membership = membershipRepository.save(membership);

        domainEventPublisher.publish(new MemberAddedEvent(
                organizationId,
                user.getId(),
                user.getEmail(),
                role.getName().toString(),
                currentUserId
        ));

        return membershipMapper.toResponse(membership);
    }

    @Override
    public MembershipResponse changeRole(UUID membershipUuid, ChangeMembershipRoleRequest request) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        Membership membership = membershipRepository.findByMembershipUuid(membershipUuid)
                .orElseThrow(() -> new DomainException("Membership not found"));

        if (!permissionCheckerPort.hasPermission(currentUserId, membership.getOrganizationId(), "org:update")) {
            throw new SecurityException("No permission to change roles in this organization");
        }

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new DomainException("Role not found"));

        String oldRoleName = roleRepository.findById(membership.getRoleId())
                .map(r -> r.getName().toString()).orElse("UNKNOWN");

        membership.changeRole(role.getId());
        membershipRepository.save(membership);

        User member = userRepository.findById(membership.getUserId()).orElse(null);
        domainEventPublisher.publish(new MemberRoleChangedEvent(
                membership.getOrganizationId(),
                membership.getUserId(),
                member != null ? member.getEmail() : "unknown",
                oldRoleName,
                role.getName().toString(),
                currentUserId
        ));

        return membershipMapper.toResponse(membership);
    }
    
    @Override
    public void remove(UUID membershipUuid) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        Membership membership = membershipRepository.findByMembershipUuid(membershipUuid)
                .orElseThrow(() -> new DomainException("Membership not found"));

        if (!permissionCheckerPort.hasPermission(currentUserId, membership.getOrganizationId(), "org:update")) {
            throw new SecurityException("No permission to remove members from this organization");
        }

        User member = userRepository.findById(membership.getUserId()).orElse(null);

        domainEventPublisher.publish(new MemberRemovedEvent(
                membership.getOrganizationId(),
                membership.getUserId(),
                member != null ? member.getEmail() : "unknown",
                currentUserId
        ));

        membershipRepository.deleteById(membership.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipResponse getById(UUID membershipUuid) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        Membership membership = membershipRepository.findByMembershipUuid(membershipUuid)
                .orElseThrow(() -> new DomainException("Membership not found"));

        if (!permissionCheckerPort.isMember(currentUserId, membership.getOrganizationId())) {
            throw new SecurityException("You are not a member of this organization");
        }

        return membershipMapper.toResponse(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipResponse> getByOrganization(UUID orgUuid) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Organization organization = organizationRepository.findByOrgUuid(orgUuid)
                .orElseThrow(() -> new DomainException("Organization not found"));

        if (!permissionCheckerPort.isMember(currentUserId, organization.getId())) {
            throw new SecurityException("You are not a member of this organization");
        }

        return membershipRepository.findByOrganizationId(organization.getId()).stream()
                .map(m -> {
                    User member = userRepository.findById(m.getUserId()).orElse(null);
                    Role role = roleRepository.findById(m.getRoleId()).orElse(null);
                    return membershipMapper.toResponse(m,
                            role != null ? role.getName().name() : null,
                            organization, member, m.getCreatedAt());
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipResponse> getByUser(UUID userUuid) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        User targetUser = userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new DomainException("User not found"));

        if (!currentUserId.equals(targetUser.getId())) {
            throw new SecurityException("You can only view your own memberships");
        }

        return membershipRepository.findByUserId(targetUser.getId()).stream()
                .map(m -> {
                    Organization org = organizationRepository.findById(m.getOrganizationId()).orElse(null);
                    Role role = roleRepository.findById(m.getRoleId()).orElse(null);
                    return membershipMapper.toResponse(m,
                            role != null ? role.getName().name() : null,
                            org, targetUser, m.getCreatedAt());
                })
                .toList();
    }
}
