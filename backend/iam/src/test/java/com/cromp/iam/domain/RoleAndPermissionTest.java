package com.cromp.iam.domain;

import com.cromp.iam.domain.model.Role;
import com.cromp.iam.domain.model.RolePermission;
import com.cromp.iam.domain.model.enums.UserRole;
import com.cromp.iam.domain.model.support.SchemaLimits;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleAndPermissionTest {

    @Test
    void shouldNormalizeRoleDescriptionWhenCreatedOrChanged() {
        Role role = Role.of(UserRole.ADMIN, "  Organization admin  ");
        assertThat(role.getDescription()).isEqualTo("Organization admin");

        role.changeDescription("   ");
        assertThat(role.getDescription()).isNull();
    }

    @Test
    void shouldRejectTooLongRoleDescription() {
        assertThatThrownBy(() -> Role.of(UserRole.ADMIN, "a".repeat(SchemaLimits.ROLE_DESCRIPTION_MAX_LENGTH + 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("description");
    }

    @Test
    void shouldValidatePermissionAndAllowChangingIt() {
        RolePermission rolePermission = RolePermission.of(10L, "org:update");
        assertThat(rolePermission.getPermission()).isEqualTo("org:update");

        rolePermission.changePermission("org:invite");
        assertThat(rolePermission.getPermission()).isEqualTo("org:invite");
    }

    @Test
    void shouldRejectBlankOrTooLongPermission() {
        assertThatThrownBy(() -> RolePermission.of(10L, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("permission");

        assertThatThrownBy(() -> RolePermission.of(10L, "a".repeat(SchemaLimits.ACTION_MAX_LENGTH + 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("permission");
    }
}
