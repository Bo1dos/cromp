package com.cromp.iam.domain.model;

import com.cromp.iam.domain.model.support.SchemaLimits;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static com.cromp.iam.domain.model.support.DomainChecks.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class User extends AbstractAuditableDomainEntity {

    @EqualsAndHashCode.Include
    private UUID userUuid;

    private String email;
    private String lastName;
    private String firstName;
    private String middleName;
    private String displayName;
    private String passwordHash;
    private Map<String, Object> profile;

    private User(Long id,
                 Instant createdAt,
                 Instant updatedAt,
                 Instant deletedAt,
                 UUID userUuid,
                 String email,
                 String lastName,
                 String firstName,
                 String middleName,
                 String displayName,
                 String passwordHash,
                 Map<String, Object> profile) {
        super(id, createdAt, updatedAt, deletedAt);
        this.userUuid = userUuid == null ? UUID.randomUUID() : userUuid;
        this.email = normalizeEmail(email);
        this.lastName = requireNonBlankMaxLength(lastName, SchemaLimits.USER_LAST_NAME_MAX_LENGTH, "lastName");
        this.firstName = requireNonBlankMaxLength(firstName, SchemaLimits.USER_FIRST_NAME_MAX_LENGTH, "firstName");
        this.middleName = requireNonBlankMaxLength(middleName, SchemaLimits.USER_MIDDLE_NAME_MAX_LENGTH, "middleName");
        this.displayName = requireNonBlankMaxLength(displayName, SchemaLimits.USER_DISPLAY_NAME_MAX_LENGTH, "displayName");
        this.passwordHash = passwordHash == null ? null : requireText(passwordHash, "passwordHash");
        this.profile = safeMap(profile);
    }

    public static User register(String email, String passwordHash) {
        return register(email, passwordHash, null, null, null, null, Map.of());
    }

    public static User register(String email,
                                String passwordHash,
                                String firstName,
                                String lastName,
                                String middleName,
                                String displayName,
                                Map<String, Object> profile) {
        return new User(
                null,
                Instant.now(),
                Instant.now(),
                null,
                UUID.randomUUID(),
                email,
                lastName,
                firstName,
                middleName,
                displayName,
                passwordHash,   
                profile
        );
    }

    public static User registerExternal(String email,
                                        String firstName,
                                        String lastName,
                                        String middleName,
                                        String displayName,
                                        Map<String, Object> profile) {
        return new User(
                null,
                Instant.now(),
                Instant.now(),
                null,
                UUID.randomUUID(),
                email,
                lastName,
                firstName,
                middleName,
                displayName,
                null,
                profile
        );
    }

    public static User reconstitute(Long id,
                                    UUID userUuid,
                                    String email,
                                    String lastName,
                                    String firstName,
                                    String middleName,
                                    String displayName,
                                    String passwordHash,
                                    Map<String, Object> profile,
                                    Instant createdAt,
                                    Instant updatedAt,
                                    Instant deletedAt) {
        return new User(
                id,
                createdAt,
                updatedAt,
                deletedAt,
                userUuid,
                email,
                lastName,
                firstName,
                middleName,
                displayName,
                passwordHash,
                profile
        );
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = requireText(passwordHash, "passwordHash");
        touch();
    }

    public void updateName(String firstName, String lastName, String middleName, String displayName) {
        this.firstName = requireNonBlankMaxLength(firstName, SchemaLimits.USER_FIRST_NAME_MAX_LENGTH, "firstName");
        this.lastName = requireNonBlankMaxLength(lastName, SchemaLimits.USER_LAST_NAME_MAX_LENGTH, "lastName");
        this.middleName = requireNonBlankMaxLength(middleName, SchemaLimits.USER_MIDDLE_NAME_MAX_LENGTH, "middleName");
        this.displayName = requireNonBlankMaxLength(displayName, SchemaLimits.USER_DISPLAY_NAME_MAX_LENGTH, "displayName");
        touch();
    }

    public void updateProfile(Map<String, Object> profile) {
        this.profile = safeMap(profile);
        touch();
    }

    public void changeEmail(String email) {
        this.email = normalizeEmail(email);
        touch();
    }

    public void validate() {
        requireNonNullValue(userUuid, "userUuid");
        requireText(email, "email");
        if (passwordHash != null) {
            requireText(passwordHash, "passwordHash");
        }
    }
}