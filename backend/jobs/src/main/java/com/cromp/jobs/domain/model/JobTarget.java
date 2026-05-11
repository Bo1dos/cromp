package com.cromp.jobs.domain.model;

import com.cromp.jobs.domain.model.support.DomainChecks;
import com.cromp.jobs.domain.model.support.SchemaLimits;
import java.util.Map;

public record JobTarget(
    String type,
    String url,
    String method,
    Map<String, String> headers,
    String body
) {
    public JobTarget {
        DomainChecks.requireText(type, "type");
        DomainChecks.requireText(url, "url");
        DomainChecks.requireMaxLength(url, SchemaLimits.TARGET_URL_MAX_LENGTH, "url");
        DomainChecks.requireText(method, "method");
        DomainChecks.requireMaxLength(method, SchemaLimits.METHOD_MAX_LENGTH, "method");
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        body = body != null ? body.trim() : "";
    }

    public static JobTarget forHttp(String url, String method, Map<String, String> headers, String body) {
        return new JobTarget("HTTP", url, method, headers, body);
    }
}