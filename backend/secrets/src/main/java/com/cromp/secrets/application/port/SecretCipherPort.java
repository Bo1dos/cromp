package com.cromp.secrets.application.port;

public interface SecretCipherPort {
    EncryptedSecret encrypt(String plainText);
    String decrypt(byte[] cipherText);

    record EncryptedSecret(byte[] cipherText, String keyId) {}
}
