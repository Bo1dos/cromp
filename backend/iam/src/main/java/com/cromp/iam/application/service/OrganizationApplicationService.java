package com.cromp.iam.application.service;

import com.cromp.iam.api.dto.request.CreateOrganizationRequest;
import com.cromp.iam.api.dto.request.RenameOrganizationRequest;
import com.cromp.iam.api.dto.response.MembershipResponse;
import com.cromp.iam.api.dto.response.OrganizationResponse;
import com.cromp.iam.api.mapper.MembershipApiMapper;
import com.cromp.iam.api.mapper.OrganizationApiMapper;
import com.cromp.iam.api.service.OrganizationFacade;
import com.cromp.iam.application.port.CurrentActorPort;
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

    @Override
    public OrganizationResponse create(CreateOrganizationRequest request, Long creatorUserId) {
        Organization org = Organization.create(request.name(), request.settings());
        org = organizationRepository.save(org);

        // Создатель становится OWNER
        Role ownerRole = roleRepository.findByName(UserRole.OWNER)
                .orElseThrow(() -> new IllegalStateException("OWNER role not found"));
        Membership membership = Membership.join(creatorUserId, org.getId(), ownerRole.getId());
        membershipRepository.save(membership);

        return organizationMapper.toResponse(org);
    }

    @Override
    public OrganizationResponse rename(RenameOrganizationRequest request) {
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("No organization selected"));
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new DomainException("Organization not found"));
        org.rename(request.name());
        return organizationMapper.toResponse(organizationRepository.save(org));
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getById(Long organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new DomainException("Organization not found"));
        return organizationMapper.toResponse(org);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipResponse> getMembers(Long organizationId) {
        List<Membership> memberships = membershipRepository.findByOrganizationId(organizationId);
        return memberships.stream()
                .map(membershipMapper::toResponse) // TODO: упрощённо, без имени роли (можно позже добавить)
                .toList();
    }
}