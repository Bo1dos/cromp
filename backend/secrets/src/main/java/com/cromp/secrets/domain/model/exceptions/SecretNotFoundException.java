package com.cromp.secrets.domain.model.exceptions;

public class SecretNotFoundException extends RuntimeException {
    public SecretNotFoundException(String message) { super(message); }
    public SecretNotFoundException(Long id) { super("Secret not found: id=" + id); }
}

