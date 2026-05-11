package com.cromp.jobs.api.dto.response;

import java.util.Map;

public record JobTargetResponse(
        String type,
        String url,
        String method,
        Map<String, String> headers,
        String body
) {}