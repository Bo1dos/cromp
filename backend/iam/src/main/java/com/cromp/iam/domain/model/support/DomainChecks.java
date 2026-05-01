package com.cromp.iam.domain.model.support;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class DomainChecks {

    private DomainChecks() {
    }

    public static <T> T requireNonNullValue(T value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " must not be null");
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    public static String requireMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        }
        return value;
    }

    /**
     * Проверяет длину и возвращает значение.
     * Если после trim() строка становится пустой — возвращает null.
     * Используется для необязательных полей, где пустая строка не должна сохраняться.
     */
    public static String requireNonBlankMaxLength(String value, int maxLength, String fieldName) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        }
        return trimmed;
    }

    public static String normalizeEmail(String email) {
        return requireText(email, "email").toLowerCase(Locale.ROOT);
    }

    /**
     * Создаёт неизменяемую копию переданного Map.
     * <p>
     * Важно: если в исходном Map присутствуют значения null,
     * будет выброшено {@link NullPointerException}, так как {@link Map#copyOf}
     * не допускает null-значений. Убедитесь, что входные данные очищены от null.
     * </p>
     */
    public static Map<String, Object> safeMap(Map<String, Object> value) {
        return value == null ? Map.of() : Map.copyOf(value);
    }
}