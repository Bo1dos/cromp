package com.cromp.iam.domain.model;

import com.cromp.iam.domain.model.support.SchemaLimits;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.cromp.iam.domain.model.support.DomainChecks.*;

import com.cromp.iam.domain.model.enums.UserRole;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {

    private Long id;

    @EqualsAndHashCode.Include
    private UserRole name;

    private String description;

    private Role(Long id, UserRole name, String description) {
        this.id = id;
        this.name = requireNonNullValue(name, "name");
        this.description = normalizeDescription(description);
    }

    public static Role of(UserRole name, String description) {
        return new Role(null, name, description);
    }

    public static Role reconstitute(Long id, UserRole name, String description) {
        return new Role(id, name, description);
    }

    public void changeDescription(String description) {
        this.description = normalizeDescription(description);
    }

    private String normalizeDescription(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return requireMaxLength(trimmed, SchemaLimits.ROLE_DESCRIPTION_MAX_LENGTH, "description");
    }

    public void validate() {
        requireNonNullValue(name, "name");
    }
}