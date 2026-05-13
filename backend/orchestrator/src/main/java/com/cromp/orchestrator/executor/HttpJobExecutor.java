package com.cromp.orchestrator.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class HttpJobExecutor {
    private final RestClient.Builder restClientBuilder;

    @SuppressWarnings("unchecked")
    public ExecutionResult execute(Map<String, Object> config) {
        Map<String, Object> target = (Map<String, Object>) config.get("target");
        if (target == null || !"HTTP".equalsIgnoreCase(String.valueOf(target.get("type")))) {
            return ExecutionResult.failure("Unsupported target type", "UNSUPPORTED_TARGET", Map.of());
        }
        String url = String.valueOf(target.get("url"));
        String method = String.valueOf(target.getOrDefault("method", "GET"));
        String body = target.get("body") != null ? String.valueOf(target.get("body")) : null;
        Map<String, String> headers = target.get("headers") instanceof Map<?, ?> raw
                ? raw.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue())))
                : Map.of();

        try {
            RestClient.RequestBodySpec request = restClientBuilder.build()
                    .method(HttpMethod.valueOf(method.toUpperCase()))
                    .uri(url);
            headers.forEach(request::header);
            int status = body == null || body.isBlank()
                    ? request.retrieve().toBodilessEntity().getStatusCode().value()
                    : request.body(body).retrieve().toBodilessEntity().getStatusCode().value();
            if (status >= 200 && status < 300) {
                return ExecutionResult.success(Map.of("httpStatus", status));
            }
            return ExecutionResult.failure("HTTP target returned non-success status", "HTTP_" + status,
                    Map.of("httpStatus", status));
        } catch (RuntimeException ex) {
            return ExecutionResult.failure(ex.getMessage(), ex.getClass().getSimpleName(), Map.of());
        }
    }
}
