package com.cromp.iam.domain.model;

import com.cromp.iam.domain.model.support.SchemaLimits;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.cromp.iam.domain.model.support.DomainChecks.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RolePermission {

    private Long id;

    @EqualsAndHashCode.Include
    private Long roleId;

    @EqualsAndHashCode.Include
    private String permission;

    private RolePermission(Long id, Long roleId, String permission) {
        this.id = id;
        this.roleId = requireNonNullValue(roleId, "roleId");
        this.permission = requireMaxLength(
                requireText(permission, "permission"),
                SchemaLimits.ACTION_MAX_LENGTH,
                "permission"
        );
    }

    public static RolePermission of(Long roleId, String permission) {
        return new RolePermission(null, roleId, permission);
    }

    public static RolePermission reconstitute(Long id, Long roleId, String permission) {
        return new RolePermission(id, roleId, permission);
    }

    public void changePermission(String permission) {
        this.permission = requireMaxLength(
                requireText(permission, "permission"),
                SchemaLimits.ACTION_MAX_LENGTH,
                "permission"
        );
    }

    public void validate() {
        requireNonNullValue(roleId, "roleId");
        requireText(permission, "permission");
    }
}