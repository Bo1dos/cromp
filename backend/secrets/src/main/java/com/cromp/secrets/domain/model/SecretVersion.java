package com.cromp.secrets.domain.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Arrays;

import static com.cromp.secrets.domain.model.support.DomainChecks.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SecretVersion {
    @EqualsAndHashCode.Include
    private Long id;
    private Long secretId;
    private int version;
    private byte[] valueCipher;
    private String keyId;
    private boolean active;
    private Long createdBy;
    private Instant createdAt;
    private Instant deprecatedAt;

    private SecretVersion(Long id, Long secretId, int version, byte[] valueCipher, String keyId,
                          boolean active, Long createdBy, Instant createdAt, Instant deprecatedAt) {
        this.id = id;
        this.secretId = requireNonNullValue(secretId, "secretId");
        if (version < 1) throw new IllegalArgumentException("version must be positive");
        this.version = version;
        this.valueCipher = requireNonNullValue(valueCipher, "valueCipher").clone();
        this.keyId = keyId;
        this.active = active;
        this.createdBy = createdBy;
        this.createdAt = requireNonNullValue(createdAt, "createdAt");
        this.deprecatedAt = deprecatedAt;
    }

    public static SecretVersion create(Long secretId, int version, byte[] valueCipher, String keyId, Long createdBy) {
        return new SecretVersion(null, secretId, version, valueCipher, keyId, true, createdBy, Instant.now(), null);
    }

    public static SecretVersion reconstitute(Long id, Long secretId, int version, byte[] valueCipher, String keyId,
                                             boolean active, Long createdBy, Instant createdAt, Instant deprecatedAt) {
        return new SecretVersion(id, secretId, version, valueCipher, keyId, active, createdBy, createdAt, deprecatedAt);
    }

    public byte[] getValueCipher() {
        return Arrays.copyOf(valueCipher, valueCipher.length);
    }

    public void deactivate() {
        if (active) {
            active = false;
            deprecatedAt = Instant.now();
        }
    }
}
