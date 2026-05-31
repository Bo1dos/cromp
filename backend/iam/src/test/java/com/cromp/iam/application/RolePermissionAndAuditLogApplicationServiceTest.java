package com.cromp.iam.application;

import com.cromp.iam.api.dto.request.AddRolePermissionRequest;
import com.cromp.iam.api.dto.request.RecordAuditLogRequest;
import com.cromp.iam.api.mapper.AuditLogApiMapper;
import com.cromp.iam.api.mapper.RolePermissionApiMapper;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.iam.application.service.AuditLogApplicationService;
import com.cromp.iam.application.service.RolePermissionApplicationService;
import com.cromp.iam.domain.model.AuditLogEntry;
import com.cromp.iam.domain.model.Organization;
import com.cromp.iam.domain.model.Role;
import com.cromp.iam.domain.model.RolePermission;
import com.cromp.iam.domain.model.User;
import com.cromp.iam.domain.model.enums.UserRole;
import com.cromp.iam.domain.model.exceptions.DomainException;
import com.cromp.iam.domain.repository.AuditLogRepositoryPort;
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
class RolePermissionAndAuditLogApplicationServiceTest {

    @Mock
    private RolePermissionRepositoryPort rolePermissionRepository;
    @Mock
    private RoleRepositoryPort roleRepository;
    @Mock
    private AuditLogRepositoryPort auditLogRepository;
    @Mock
    private OrganizationRepositoryPort organizationRepository;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private CurrentActorPort currentActorPort;
    @Mock
    private PermissionCheckerPort permissionCheckerPort;

    private RolePermissionApplicationService rolePermissionService;
    private AuditLogApplicationService auditLogService;

    @BeforeEach
    void setUp() {
        rolePermissionService = new RolePermissionApplicationService(
                rolePermissionRepository,
                roleRepository,
                new RolePermissionApiMapper()
        );
        auditLogService = new AuditLogApplicationService(
                auditLogRepository,
                organizationRepository,
                userRepository,
                new AuditLogApiMapper(),
                currentActorPort,
                permissionCheckerPort
        );
    }

    @Test
    void shouldAddPermissionWhenRoleExists() {
        when(roleRepository.findById(10L)).thenReturn(Optional.of(Role.reconstitute(10L, UserRole.ADMIN, "Admin")));
        when(rolePermissionRepository.save(any(RolePermission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = rolePermissionService.add(new AddRolePermissionRequest(10L, "org:update"));

        assertThat(response.roleId()).isEqualTo(10L);
        assertThat(response.permission()).isEqualTo("org:update");
    }

    @Test
    void shouldThrowWhenAddingPermissionToMissingRole() {
        when(roleRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolePermissionService.add(new AddRolePermissionRequest(10L, "org:update")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Role not found");
    }

    @Test
    void shouldReturnPermissionsByRole() {
        when(rolePermissionRepository.findByRoleId(10L))
                .thenReturn(List.of(RolePermission.reconstitute(1L, 10L, "org:update")));

        assertThat(rolePermissionService.getByRoleId(10L)).singleElement()
                .satisfies(item -> assertThat(item.permission()).isEqualTo("org:update"));
    }

    @Test
    void shouldRecordAuditEntryUsingCurrentActorFallback() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));

        auditLogService.record(new RecordAuditLogRequest(
                20L,
                null,
                Map.of("email", "user@example.com"),
                "org:update",
                "organization",
                30L,
                Map.of("field", "name")
        ));

        ArgumentCaptor<AuditLogEntry> captor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getActorId()).isEqualTo(10L);
        assertThat(captor.getValue().getAction()).isEqualTo("org:update");
    }

    @Test
    void shouldReturnAuditLogByOrganizationWhenPermissionGranted() {
        UUID orgUuid = UUID.randomUUID();
        AuditLogEntry entry = AuditLogEntry.reconstitute(
                1L,
                20L,
                Instant.now(),
                10L,
                Map.of(),
                "org:update",
                "organization",
                30L,
                Map.of()
        );

        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(organizationRepository.findByOrgUuid(orgUuid)).thenReturn(Optional.of(organization(20L, orgUuid)));
        when(permissionCheckerPort.hasPermission(10L, 20L, "org:audit")).thenReturn(true);
        when(auditLogRepository.findByOrganizationId(20L)).thenReturn(List.of(entry));

        assertThat(auditLogService.getByOrganization(orgUuid)).singleElement()
                .satisfies(item -> assertThat(item.organizationId()).isEqualTo(20L));
    }

    @Test
    void shouldThrowWhenGettingAuditLogWithoutPermission() {
        UUID orgUuid = UUID.randomUUID();
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(10L));
        when(organizationRepository.findByOrgUuid(orgUuid)).thenReturn(Optional.of(organization(20L, orgUuid)));
        when(permissionCheckerPort.hasPermission(10L, 20L, "org:audit")).thenReturn(false);

        assertThatThrownBy(() -> auditLogService.getByOrganization(orgUuid))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void shouldReturnAuditLogByActorWhenUserExists() {
        UUID userUuid = UUID.randomUUID();
        when(userRepository.findByUserUuid(userUuid)).thenReturn(Optional.of(user(10L, userUuid)));
        when(auditLogRepository.findByActorId(10L))
                .thenReturn(List.of(AuditLogEntry.record(20L, 10L, Map.of(), "user:update", "user", 10L, Map.of())));

        assertThat(auditLogService.getByActor(userUuid)).hasSize(1);
    }

    private static Organization organization(Long id, UUID uuid) {
        return Organization.reconstitute(
                id,
                uuid,
                "Acme",
                Map.of(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
    }

    private static User user(Long id, UUID uuid) {
        return User.reconstitute(
                id,
                uuid,
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
}
