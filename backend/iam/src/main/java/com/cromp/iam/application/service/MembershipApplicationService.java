package com.cromp.iam.application.service;

import com.cromp.iam.api.dto.request.AddMembershipRequest;
import com.cromp.iam.api.dto.request.ChangeMembershipRoleRequest;
import com.cromp.iam.api.dto.request.RemoveMembershipRequest;
import com.cromp.iam.api.dto.response.MembershipResponse;
import com.cromp.iam.api.mapper.MembershipApiMapper;
import com.cromp.iam.api.service.MembershipFacade;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.iam.domain.model.Membership;
import com.cromp.iam.domain.model.Role;
import com.cromp.iam.domain.model.User;
import com.cromp.iam.domain.model.exceptions.DomainException;
import com.cromp.iam.domain.repository.MembershipRepositoryPort;
import com.cromp.iam.domain.repository.RoleRepositoryPort;
import com.cromp.iam.domain.repository.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipApplicationService implements MembershipFacade {

    private final MembershipRepositoryPort membershipRepository;
    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;
    private final MembershipApiMapper membershipMapper;
    private final CurrentActorPort currentActorPort;
    private final PermissionCheckerPort permissionCheckerPort;

    @Override
    public MembershipResponse add(AddMembershipRequest request) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        // Проверяем право на приглашение в организацию
        if (!permissionCheckerPort.hasPermission(currentUserId, request.organizationId(), "org:invite")) {
            throw new SecurityException("No permission to invite users to this organization");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new DomainException("User not found"));
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new DomainException("Role not found"));

        Membership membership = Membership.join(user.getId(), request.organizationId(), role.getId());
        membership = membershipRepository.save(membership);
        return membershipMapper.toResponse(membership);
    }

    @Override
    public MembershipResponse changeRole(ChangeMembershipRoleRequest request) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        Membership membership = membershipRepository.findById(request.membershipId())
                .orElseThrow(() -> new DomainException("Membership not found"));

        // Проверяем право на управление ролями в организации
        if (!permissionCheckerPort.hasPermission(currentUserId, membership.getOrganizationId(), "org:update")) {
            throw new SecurityException("No permission to change roles in this organization");
        }

        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new DomainException("Role not found"));

        membership.changeRole(role.getId());
        membershipRepository.save(membership);
        return membershipMapper.toResponse(membership);
    }

    @Override
    public void remove(RemoveMembershipRequest request) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        Membership membership = membershipRepository.findById(request.membershipId())
                .orElseThrow(() -> new DomainException("Membership not found"));

        if (!permissionCheckerPort.hasPermission(currentUserId, membership.getOrganizationId(), "org:update")) {
            throw new SecurityException("No permission to remove members from this organization");
        }

        membershipRepository.deleteById(membership.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipResponse getById(Long membershipId) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new DomainException("Membership not found"));

        // Только член организации может видеть членство
        if (!permissionCheckerPort.isMember(currentUserId, membership.getOrganizationId())) {
            throw new SecurityException("You are not a member of this organization");
        }

        return membershipMapper.toResponse(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipResponse> getByOrganization(Long organizationId) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        if (!permissionCheckerPort.isMember(currentUserId, organizationId)) {
            throw new SecurityException("You are not a member of this organization");
        }

        return membershipRepository.findByOrganizationId(organizationId).stream()
                .map(membershipMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipResponse> getByUser(Long userId) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        // Пользователь может видеть только свои членства 
        // TODO: (админ-логику добавим позже)
        if (!currentUserId.equals(userId)) {
            throw new SecurityException("You can only view your own memberships");
        }

        return membershipRepository.findByUserId(userId).stream()
                .map(membershipMapper::toResponse)
                .toList();
    }
}