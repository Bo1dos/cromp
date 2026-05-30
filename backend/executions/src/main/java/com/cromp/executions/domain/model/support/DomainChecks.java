package com.cromp.executions.domain.model.support;

import java.util.Objects;

public final class DomainChecks {

    private DomainChecks() {
    }

    public static <T> T requireNonNullValue(T value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " must not be null");
    }
}
