package com.cromp.secrets.infrastructure.encryption;

import com.cromp.secrets.domain.service.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;

@Service
@Slf4j
public class AesEncryptionService implements EncryptionService {

    private final Key secretKey;

    public AesEncryptionService(@Value("${cromp.secrets.encryption.key}") String keyString) {
        byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
        // AES ключ должен быть 16, 24 или 32 байта
        int keySize = keyBytes.length;
        if (keySize != 16 && keySize != 24 && keySize != 32) {
            log.warn("AES key length is {}, padding/truncating to 32 bytes", keySize);
            keyBytes = Arrays.copyOf(keyBytes, 32); // pad to 32 bytes
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public byte[] encrypt(byte[] plaintext) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}