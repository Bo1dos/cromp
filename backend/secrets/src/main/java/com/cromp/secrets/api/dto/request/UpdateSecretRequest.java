package com.cromp.secrets.api.dto.request;

public record UpdateSecretRequest(String name, String description, String scope) {}
