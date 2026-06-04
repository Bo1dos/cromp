package com.cromp.iam.application;

import com.cromp.iam.api.dto.request.AddMembershipRequest;
import com.cromp.iam.api.dto.request.ChangeMembershipRoleRequest;
import com.cromp.iam.api.mapper.MembershipApiMapper;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.iam.application.service.MembershipApplicationService;
import com.cromp.iam.domain.model.Membership;
import com.cromp.iam.domain.model.Organization;
import com.cromp.iam.domain.model.Role;
import com.cromp.iam.domain.model.User;
import com.cromp.iam.domain.model.enums.UserRole;
import com.cromp.iam.domain.repository.MembershipRepositoryPort;
import com.cromp.iam.domain.repository.OrganizationRepositoryPort;
import com.cromp.iam.domain.repository.RoleRepositoryPort;
import com.cromp.iam.domain.repository.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipApplicationServiceTest {

    @Mock
    private MembershipRepositoryPort membershipRepository;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private OrganizationRepositoryPort organizationRepository;
    @Mock
    private RoleRepositoryPort roleRepository;
    @Mock
    private CurrentActorPort currentActorPort;
    @Mock
    private PermissionCheckerPort permissionCheckerPort;

    private MembershipApplicationService service;

    @BeforeEach
    void setUp() {
        service = new MembershipApplicationService(
                membershipRepository,
                userRepository,
                organizationRepository,
                roleRepository,
                new MembershipApiMapper(new com.cromp.iam.api.mapper.OrganizationApiMapper()),
                currentActorPort,
                permissionCheckerPort
        );
    }

    @Test
    void shouldAddMembershipWhenPermissionGranted() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(1L));
        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.of(20L));
        when(permissionCheckerPort.hasPermission(1L, 20L, "org:invite")).thenReturn(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L)));
        when(roleRepository.findByName(UserRole.ADMIN)).thenReturn(Optional.of(Role.reconstitute(30L, UserRole.ADMIN, "Admin")));
        when(membershipRepository.save(any(Membership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.add(new AddMembershipRequest(10L, UserRole.ADMIN));

        assertThat(response.roleName()).isNull();
        assertThat(response.membershipUuid()).isNotNull();
    }

    @Test
    void shouldThrowWhenAddingMembershipWithoutPermission() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(1L));
        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.of(20L));
        when(permissionCheckerPort.hasPermission(1L, 20L, "org:invite")).thenReturn(false);

        assertThatThrownBy(() -> service.add(new AddMembershipRequest(10L, UserRole.ADMIN)))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void shouldChangeMembershipRoleWhenAuthorized() {
        UUID membershipUuid = UUID.randomUUID();
        Membership membership = membership(100L, membershipUuid, 10L, 20L, 30L);

        when(currentActorPort.currentUserId()).thenReturn(Optional.of(1L));
        when(membershipRepository.findByMembershipUuid(membershipUuid)).thenReturn(Optional.of(membership));
        when(permissionCheckerPort.hasPermission(1L, 20L, "org:update")).thenReturn(true);
        when(roleRepository.findByName(UserRole.MEMBER)).thenReturn(Optional.of(Role.reconstitute(40L, UserRole.MEMBER, "Member")));
        when(membershipRepository.save(membership)).thenReturn(membership);

        var response = service.changeRole(membershipUuid, new ChangeMembershipRoleRequest(UserRole.MEMBER));

        assertThat(response.updatedAt()).isAfter(Instant.parse("2024-01-01T00:00:00Z"));
    }

    @Test
    void shouldRemoveMembershipWhenAuthorized() {
        UUID membershipUuid = UUID.randomUUID();
        Membership membership = membership(100L, membershipUuid, 10L, 20L, 30L);

        when(currentActorPort.currentUserId()).thenReturn(Optional.of(1L));
        when(membershipRepository.findByMembershipUuid(membershipUuid)).thenReturn(Optional.of(membership));
        when(permissionCheckerPort.hasPermission(1L, 20L, "org:update")).thenReturn(true);

        service.remove(membershipUuid);

        verify(membershipRepository).deleteById(100L);
    }

    @Test
    void shouldReturnMembershipsByOrganizationForMembersOnly() {
        UUID orgUuid = UUID.randomUUID();
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(1L));
        when(organizationRepository.findByOrgUuid(orgUuid)).thenReturn(Optional.of(organization(20L)));
        when(permissionCheckerPort.isMember(1L, 20L)).thenReturn(true);
        when(membershipRepository.findByOrganizationId(20L)).thenReturn(List.of(membership(100L, UUID.randomUUID(), 10L, 20L, 30L)));

        var response = service.getByOrganization(orgUuid);

        assertThat(response).hasSize(1);
    }

    @Test
    void shouldThrowWhenGettingMembershipsOfAnotherUser() {
        UUID userUuid = UUID.randomUUID();
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(1L));
        when(userRepository.findByUserUuid(userUuid)).thenReturn(Optional.of(user(10L)));

        assertThatThrownBy(() -> service.getByUser(userUuid))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("own memberships");
    }

    private static User user(Long id) {
        return User.reconstitute(
                id,
                UUID.randomUUID(),
                "user@example.com",
                "Doe",
                "John",
                null,
                "John Doe",
                "hash",
                Map.of(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
    }

    private static Organization organization(Long id) {
        return Organization.reconstitute(
                id,
                UUID.randomUUID(),
                "Acme",
                Map.of(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
    }

    private static Membership membership(Long id, UUID uuid, Long userId, Long orgId, Long roleId) {
        return Membership.reconstitute(
                id,
                uuid,
                userId,
                orgId,
                roleId,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
    }
}
