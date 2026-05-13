package com.cromp.secrets.api.dto.response;

public record SecretValueResponse(Long secretId, int version, String value) {}
