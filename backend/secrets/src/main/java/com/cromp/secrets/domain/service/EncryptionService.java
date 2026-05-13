package com.cromp.secrets.domain.service;

public interface EncryptionService {
    byte[] encrypt(byte[] plaintext);
    byte[] decrypt(byte[] ciphertext);
}