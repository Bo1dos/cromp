package com.cromp.iam.domain;

import com.cromp.iam.domain.model.User;
import com.cromp.iam.domain.model.support.SchemaLimits;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void shouldRegisterAndNormalizeEmailWhenValidDataProvided() {
        User user = User.register(
                "  USER@Example.COM  ",
                "hash-123",
                " John ",
                " Doe ",
                " Middle ",
                " Johnny ",
                Map.of("tz", "UTC")
        );

        assertThat(user.getUserUuid()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getMiddleName()).isEqualTo("Middle");
        assertThat(user.getDisplayName()).isEqualTo("Johnny");
        assertThat(user.getProfile()).containsEntry("tz", "UTC");
    }

    @Test
    void shouldUpdateNameAndTouchUpdatedAtWhenCalled() {
        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        User user = User.reconstitute(
                1L,
                null,
                "user@example.com",
                "Doe",
                "John",
                null,
                "John Doe",
                "hash-123",
                Map.of(),
                before,
                before,
                null
        );

        user.updateName(" Jane ", " Smith ", " Ann ", " Jane Smith ");

        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getMiddleName()).isEqualTo("Ann");
        assertThat(user.getDisplayName()).isEqualTo("Jane Smith");
        assertThat(user.getUpdatedAt()).isAfter(before);
    }

    @Test
    void shouldUpdateProfileAndTouchUpdatedAtWhenProfileProvided() {
        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        User user = User.reconstitute(
                1L,
                null,
                "user@example.com",
                "Doe",
                "John",
                null,
                "John Doe",
                "hash-123",
                Map.of(),
                before,
                before,
                null
        );

        user.updateProfile(Map.of("locale", "ru-RU"));

        assertThat(user.getProfile()).containsEntry("locale", "ru-RU");
        assertThat(user.getUpdatedAt()).isAfter(before);
    }

    @Test
    void shouldChangePasswordHashAndTouchUpdatedAtWhenCalled() {
        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        User user = User.reconstitute(
                1L,
                null,
                "user@example.com",
                "Doe",
                "John",
                null,
                "John Doe",
                "hash-123",
                Map.of(),
                before,
                before,
                null
        );

        user.changePasswordHash("new-hash");

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(user.getUpdatedAt()).isAfter(before);
    }

    @Test
    void shouldChangeEmailAndNormalizeItWhenCalled() {
        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        User user = User.reconstitute(
                1L,
                null,
                "user@example.com",
                "Doe",
                "John",
                null,
                "John Doe",
                "hash-123",
                Map.of(),
                before,
                before,
                null
        );

        user.changeEmail("  NEW@Example.com ");

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getUpdatedAt()).isAfter(before);
    }

    @Test
    void shouldAllowNullOptionalNamesButRejectTooLongValues() {
        assertThat(User.register("user@example.com", "hash").getFirstName()).isNull();

        assertThatThrownBy(() -> User.register(
                "user@example.com",
                "hash",
                "a".repeat(SchemaLimits.USER_FIRST_NAME_MAX_LENGTH + 1),
                null,
                null,
                null,
                Map.of()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("firstName");
    }

    @Test
    void shouldConvertBlankOptionalFieldsToNullWhenUpdatingName() {
        User user = User.register("user@example.com", "hash", "John", "Doe", "Mid", "Display", Map.of());

        user.updateName("   ", "  ", "\t", "\n");

        assertThat(user.getFirstName()).isNull();
        assertThat(user.getLastName()).isNull();
        assertThat(user.getMiddleName()).isNull();
        assertThat(user.getDisplayName()).isNull();
    }

    @Test
    void shouldRejectBlankEmailWhenRegistering() {
        assertThatThrownBy(() -> User.register("   ", "hash"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");
    }
}
