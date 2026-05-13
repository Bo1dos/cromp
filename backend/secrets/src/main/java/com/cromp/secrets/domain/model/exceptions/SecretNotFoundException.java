package com.cromp.secrets.domain.model.exceptions;

public class SecretNotFoundException extends RuntimeException {
    public SecretNotFoundException(Long secretId) {
        super("Secret not found: " + secretId);
    }
}
