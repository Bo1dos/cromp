package com.cromp.iam.application.service;

import com.cromp.iam.api.dto.request.LoginRequest;
import com.cromp.iam.api.dto.request.RegisterRequest;
import com.cromp.iam.api.dto.request.SelectOrganizationRequest;
import com.cromp.iam.api.dto.response.AuthResponse;
import com.cromp.iam.api.dto.response.OrganizationResponse;
import com.cromp.iam.api.dto.response.UserResponse;
import com.cromp.iam.api.mapper.AuthApiMapper;
import com.cromp.iam.api.mapper.OrganizationApiMapper;
import com.cromp.iam.api.mapper.UserApiMapper;
import com.cromp.iam.api.service.AuthFacade;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PasswordHasherPort;
import com.cromp.iam.application.port.TokenIssuerPort;
import com.cromp.iam.domain.model.*;
import com.cromp.iam.domain.model.enums.UserRole;
import com.cromp.iam.domain.model.exceptions.DomainException;
import com.cromp.iam.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthApplicationService implements AuthFacade {

    private final UserRepositoryPort userRepository;
    private final OrganizationRepositoryPort organizationRepository;
    private final RoleRepositoryPort roleRepository;
    private final MembershipRepositoryPort membershipRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenIssuerPort tokenIssuer;
    private final UserApiMapper userMapper;
    private final OrganizationApiMapper organizationMapper;
    private final AuthApiMapper authApiMapper;
    private final RolePermissionRepositoryPort rolePermissionRepository;
    private final CurrentActorPort currentActorPort;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DomainException("User with email " + request.email() + " already exists");
        }

        String passwordHash = passwordHasher.hash(request.password());
        User user = User.register(
                request.email(),
                passwordHash,
                request.firstName(),
                request.lastName(),
                request.middleName(),
                request.displayName(),
                java.util.Map.of()
        );
        user = userRepository.save(user);

        String orgName = request.organizationName() != null && !request.organizationName().isBlank()
                ? request.organizationName()
                : request.email().split("@")[0] + "'s Organization";
        Organization organization = Organization.create(orgName);
        organization = organizationRepository.save(organization);

        Role ownerRole = roleRepository.findByName(UserRole.OWNER)
                .orElseThrow(() -> new IllegalStateException("OWNER role not found in database"));

        Membership membership = Membership.join(user.getId(), organization.getId(), ownerRole.getId());
        membershipRepository.save(membership);

        // После регистрации сразу даём токен с этой организацией
        List<String> permissions = rolePermissionRepository.findByRoleId(ownerRole.getId()).stream()
                .map(RolePermission::getPermission)
                .toList();
        String token = tokenIssuer.issueToken(user, organization, permissions);

        return authApiMapper.toSelectResponse(token, user, organization);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new DomainException("Invalid email or password"));

        if (user.getPasswordHash() == null || !passwordHasher.matches(request.password(), user.getPasswordHash())) {
            throw new DomainException("Invalid email or password");
        }

        List<Membership> memberships = membershipRepository.findByUserId(user.getId());
        if (memberships.isEmpty()) {
            throw new DomainException("User has no organization membership");
        }

        // Собрать список организаций пользователя
        List<Organization> orgs = memberships.stream()
                .map(m -> organizationRepository.findById(m.getOrganizationId()).orElseThrow())
                .toList();

        // Выпустить токен без организации
        String token = tokenIssuer.issueToken(user, null, List.of()); // пустой список прав

        return authApiMapper.toLoginResponse(token, user, orgs);
    }


        @Override
        @Transactional(readOnly = true)
        public AuthResponse selectOrganization(SelectOrganizationRequest request) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        Membership membership = membershipRepository.findByUserIdAndOrganizationId(userId, request.organizationId())
                .orElseThrow(() -> new DomainException("User is not a member of this organization"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("User not found"));
        Organization org = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> new DomainException("Organization not found"));

        List<String> permissions = rolePermissionRepository.findByRoleId(membership.getRoleId()).stream()
                .map(RolePermission::getPermission)
                .toList();

        String token = tokenIssuer.issueToken(user, org, permissions);

        return authApiMapper.toSelectResponse(token, user, org);
        }
}