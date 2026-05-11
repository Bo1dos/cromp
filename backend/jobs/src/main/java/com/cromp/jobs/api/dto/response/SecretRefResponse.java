package com.cromp.jobs.api.dto.response;

import java.util.UUID;

public record SecretRefResponse(
        UUID secretId,
        String envName
) {}