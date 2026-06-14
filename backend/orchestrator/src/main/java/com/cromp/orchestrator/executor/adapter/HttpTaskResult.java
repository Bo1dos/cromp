package com.cromp.orchestrator.executor.adapter;

/**
 * Результат выполнения HTTP-задачи.
 *
 * Передаётся из {@link HttpTaskAdapter} в {@link com.cromp.orchestrator.executor.ExecutorService}
 * для последующей фиксации через {@code AttemptCompletionPort}.
 */
public record HttpTaskResult(
        boolean success,
        int statusCode,
        String body,
        String errorClass,
        String errorMessage,
        byte[] rawBody,
        String contentType
) {

    /** Успешный результат (2xx). */
    public static HttpTaskResult success(int statusCode, String body) {
        return new HttpTaskResult(true, statusCode, body, null, null, null, null);
    }

    /** Успешный результат с бинарным телом. */
    public static HttpTaskResult success(int statusCode, String bodyPreview,
                                         byte[] rawBody, String contentType) {
        return new HttpTaskResult(true, statusCode, bodyPreview, null, null, rawBody, contentType);
    }

    /** Ошибка HTTP (4xx, 5xx) или сетевая ошибка. */
    public static HttpTaskResult failure(int statusCode, String body,
                                         String errorClass, String errorMessage) {
        return new HttpTaskResult(false, statusCode, body, errorClass, errorMessage, null, null);
    }

    /** Таймаут запроса. */
    public static HttpTaskResult timeout(String errorMessage) {
        return new HttpTaskResult(false, 0, null, "TIMEOUT", errorMessage, null, null);
    }

    /** Сетевая / иная ошибка без HTTP-статуса. */
    public static HttpTaskResult networkError(String errorMessage) {
        return new HttpTaskResult(false, 0, null, "NETWORK_ERROR", errorMessage, null, null);
    }

    /**
     * Классифицирует тип ошибки для retry policy.
     * Соответствует значениям в {@code RetryPolicy.retryableErrors}.
     */
    public String errorType() {
        if (success) return null;
        if ("TIMEOUT".equals(errorClass)) return "TIMEOUT";
        if ("NETWORK_ERROR".equals(errorClass)) return "NETWORK_ERROR";
        if (statusCode >= 500) return "5xx";
        if (statusCode >= 400) return "4xx";
        return "UNKNOWN";
    }
}