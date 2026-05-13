package com.cromp.secrets.domain.model.support;

import java.util.Map;

public final class DomainChecks {
    private DomainChecks() {}
    public static <T> T requireNonNullValue(T value, String fieldName) {
        if (value == null) throw new IllegalArgumentException(fieldName + " must not be null");
        return value;
    }
    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(fieldName + " must not be blank");
        return value.trim();
    }
    public static String requireMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength)
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        return value;
    }
    public static Map<String, Object> safeMap(Map<String, Object> value) {
        return value == null ? Map.of() : Map.copyOf(value);
    }
}