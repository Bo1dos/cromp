package com.cromp.iam.application;

import com.cromp.iam.api.dto.request.AcceptInvitationRequest;
import com.cromp.iam.api.dto.request.InviteUserRequest;
import com.cromp.iam.api.dto.request.RejectInvitationRequest;
import com.cromp.iam.api.dto.request.RevokeInvitationRequest;
import com.cromp.iam.api.mapper.InvitationApiMapper;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.iam.application.service.InvitationApplicationService;
import com.cromp.iam.domain.model.Invitation;
import com.cromp.iam.domain.model.Organization;
import com.cromp.iam.domain.model.Role;
import com.cromp.iam.domain.model.User;
import com.cromp.iam.domain.model.enums.InvitationStatus;
import com.cromp.iam.domain.model.enums.UserRole;
import com.cromp.iam.domain.model.exceptions.DomainException;
import com.cromp.iam.domain.repository.InvitationRepositoryPort;
import com.cromp.iam.domain.repository.MembershipRepositoryPort;
import com.cromp.iam.domain.repository.OrganizationRepositoryPort;
import com.cromp.iam.domain.repository.RoleRepositoryPort;
import com.cromp.iam.domain.repository.UserRepositoryPort;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
class InvitationApplicationServiceTest {

    @Mock
    private InvitationRepositoryPort invitationRepository;
    @Mock
    private OrganizationRepositoryPort organizationRepository;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private RoleRepositoryPort roleRepository;
    @Mock
    private MembershipRepositoryPort membershipRepository;
    @Mock
    private CurrentActorPort currentActorPort;
    @Mock
    private PermissionCheckerPort permissionCheckerPort;

    private InvitationApplicationService service;

    @BeforeEach
    void setUp() {
        service = new InvitationApplicationService(
                invitationRepository,
                organizationRepository,
                userRepository,
                roleRepository,
                membershipRepository,
                new InvitationApiMapper(),
                currentActorPort,
                permissionCheckerPort
        );
    }

