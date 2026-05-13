package com.cromp.executions.domain.model.support;

import java.util.Map;
import java.util.Objects;

public final class DomainChecks {
    private DomainChecks() {}

    public static <T> T requireNonNullValue(T value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " must not be null");
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(fieldName + " must not be blank");
        return value.trim();
    }

    public static Map<String, Object> safeMap(Map<String, Object> value) {
        return value == null ? Map.of() : Map.copyOf(value);
    }
}
