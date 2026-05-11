package com.cromp.jobs.domain.model;

import java.util.UUID;

public record JobSecretRef(
    UUID secretId,
    String envName
) {
    public JobSecretRef {
        if (secretId == null) throw new IllegalArgumentException("secretId must not be null");
        if (envName == null || envName.isBlank()) throw new IllegalArgumentException("envName must not be blank");
    }
}