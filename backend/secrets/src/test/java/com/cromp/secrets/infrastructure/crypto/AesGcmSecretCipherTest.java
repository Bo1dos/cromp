package com.cromp.secrets.infrastructure.crypto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class AesGcmSecretCipherTest {
    @Test
    void encryptsAndDecryptsSecretValue() {
        String key = Base64.getEncoder().encodeToString("test-master-key-32-bytes-value!!".getBytes());
        AesGcmSecretCipher cipher = new AesGcmSecretCipher(key, "test-key");

        var encrypted = cipher.encrypt("plain-secret");

        assertThat(encrypted.keyId()).isEqualTo("test-key");
        assertThat(encrypted.cipherText()).isNotEmpty();
        assertThat(Arrays.equals(encrypted.cipherText(), "plain-secret".getBytes())).isFalse();
        assertThat(cipher.decrypt(encrypted.cipherText())).isEqualTo("plain-secret");
    }
}
