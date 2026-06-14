package com.cromp.orchestrator.executor.adapter;

import com.cromp.jobs.domain.model.JobTarget;
import com.cromp.orchestrator.config.OrchestratorProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * Выполняет HTTP-вызов согласно конфигу {@link JobTarget}.
 *
 * <p><b>Dry-run:</b> если {@code orchestrator.dry-run=true} — запрос не отправляется,
 * метод возвращает синтетический успешный результат. Полезно для отладки.
 *
 * <p><b>Заголовки:</b>
 * <ul>
 *   <li>Все заголовки из {@code JobTarget.headers} — как есть.
 *   <li>{@code X-Execution-Id} — UUID попытки для идемпотентности на стороне получателя.
 *   <li>Значения секретов подставляются как дополнительные заголовки (envName → value).
 * </ul>
 *
 * <p>Таймаут задаётся на уровне {@link RestClient} через {@code RestClientConfig} —
 * здесь дублировать не нужно.
 */
@Slf4j
@Component
public class HttpTaskAdapter {

    private final RestClient restClient;
    private final OrchestratorProperties properties;

    public HttpTaskAdapter(
            @Qualifier("orchestratorRestClient") RestClient restClient,
            OrchestratorProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    /**
     * @param attemptUuid UUID попытки — добавляется в заголовок {@code X-Execution-Id}
     * @param target      HTTP-конфиг задачи (url, method, headers, body)
     * @param resolvedSecrets Map envName → plaintext; значения добавляются как заголовки
     * @return результат вызова
     */
    public HttpTaskResult execute(UUID attemptUuid, JobTarget target,
                                  Map<String, String> resolvedSecrets) {
        // Dry-run: не отправляем реальный запрос
        if (properties.isDryRun()) {
            log.info("[executor] dry-run mode, skipping HTTP call attemptUuid={} url={}",
                    attemptUuid, target.url());
            return HttpTaskResult.success(200, "{\"dry-run\":true}");
        }

        log.debug("[executor] executing HTTP {} {} attemptUuid={}",
                target.method(), target.url(), attemptUuid);

        try {
            var requestSpec = restClient
                    .method(HttpMethod.valueOf(target.method().toUpperCase()))
                    .uri(target.url())
                    // Идентификатор для идемпотентности
                    .header("X-Execution-Id", attemptUuid.toString());

            // Заголовки из конфига задачи
            for (Map.Entry<String, String> h : target.headers().entrySet()) {
                requestSpec.header(h.getKey(), h.getValue());
            }

            // Значения секретов как заголовки (envName = заголовок)
            for (Map.Entry<String, String> secret : resolvedSecrets.entrySet()) {
                requestSpec.header(secret.getKey(), secret.getValue());
            }

            // Тело запроса, если есть
            if (target.body() != null && !target.body().isBlank()) {
                requestSpec
                        .header("Content-Type", "application/json")
                        .body(target.body());
            }

            ResponseEntity<byte[]> response = requestSpec
                    .retrieve()
                    .toEntity(byte[].class);

            int statusCode = response.getStatusCode().value();
            byte[] bodyBytes = response.getBody();
            MediaType ct = response.getHeaders().getContentType();
            String body = bodyToText(ct, bodyBytes);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[executor] HTTP success status={} attemptUuid={}", statusCode, attemptUuid);
                if (isBinaryContent(ct) && bodyBytes != null && bodyBytes.length > 0) {
                    return HttpTaskResult.success(statusCode, truncate(body),
                            bodyBytes, ct != null ? ct.toString() : "application/octet-stream");
                }
                return HttpTaskResult.success(statusCode, truncate(body));
            } else {
                log.warn("[executor] HTTP non-2xx status={} attemptUuid={}", statusCode, attemptUuid);
                return HttpTaskResult.failure(statusCode, truncate(body),
                        "HTTP_" + statusCode, "Non-2xx response: " + statusCode);
            }

        } catch (HttpStatusCodeException e) {
            int statusCode = e.getStatusCode().value();
            log.warn("[executor] HTTP error status={} attemptUuid={}", statusCode, attemptUuid);
            return HttpTaskResult.failure(
                    statusCode,
                    truncate(e.getResponseBodyAsString()),
                    "HTTP_" + statusCode,
                    e.getMessage()
            );

        } catch (ResourceAccessException e) {
            // Сюда попадают таймауты и сетевые ошибки
            String message = e.getMessage() != null ? e.getMessage() : "Resource access error";
            if (message.toLowerCase().contains("timeout") ||
                    message.toLowerCase().contains("timed out")) {
                log.warn("[executor] HTTP timeout attemptUuid={}: {}", attemptUuid, message);
                return HttpTaskResult.timeout(message);
            }
            log.warn("[executor] HTTP network error attemptUuid={}: {}", attemptUuid, message);
            return HttpTaskResult.networkError(message);

        } catch (Exception e) {
            log.error("[executor] unexpected error during HTTP call attemptUuid={}", attemptUuid, e);
            return HttpTaskResult.networkError(e.getMessage());
        }
    }

    // ── Private ───────────────────────────────────────────────────────────────

    /**
     * Конвертирует тело ответа в строку.
     * Для текстовых типов (text/*, application/json, application/xml) — как есть.
     * Для бинарных (image/*, application/octet-stream) — Base64.
     */
    private String bodyToText(MediaType contentType, byte[] body) {
        if (body == null || body.length == 0) return "";
        if (contentType != null && (
                contentType.getType().equals("text") ||
                contentType.includes(MediaType.APPLICATION_JSON) ||
                contentType.includes(MediaType.APPLICATION_XML) ||
                contentType.getSubtype().contains("json") ||
                contentType.getSubtype().contains("xml"))) {
            return new String(body, java.nio.charset.StandardCharsets.UTF_8);
        }
        // Binary or unknown: store as Base64
        return "[base64]" + Base64.getEncoder().encodeToString(body);
    }

    private boolean isBinaryContent(MediaType contentType) {
        if (contentType == null) return false;
        String type = contentType.getType();
        return type.equals("image") || type.equals("audio") || type.equals("video")
                || contentType.includes(MediaType.APPLICATION_OCTET_STREAM);
    }

    /** Обрезаем тело ответа — не храним мегабайты в БД. */
    private String truncate(String body) {
        if (body == null) return null;
        int maxLen = 4096;
        return body.length() > maxLen ? body.substring(0, maxLen) + "...[truncated]" : body;
    }
}