package com.cromp.notifications.domain.model.support;

import java.util.Map;

public final class DomainChecks {

    private DomainChecks() {
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    public static <T> T requireNonNullValue(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> safeMap(Map<K, V> map) {
        return map == null ? Map.of() : Map.copyOf(map);
    }
}
