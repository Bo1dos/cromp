package com.cromp.iam.application;

import com.cromp.iam.api.dto.request.ChangeUserEmailRequest;
import com.cromp.iam.api.dto.request.ChangeUserPasswordRequest;
import com.cromp.iam.api.dto.request.CreateOrganizationRequest;
import com.cromp.iam.api.dto.request.RenameOrganizationRequest;
import com.cromp.iam.api.dto.request.UpdateUserProfileRequest;
import com.cromp.iam.api.mapper.MembershipApiMapper;
import com.cromp.iam.api.mapper.OrganizationApiMapper;
import com.cromp.iam.api.mapper.UserApiMapper;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PasswordHasherPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.iam.application.service.OrganizationApplicationService;
import com.cromp.iam.application.service.UserApplicationService;
import com.cromp.iam.domain.model.Membership;
import com.cromp.iam.domain.model.Organization;
import com.cromp.iam.domain.model.Role;
import com.cromp.iam.domain.model.User;
import com.cromp.iam.domain.model.enums.UserRole;
import com.cromp.iam.domain.model.exceptions.DomainException;
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
class OrganizationAndUserApplicationServiceTest {

    @Mock
    private OrganizationRepositoryPort organizationRepository;
    @Mock
    private MembershipRepositoryPort membershipRepository;
    @Mock
    private RoleRepositoryPort roleRepository;
    @Mock
    private CurrentActorPort currentActorPort;
    @Mock
    private PermissionCheckerPort permissionCheckerPort;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private PasswordHasherPort passwordHasher;

    private OrganizationApplicationService organizationService;
    private UserApplicationService userService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationApplicationService(
                organizationRepository,
                membershipRepository,
                roleRepository,
                new OrganizationApiMapper(),
                new MembershipApiMapper(new com.cromp.iam.api.mapper.OrganizationApiMapper()),
                currentActorPort,
                permissionCheckerPort
        );
        userService = new UserApplicationService(
                userRepository,
                new UserApiMapper(),
                passwordHasher,
                currentActorPort
        );
    }

    @Test
    void shouldCreateOrganizationAndOwnerMembership() {
        Organization organization = organization(20L, UUID.randomUUID(), "Acme");
        when(organizationRepository.save(any(Organization.class))).thenReturn(organization);
        when(roleRepository.findByName(UserRole.OWNER)).thenReturn(Optional.of(Role.reconstitute(30L, UserRole.OWNER, "Owner")));

        var response = organizationService.create(new CreateOrganizationRequest("Acme", Map.of("plan", "pro")), 10L);

        assertThat(response.id()).isEqualTo(20L);
        verify(membershipRepository).save(any(Membership.class));
    }

    @Test
    void shouldRenameOrganizationWhenContextAndPermissionMatch() {
        UUID orgUuid = UUID.randomUUID();
        Organization organization = organization(20L, orgUuid, "Acme");

        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.of(20L));
        when(organizationRepository.findByOrgUuid(orgUuid)).thenReturn(Optional.of(organization));
        when(permissionCheckerPort.hasPermission(10L, 20L, "org:update")).thenReturn(true);
        when(organizationRepository.save(organization)).thenReturn(organization);

        var response = organizationService.rename(orgUuid, new RenameOrganizationRequest("Renamed"));

        assertThat(response.name()).isEqualTo("Renamed");
    }

    @Test
    void shouldThrowWhenRenamingOutsideCurrentOrganizationContext() {
        UUID orgUuid = UUID.randomUUID();
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.of(99L));
        when(organizationRepository.findByOrgUuid(orgUuid)).thenReturn(Optional.of(organization(20L, orgUuid, "Acme")));

        assertThatThrownBy(() -> organizationService.rename(orgUuid, new RenameOrganizationRequest("Renamed")))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("does not match current context");
    }

    @Test
    void shouldReturnMembersWhenCurrentUserBelongsToOrganization() {
        UUID orgUuid = UUID.randomUUID();
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(organizationRepository.findByOrgUuid(orgUuid)).thenReturn(Optional.of(organization(20L, orgUuid, "Acme")));
        when(permissionCheckerPort.isMember(10L, 20L)).thenReturn(true);
        when(membershipRepository.findByOrganizationId(20L))
                .thenReturn(List.of(Membership.join(10L, 20L, 30L)));

        assertThat(organizationService.getMembers(orgUuid)).hasSize(1);
    }

    @Test
    void shouldReturnUserByIdForSelfOnly() {
        UUID userUuid = UUID.randomUUID();
        User user = user(10L, userUuid, "user@example.com");

        when(userRepository.findByUserUuid(userUuid)).thenReturn(Optional.of(user));
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));

        var response = userService.getById(userUuid);

        assertThat(response.email()).isEqualTo("user@example.com");
    }

    @Test
    void shouldUpdateProfileAndSaveUserWhenSelfAccess() {
        UUID userUuid = UUID.randomUUID();
        User user = user(10L, userUuid, "user@example.com");

        when(userRepository.findByUserUuid(userUuid)).thenReturn(Optional.of(user));
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(userRepository.save(user)).thenReturn(user);

        var response = userService.updateProfile(
                userUuid,
                new UpdateUserProfileRequest("Jane", "Smith", null, "Jane Smith", Map.of("locale", "en"))
        );

        assertThat(response.firstName()).isEqualTo("Jane");
        assertThat(response.lastName()).isEqualTo("Smith");
        assertThat(response.profile()).containsEntry("locale", "en");
    }

    @Test
    void shouldThrowWhenChangingEmailToDuplicateOne() {
        UUID userUuid = UUID.randomUUID();
        User user = user(10L, userUuid, "user@example.com");

        when(userRepository.findByUserUuid(userUuid)).thenReturn(Optional.of(user));
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(userRepository.existsByEmail("used@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.changeEmail(userUuid, new ChangeUserEmailRequest("used@example.com")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("already in use");
    }

    @Test
    void shouldHashAndSaveNewPasswordWhenOldPasswordMatches() {
        UUID userUuid = UUID.randomUUID();
        User user = user(10L, userUuid, "user@example.com");

        when(userRepository.findByUserUuid(userUuid)).thenReturn(Optional.of(user));
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(passwordHasher.matches("old-password", user.getPasswordHash())).thenReturn(true);
        when(passwordHasher.hash("new-password")).thenReturn("new-hash");

        userService.changePassword(userUuid, new ChangeUserPasswordRequest("old-password", "new-password"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowWhenChangingPasswordWithWrongOldPassword() {
        UUID userUuid = UUID.randomUUID();
        User user = user(10L, userUuid, "user@example.com");

        when(userRepository.findByUserUuid(userUuid)).thenReturn(Optional.of(user));
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(passwordHasher.matches("wrong", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(userUuid, new ChangeUserPasswordRequest("wrong", "new-password")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("incorrect");
    }

    private static Organization organization(Long id, UUID uuid, String name) {
        return Organization.reconstitute(
                id,
                uuid,
                name,
                Map.of(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
    }

    private static User user(Long id, UUID uuid, String email) {
        return User.reconstitute(
                id,
                uuid,
                email,
                "Doe",
                "John",
                null,
                "John Doe",
                "stored-hash",
                Map.of(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
    }
}
