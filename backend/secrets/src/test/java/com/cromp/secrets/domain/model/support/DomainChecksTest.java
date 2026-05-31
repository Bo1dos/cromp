package com.cromp.secrets.domain.model.support;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainChecksTest {

    @Test
    void shouldReturnValueWhenNonNullValueIsProvided() {
        assertThat(DomainChecks.requireNonNullValue("value", "field")).isEqualTo("value");
    }

    @Test
    void shouldThrowWhenNonNullValueIsNull() {
        assertThatThrownBy(() -> DomainChecks.requireNonNullValue(null, "field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("field must not be null");
    }

    @Test
    void shouldTrimTextWhenValidTextIsProvided() {
        assertThat(DomainChecks.requireText("  hello  ", "field")).isEqualTo("hello");
    }

    @Test
    void shouldThrowWhenTextIsBlank() {
        assertThatThrownBy(() -> DomainChecks.requireText("   ", "field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("field must not be blank");
    }

    @Test
    void shouldReturnValueWhenMaxLengthIsRespected() {
        assertThat(DomainChecks.requireMaxLength("abc", 3, "field")).isEqualTo("abc");
    }

    @Test
    void shouldThrowWhenMaxLengthIsExceeded() {
        assertThatThrownBy(() -> DomainChecks.requireMaxLength("abcd", 3, "field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("field must not exceed 3 characters");
    }

    @Test
    void shouldReturnEmptyMapWhenSafeMapIsCalledWithNull() {
        assertThat(DomainChecks.safeMap(null)).isEmpty();
    }

    @Test
    void shouldCopyMapWhenSafeMapIsCalledWithValue() {
        Map<String, Object> original = Map.of("key", "value");

        Map<String, Object> safe = DomainChecks.safeMap(original);

        assertThat(safe).containsEntry("key", "value");
        assertThatThrownBy(() -> safe.put("another", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