    @Test
    void shouldInviteUserWithDefaultExpirationAndHashedToken() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.of(20L));
        when(permissionCheckerPort.hasPermission(10L, 20L, "org:invite")).thenReturn(true);
        when(organizationRepository.findById(20L)).thenReturn(Optional.of(organization(20L)));
        when(roleRepository.findByName(UserRole.ADMIN)).thenReturn(Optional.of(Role.reconstitute(30L, UserRole.ADMIN, "Admin")));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();
        var response = service.invite(new InviteUserRequest("INVITED@example.com", UserRole.ADMIN, null));
        Instant after = Instant.now();

        assertThat(response.invitationToken()).isNotBlank();
        assertThat(response.invitation().email()).isEqualTo("invited@example.com");
        assertThat(response.invitation().roleName()).isEqualTo("ADMIN");
        assertThat(response.invitation().status()).isEqualTo("PENDING");

        ArgumentCaptor<Invitation> invitationCaptor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(invitationCaptor.capture());
        Invitation saved = invitationCaptor.getValue();
        assertThat(saved.getTokenHash()).isEqualTo(DigestUtils.sha256Hex(response.invitationToken()));
        assertThat(saved.getTokenHash()).isNotEqualTo(response.invitationToken());
        assertThat(saved.getExpiresAt()).isBetween(before.plus(6, ChronoUnit.DAYS), after.plus(8, ChronoUnit.DAYS));
    }

    @Test
    void shouldThrowWhenInvitingWithoutPermission() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.of(20L));
        when(permissionCheckerPort.hasPermission(10L, 20L, "org:invite")).thenReturn(false);

        assertThatThrownBy(() -> service.invite(new InviteUserRequest("user@example.com", UserRole.ADMIN, null)))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void shouldAcceptInvitationAndCreateMembershipForMatchingEmail() {
        String rawToken = "raw-token";
        Invitation invitation = Invitation.create(20L, "user@example.com", DigestUtils.sha256Hex(rawToken), 30L, 99L, Instant.now().plusSeconds(3600));
        invitation = Invitation.reconstitute(
                1L,
                invitation.getInvitationUuid(),
                invitation.getOrganizationId(),
                invitation.getEmail(),
                invitation.getTokenHash(),
                invitation.getRoleId(),
                invitation.getInvitedBy(),
                invitation.getExpiresAt(),
                invitation.getCreatedAt(),
                invitation.getAcceptedAt(),
                invitation.getStatus()
        );
        User user = user(10L, "user@example.com");

        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(invitationRepository.findByTokenHash(DigestUtils.sha256Hex(rawToken))).thenReturn(Optional.of(invitation));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.findById(30L)).thenReturn(Optional.of(Role.reconstitute(30L, UserRole.ADMIN, "Admin")));

        var response = service.accept(new AcceptInvitationRequest(rawToken));

        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.acceptedAt()).isNotNull();
        verify(membershipRepository).save(any());
    }

    @Test
    void shouldExpireInvitationAndThrowWhenAcceptingExpiredToken() {
        String rawToken = "expired-token";
        Invitation invitation = Invitation.reconstitute(
                1L,
                UUID.randomUUID(),
                20L,
                "user@example.com",
                DigestUtils.sha256Hex(rawToken),
                30L,
                99L,
                Instant.now().minusSeconds(60),
                Instant.now().minusSeconds(3600),
                null,
                InvitationStatus.PENDING
        );

        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(invitationRepository.findByTokenHash(DigestUtils.sha256Hex(rawToken))).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.accept(new AcceptInvitationRequest(rawToken)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("expired");

        verify(invitationRepository).save(invitation);
    }

    @Test
    void shouldDeleteInvitationWhenRejectingMatchingInvitation() {
        String rawToken = "reject-token";
        Invitation invitation = Invitation.reconstitute(
                5L,
                UUID.randomUUID(),
                20L,
                "user@example.com",
                DigestUtils.sha256Hex(rawToken),
                30L,
                99L,
                Instant.now().plusSeconds(3600),
                Instant.now(),
                null,
                InvitationStatus.PENDING
        );

        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L, "user@example.com")));
        when(invitationRepository.findByTokenHash(DigestUtils.sha256Hex(rawToken))).thenReturn(Optional.of(invitation));

        service.reject(new RejectInvitationRequest(rawToken));

        verify(invitationRepository).deleteById(5L);
    }

    @Test
    void shouldRevokeInvitationWhenPermissionExists() {
        UUID invitationUuid = UUID.randomUUID();
        Invitation invitation = Invitation.reconstitute(
                5L,
                invitationUuid,
                20L,
                "user@example.com",
                "hash",
                30L,
                99L,
                Instant.now().plusSeconds(3600),
                Instant.now(),
                null,
                InvitationStatus.PENDING
        );

        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(invitationRepository.findByInvitationUuid(invitationUuid)).thenReturn(Optional.of(invitation));
        when(permissionCheckerPort.hasPermission(10L, 20L, "org:invite")).thenReturn(true);
        when(invitationRepository.save(invitation)).thenReturn(invitation);
        when(roleRepository.findById(30L)).thenReturn(Optional.of(Role.reconstitute(30L, UserRole.ADMIN, "Admin")));

        var response = service.revoke(new RevokeInvitationRequest(invitationUuid));

        assertThat(response.status()).isEqualTo("REVOKED");
    }

    @Test
    void shouldReturnInvitationsByOrganizationForMembersOnly() {
        UUID orgUuid = UUID.randomUUID();
        Invitation invitation = Invitation.reconstitute(
                5L,
                UUID.randomUUID(),
                20L,
                "user@example.com",
                "hash",
                30L,
                99L,
                Instant.now().plusSeconds(3600),
                Instant.now(),
                null,
                InvitationStatus.PENDING
        );

        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(organizationRepository.findByOrgUuid(orgUuid)).thenReturn(Optional.of(organization(20L)));
        when(permissionCheckerPort.isMember(10L, 20L)).thenReturn(true);
        when(invitationRepository.findByOrganizationId(20L)).thenReturn(List.of(invitation));
        when(roleRepository.findById(30L)).thenReturn(Optional.of(Role.reconstitute(30L, UserRole.ADMIN, "Admin")));

        var response = service.getByOrganization(orgUuid);

        assertThat(response).singleElement().satisfies(item -> {
            assertThat(item.email()).isEqualTo("user@example.com");
            assertThat(item.roleName()).isEqualTo("ADMIN");
        });
    }

    private static User user(Long id, String email) {
        return User.reconstitute(
                id,
                UUID.randomUUID(),
                email,
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
}
