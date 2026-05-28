package com.cromp.secrets.infrastructure.encryption;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AesEncryptionServiceTest {

    @Test
    void shouldThrowWhenKeyIsBlank() {
        assertThatThrownBy(() -> new AesEncryptionService("   "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    void shouldEncryptAndDecryptRoundTripWhenKeyIsValid() {
        AesEncryptionService service = new AesEncryptionService("super-secret-key");
        byte[] plaintext = "hello secrets".getBytes(StandardCharsets.UTF_8);

        byte[] ciphertext = service.encrypt(plaintext);

        assertThat(ciphertext).isNotEmpty();
        assertThat(service.decrypt(ciphertext)).isEqualTo(plaintext);
    }

    @Test
    void shouldProduceDifferentCiphertextForSamePlaintextBecauseOfRandomIv() {
        AesEncryptionService service = new AesEncryptionService("super-secret-key");
        byte[] plaintext = "hello secrets".getBytes(StandardCharsets.UTF_8);

        byte[] firstCiphertext = service.encrypt(plaintext);
        byte[] secondCiphertext = service.encrypt(plaintext);

        assertThat(firstCiphertext).isNotEqualTo(secondCiphertext);
        assertThat(service.decrypt(firstCiphertext)).isEqualTo(plaintext);
        assertThat(service.decrypt(secondCiphertext)).isEqualTo(plaintext);
    }

    @Test
    void shouldThrowWhenCiphertextIsInvalidOrTampered() {
        AesEncryptionService service = new AesEncryptionService("super-secret-key");

        assertThatThrownBy(() -> service.decrypt(new byte[]{1, 2, 3}))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Decryption failed");

        byte[] ciphertext = service.encrypt("hello".getBytes(StandardCharsets.UTF_8));
        ciphertext[ciphertext.length - 1] ^= 0x01;

        assertThatThrownBy(() -> service.decrypt(ciphertext))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Decryption failed");
    }

    @Test
    void shouldKeepRoundTripWorkingForUtf8Payloads() {
        AesEncryptionService service = new AesEncryptionService("another-super-secret-key");
        byte[] plaintext = "привет, secrets".getBytes(StandardCharsets.UTF_8);

        byte[] ciphertext = service.encrypt(plaintext);
        byte[] decrypted = service.decrypt(ciphertext);

        assertThat(new String(decrypted, StandardCharsets.UTF_8)).isEqualTo("привет, secrets");
        assertThat(Arrays.equals(plaintext, decrypted)).isTrue();
    }
}
