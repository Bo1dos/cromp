package com.cromp.iam.domain;

import com.cromp.iam.domain.model.Organization;
import com.cromp.iam.domain.model.support.SchemaLimits;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrganizationTest {

    @Test
    void shouldCreateOrganizationWithUuidAndSettings() {
        Organization organization = Organization.create("  Acme  ", Map.of("region", "eu"));

        assertThat(organization.getOrgUuid()).isNotNull();
        assertThat(organization.getName()).isEqualTo("Acme");
        assertThat(organization.getSettings()).containsEntry("region", "eu");
    }

    @Test
    void shouldRenameAndTouchUpdatedAtWhenNameChanges() {
        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        Organization organization = Organization.reconstitute(
                1L,
                null,
                "Acme",
                Map.of(),
                before,
                before,
                null
        );

        organization.rename("  Renamed Org ");

        assertThat(organization.getName()).isEqualTo("Renamed Org");
        assertThat(organization.getUpdatedAt()).isAfter(before);
    }

    @Test
    void shouldUpdateSettingsAndTouchUpdatedAtWhenCalled() {
        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        Organization organization = Organization.reconstitute(
                1L,
                null,
                "Acme",
                Map.of("region", "us"),
                before,
                before,
                null
        );

        organization.updateSettings(Map.of("region", "eu"));

        assertThat(organization.getSettings()).containsEntry("region", "eu");
        assertThat(organization.getUpdatedAt()).isAfter(before);
    }

    @Test
    void shouldRejectBlankOrTooLongName() {
        assertThatThrownBy(() -> Organization.create(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");

        assertThatThrownBy(() -> Organization.create("a".repeat(SchemaLimits.ORG_NAME_MAX_LENGTH + 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }
}
