package com.cromp.executions.application.port;

public record StoredArtifact(
        String storagePath,
        long sizeBytes,
        String checksumSha256
) {}
