package com.cromp.secrets.domain.model;

import com.cromp.secrets.domain.model.support.DomainChecks;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SecretVersion {
    private Long id;
    private Long secretId;
    private int version;
    private byte[] valueCipher;       // зашифрованное значение
    private String keyId;             // идентификатор ключа (если используем KMS)
    private boolean active;
    private Long createdBy;
    private Instant createdAt;
    private Instant deprecatedAt;

    private SecretVersion(Long id, Long secretId, int version, byte[] valueCipher,
                          String keyId, boolean active, Long createdBy,
                          Instant createdAt, Instant deprecatedAt) {
        this.id = id;
        this.secretId = DomainChecks.requireNonNullValue(secretId, "secretId");
        this.version = version;
        this.valueCipher = DomainChecks.requireNonNullValue(valueCipher, "valueCipher");
        this.keyId = keyId;
        this.active = active;
        this.createdBy = createdBy;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.deprecatedAt = deprecatedAt;
    }

    public static SecretVersion createNew(Long secretId, int version, byte[] encryptedValue,
                                          String keyId, Long createdBy) {
        return new SecretVersion(null, secretId, version, encryptedValue, keyId, true,
                createdBy, Instant.now(), null);
    }

    public static SecretVersion reconstitute(Long id, Long secretId, int version,
                                             byte[] valueCipher, String keyId,
                                             boolean active, Long createdBy,
                                             Instant createdAt, Instant deprecatedAt) {
        return new SecretVersion(id, secretId, version, valueCipher, keyId, active,
                createdBy, createdAt, deprecatedAt);
    }

    public void deprecate() {
        if (!this.active) throw new IllegalStateException("Version already deprecated");
        this.active = false;
        this.deprecatedAt = Instant.now();
    }
}