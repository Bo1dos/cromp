package com.cromp.secrets.domain.model.exceptions;

public class SecretVersionNotFoundException extends RuntimeException {
    public SecretVersionNotFoundException(String message) { super(message); }
    public SecretVersionNotFoundException(Long secretId, int version) {
        super("Version " + version + " not found for secret " + secretId);
    }
}

