package com.cromp.jobs.domain.support;

import com.cromp.jobs.domain.model.support.DomainChecks;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainChecksTest {

    @Test
    void shouldTrimTextWhenTextIsValid() {
        assertThat(DomainChecks.requireText("  job-name  ", "name")).isEqualTo("job-name");
    }

    @Test
    void shouldThrowWhenTextIsBlank() {
        assertThatThrownBy(() -> DomainChecks.requireText("   ", "name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name must not be blank");
    }

    @Test
    void shouldThrowWhenValueIsNull() {
        assertThatThrownBy(() -> DomainChecks.requireNonNullValue(null, "organizationId"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("organizationId must not be null");
    }

    @Test
    void shouldThrowWhenTextExceedsMaxLength() {
        assertThatThrownBy(() -> DomainChecks.requireMaxLength("x".repeat(4), 3, "name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name must not exceed 3 characters");
    }

    @Test
    void shouldReturnNullWhenOptionalTextIsBlank() {
        assertThat(DomainChecks.requireNonBlankMaxLength("   ", 10, "description")).isNull();
    }

    @Test
    void shouldReturnImmutableMapWhenMapIsProvided() {
        Map<String, Object> map = DomainChecks.safeMap(Map.of("a", 1));

        assertThat(map).containsEntry("a", 1);
        assertThatThrownBy(() -> map.put("b", 2)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldReturnEmptyMapWhenMapIsNull() {
        assertThat(DomainChecks.safeMap(null)).isEmpty();
    }
}
