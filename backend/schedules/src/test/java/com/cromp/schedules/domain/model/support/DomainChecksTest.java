package com.cromp.schedules.domain.model.support;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainChecksTest {

    @Test
    void requireNonNullValueShouldReturnValueWhenPresent() {
        assertThat(DomainChecks.requireNonNullValue("value", "field")).isEqualTo("value");
    }

    @Test
    void requireNonNullValueShouldThrowWhenNull() {
        assertThatThrownBy(() -> DomainChecks.requireNonNullValue(null, "field"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("field must not be null");
    }

    @Test
    void requireTextShouldTrimAndRejectNullAndBlank() {
        assertThat(DomainChecks.requireText("  hello  ", "field")).isEqualTo("hello");

        assertThatThrownBy(() -> DomainChecks.requireText(null, "field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("field must not be blank");

        assertThatThrownBy(() -> DomainChecks.requireText("   ", "field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("field must not be blank");
    }

    @Test
    void requireMaxLengthShouldAllowBoundaryAndRejectOverflow() {
        assertThat(DomainChecks.requireMaxLength("abcd", 4, "field")).isEqualTo("abcd");
        assertThat(DomainChecks.requireMaxLength(null, 4, "field")).isNull();

        assertThatThrownBy(() -> DomainChecks.requireMaxLength("abcde", 4, "field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("field must not exceed 4 characters");
    }

    @Test
    void requireNonBlankMaxLengthShouldHandleNullBlankTrimAndOverflow() {
        assertThat(DomainChecks.requireNonBlankMaxLength(null, 4, "field")).isNull();
        assertThat(DomainChecks.requireNonBlankMaxLength("   ", 4, "field")).isNull();
        assertThat(DomainChecks.requireNonBlankMaxLength("  abcd  ", 4, "field")).isEqualTo("abcd");

        assertThatThrownBy(() -> DomainChecks.requireNonBlankMaxLength("abcde", 4, "field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("field must not exceed 4 characters");
    }

    @Test
    void safeMapShouldReturnEmptyMapForNullAndCopyInput() {
        assertThat(DomainChecks.safeMap(null)).isEmpty();

        Map<String, Object> original = Map.of("key", "value");
        Map<String, Object> safe = DomainChecks.safeMap(original);

        assertThat(safe).containsEntry("key", "value");
        assertThatThrownBy(() -> safe.put("newKey", "newValue"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
