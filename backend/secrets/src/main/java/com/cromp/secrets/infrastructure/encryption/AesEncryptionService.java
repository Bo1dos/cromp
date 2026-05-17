package com.cromp.secrets.infrastructure.encryption;

import com.cromp.secrets.domain.service.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

@Service
@Slf4j
public class AesEncryptionService implements EncryptionService {

    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int KEY_LENGTH_BYTES = 32;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesEncryptionService(@Value("${cromp.secrets.encryption.key}") String keyString) {
        if (keyString == null || keyString.isBlank()) {
            throw new IllegalStateException("cromp.secrets.encryption.key must not be blank");
        }
        this.secretKey = new SecretKeySpec(deriveKey(keyString), "AES");
    }

    @Override
    public byte[] encrypt(byte[] plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext);

            return ByteBuffer.allocate(iv.length + ciphertext.length)
                    .put(iv)
                    .put(ciphertext)
                    .array();
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) {
        try {
            if (ciphertext == null || ciphertext.length <= IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("Invalid ciphertext");
            }

            byte[] iv = Arrays.copyOfRange(ciphertext, 0, IV_LENGTH_BYTES);
            byte[] payload = Arrays.copyOfRange(ciphertext, IV_LENGTH_BYTES, ciphertext.length);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return cipher.doFinal(payload);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private byte[] deriveKey(String keyString) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] raw = digest.digest(keyString.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(raw, KEY_LENGTH_BYTES);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to derive encryption key", e);
        }
    }
}