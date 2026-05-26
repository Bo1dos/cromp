package com.cromp.executions.infrastructure.storage;

import com.cromp.executions.application.port.ArtifactStoragePort;
import com.cromp.executions.application.port.StoredArtifact;
import com.cromp.executions.infrastructure.storage.config.MinioProperties;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class MinioArtifactStorage implements ArtifactStoragePort {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Override
    public StoredArtifact store(Long executionId, String filename,
                                String contentType, InputStream data, long size) {
        ensureBucketExists();

        String objectName = "executions/%d/%s".formatted(executionId, filename);

        // Читаем в память для подсчёта чексуммы (файлы артефактов обычно небольшие)
        byte[] bytes;
        try {
            bytes = data.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read artifact stream", e);
        }

        String checksum = sha256Hex(bytes);

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .stream(new java.io.ByteArrayInputStream(bytes), bytes.length, -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to store artifact in MinIO", e);
        }

        return new StoredArtifact(objectName, bytes.length, checksum);
    }

    @Override
    public String generateDownloadUrl(String storagePath, int expirySeconds) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(properties.getBucket())
                    .object(storagePath)
                    .method(Method.GET)
                    .expiry(expirySeconds, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.getBucket()).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(properties.getBucket()).build());
                log.info("Created MinIO bucket: {}", properties.getBucket());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure bucket exists", e);
        }
    }

    private String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(data));
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}