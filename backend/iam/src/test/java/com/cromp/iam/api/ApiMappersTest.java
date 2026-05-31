package com.cromp.iam.api;

import com.cromp.iam.api.mapper.AuditLogApiMapper;
import com.cromp.iam.api.mapper.AuthApiMapper;
import com.cromp.iam.api.mapper.InvitationApiMapper;
import com.cromp.iam.api.mapper.MembershipApiMapper;
import com.cromp.iam.api.mapper.OrganizationApiMapper;
import com.cromp.iam.api.mapper.RolePermissionApiMapper;
import com.cromp.iam.api.mapper.UserApiMapper;
import com.cromp.iam.domain.model.AuditLogEntry;
import com.cromp.iam.domain.model.Invitation;
import com.cromp.iam.domain.model.Membership;
import com.cromp.iam.domain.model.Organization;
import com.cromp.iam.domain.model.RolePermission;
import com.cromp.iam.domain.model.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApiMappersTest {

    private final UserApiMapper userApiMapper = new UserApiMapper();
    private final OrganizationApiMapper organizationApiMapper = new OrganizationApiMapper();
    private final MembershipApiMapper membershipApiMapper = new MembershipApiMapper();
    private final InvitationApiMapper invitationApiMapper = new InvitationApiMapper();
    private final RolePermissionApiMapper rolePermissionApiMapper = new RolePermissionApiMapper();
    private final AuditLogApiMapper auditLogApiMapper = new AuditLogApiMapper();
    private final AuthApiMapper authApiMapper = new AuthApiMapper(userApiMapper, organizationApiMapper);

    @Test
    void shouldMapUserOrganizationAndAuthResponses() {
        User user = User.reconstitute(
                1L,
                UUID.randomUUID(),
                "user@example.com",
                "Doe",
                "John",
                null,
                "John Doe",
                "hash",
                Map.of("locale", "en"),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T01:00:00Z"),
                null
        );
        Organization organization = Organization.reconstitute(
                2L,
                UUID.randomUUID(),
                "Acme",
                Map.of("plan", "pro"),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T01:00:00Z"),
                null
        );

        var authResponse = authApiMapper.toLoginResponse("token", user, List.of(organization));

        assertThat(userApiMapper.toResponse(user).email()).isEqualTo("user@example.com");
        assertThat(organizationApiMapper.toResponse(organization).settings()).containsEntry("plan", "pro");
        assertThat(authResponse.accessToken()).isEqualTo("token");
        assertThat(authResponse.organizations()).singleElement().satisfies(item -> assertThat(item.name()).isEqualTo("Acme"));
    }

    @Test
    void shouldMapMembershipInvitationRolePermissionAndAuditLog() {
        Membership membership = Membership.reconstitute(
                3L,
                UUID.randomUUID(),
                1L,
                2L,
                4L,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T01:00:00Z"),
                null
        );
        Invitation invitation = Invitation.create(
                2L,
                "user@example.com",
                "hash",
                4L,
                1L,
                Instant.now().plusSeconds(3600)
        );
        RolePermission rolePermission = RolePermission.reconstitute(5L, 4L, "org:update");
        AuditLogEntry auditLogEntry = AuditLogEntry.record(
                2L,
                1L,
                null,
                "org:update",
                null,
                2L,
                null
        );

        assertThat(membershipApiMapper.toResponse(membership, "ADMIN").roleName()).isEqualTo("ADMIN");
        assertThat(invitationApiMapper.toResponse(invitation, "ADMIN").status()).isEqualTo("PENDING");
        assertThat(rolePermissionApiMapper.toResponse(rolePermission).permission()).isEqualTo("org:update");
        assertThat(auditLogApiMapper.toResponse(auditLogEntry).resourceType()).isNull();
    }
}
