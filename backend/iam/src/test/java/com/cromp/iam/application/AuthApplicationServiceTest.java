package com.cromp.iam.application;

import com.cromp.iam.api.dto.request.LoginRequest;
import com.cromp.iam.api.dto.request.RegisterRequest;
import com.cromp.iam.api.dto.request.SelectOrganizationRequest;
import com.cromp.iam.api.mapper.AuthApiMapper;
import com.cromp.iam.api.mapper.OrganizationApiMapper;
import com.cromp.iam.api.mapper.UserApiMapper;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PasswordHasherPort;
import com.cromp.iam.application.port.TokenIssuerPort;
import com.cromp.iam.application.service.AuthApplicationService;
import com.cromp.iam.domain.model.Membership;
import com.cromp.iam.domain.model.Organization;
import com.cromp.iam.domain.model.Role;
import com.cromp.iam.domain.model.RolePermission;
import com.cromp.iam.domain.model.User;
import com.cromp.iam.domain.model.enums.UserRole;
import com.cromp.iam.domain.model.exceptions.DomainException;
import com.cromp.iam.domain.repository.MembershipRepositoryPort;
import com.cromp.iam.domain.repository.OrganizationRepositoryPort;
import com.cromp.iam.domain.repository.RolePermissionRepositoryPort;
import com.cromp.iam.domain.repository.RoleRepositoryPort;
import com.cromp.iam.domain.repository.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class AuthApplicationServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private OrganizationRepositoryPort organizationRepository;
    @Mock
    private RoleRepositoryPort roleRepository;
    @Mock
    private MembershipRepositoryPort membershipRepository;
    @Mock
    private PasswordHasherPort passwordHasher;
    @Mock
    private TokenIssuerPort tokenIssuer;
    @Mock
    private RolePermissionRepositoryPort rolePermissionRepository;
    @Mock
    private CurrentActorPort currentActorPort;

    private AuthApplicationService service;

    @BeforeEach
    void setUp() {
        service = new AuthApplicationService(
                userRepository,
                organizationRepository,
                roleRepository,
                membershipRepository,
                passwordHasher,
                tokenIssuer,
                new UserApiMapper(),
                new OrganizationApiMapper(),
                new AuthApiMapper(new UserApiMapper(), new OrganizationApiMapper()),
                rolePermissionRepository,
                currentActorPort
        );
    }

    @Test
    void shouldRegisterWhenRequestIsValid() {
        RegisterRequest request = new RegisterRequest(
                "USER@example.com",
                "password123",
                "Acme",
                "John",
                "Doe",
                null,
                "John Doe"
        );
        User savedUser = user(11L, "user@example.com");
        Organization savedOrganization = organization(22L, "Acme");
        Role ownerRole = Role.reconstitute(33L, UserRole.OWNER, "Owner");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordHasher.hash("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(organizationRepository.save(any(Organization.class))).thenReturn(savedOrganization);
        when(roleRepository.findByName(UserRole.OWNER)).thenReturn(Optional.of(ownerRole));
        when(rolePermissionRepository.findByRoleId(33L))
                .thenReturn(List.of(RolePermission.of(33L, "org:update"), RolePermission.of(33L, "org:invite")));
        when(tokenIssuer.issueToken(savedUser, savedOrganization, List.of("org:update", "org:invite")))
                .thenReturn("jwt-token");

        var response = service.register(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.activeOrganization()).isNotNull();
        assertThat(response.user().email()).isEqualTo("user@example.com");

        ArgumentCaptor<Membership> membershipCaptor = ArgumentCaptor.forClass(Membership.class);
        verify(membershipRepository).save(membershipCaptor.capture());
        assertThat(membershipCaptor.getValue().getUserId()).isEqualTo(11L);
        assertThat(membershipCaptor.getValue().getOrganizationId()).isEqualTo(22L);
        assertThat(membershipCaptor.getValue().getRoleId()).isEqualTo(33L);
    }

    @Test
    void shouldThrowWhenRegisteringDuplicateEmail() {
        when(userRepository.existsByEmail("used@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterRequest(
                "used@example.com",
                "password123",
                "Acme",
                null,
                null,
                null,
                null
        ))).isInstanceOf(DomainException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldLoginWhenCredentialsAreValid() {
        User user = user(10L, "user@example.com");
        Membership membership = membership(101L, 10L, 20L, 30L);
        Organization organization = organization(20L, "Acme");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("password123", user.getPasswordHash())).thenReturn(true);
        when(membershipRepository.findByUserId(10L)).thenReturn(List.of(membership));
        when(organizationRepository.findById(20L)).thenReturn(Optional.of(organization));
        when(tokenIssuer.issueToken(user, null, List.of())).thenReturn("login-token");

        var response = service.login(new LoginRequest("USER@example.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("login-token");
        assertThat(response.organizations()).singleElement().satisfies(org -> assertThat(org.name()).isEqualTo("Acme"));
        assertThat(response.activeOrganization()).isNull();
    }

    @Test
    void shouldThrowWhenLoginPasswordIsInvalid() {
        User user = user(10L, "user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("user@example.com", "wrong")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void shouldThrowWhenLoginUserHasNoMemberships() {
        User user = user(10L, "user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("password123", user.getPasswordHash())).thenReturn(true);
        when(membershipRepository.findByUserId(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.login(new LoginRequest("user@example.com", "password123")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("no organization membership");
    }

    @Test
    void shouldSelectOrganizationWhenMembershipExists() {
        User user = user(10L, "user@example.com");
        Membership membership = membership(101L, 10L, 20L, 30L);
        Organization organization = organization(20L, "Acme");

        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(membershipRepository.findByUserIdAndOrganizationId(10L, 20L)).thenReturn(Optional.of(membership));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(organizationRepository.findById(20L)).thenReturn(Optional.of(organization));
        when(rolePermissionRepository.findByRoleId(30L)).thenReturn(List.of(RolePermission.of(30L, "org:update")));
        when(tokenIssuer.issueToken(user, organization, List.of("org:update"))).thenReturn("selected-token");

        var response = service.selectOrganization(new SelectOrganizationRequest(20L));

        assertThat(response.accessToken()).isEqualTo("selected-token");
        assertThat(response.activeOrganization().name()).isEqualTo("Acme");
    }

    @Test
    void shouldThrowWhenSelectingOrganizationWithoutAuthentication() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.selectOrganization(new SelectOrganizationRequest(20L)))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not authenticated");
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
                "stored-hash",
                Map.of(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
    }

    private static Organization organization(Long id, String name) {
        return Organization.reconstitute(
                id,
                UUID.randomUUID(),
                name,
                Map.of(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
    }

    private static Membership membership(Long id, Long userId, Long orgId, Long roleId) {
        return Membership.reconstitute(
                id,
                UUID.randomUUID(),
                userId,
                orgId,
                roleId,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
    }
}
