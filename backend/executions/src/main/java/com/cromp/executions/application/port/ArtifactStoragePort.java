package com.cromp.executions.application.port;

import java.io.InputStream;

public interface ArtifactStoragePort {
    StoredArtifact store(Long executionId, String filename,
                         String contentType, InputStream data, long size);
    String generateDownloadUrl(String storagePath, int expirySeconds);
    InputStream download(String storagePath);
}
