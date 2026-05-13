package com.cromp.secrets.domain.model.exceptions;

public class InvalidSecretStateException extends RuntimeException {
    public InvalidSecretStateException(String message) { super(message); }
}
