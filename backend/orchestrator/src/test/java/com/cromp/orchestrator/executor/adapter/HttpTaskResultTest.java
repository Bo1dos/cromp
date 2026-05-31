package com.cromp.orchestrator.executor.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HttpTaskResult")
class HttpTaskResultTest {

    @Nested
    @DisplayName("success()")
    class Success {

        @Test
        @DisplayName("should create successful result with status code and body")
        void shouldCreateSuccessResult() {
            HttpTaskResult result = HttpTaskResult.success(200, "{\"ok\":true}");

            assertThat(result.success()).isTrue();
            assertThat(result.statusCode()).isEqualTo(200);
            assertThat(result.body()).isEqualTo("{\"ok\":true}");
            assertThat(result.errorClass()).isNull();
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("should return null errorType for success")
        void shouldReturnNullErrorType() {
            HttpTaskResult result = HttpTaskResult.success(201, "created");

            assertThat(result.errorType()).isNull();
        }
    }

    @Nested
    @DisplayName("failure()")
    class Failure {

        @Test
        @DisplayName("should create failure result with status, body and error info")
        void shouldCreateFailureResult() {
            HttpTaskResult result = HttpTaskResult.failure(500, "boom", "HTTP_500", "Server error");

            assertThat(result.success()).isFalse();
            assertThat(result.statusCode()).isEqualTo(500);
            assertThat(result.body()).isEqualTo("boom");
            assertThat(result.errorClass()).isEqualTo("HTTP_500");
            assertThat(result.errorMessage()).isEqualTo("Server error");
        }

        @Test
        @DisplayName("should return 5xx for status >= 500")
        void shouldReturn5xxErrorType() {
            HttpTaskResult result = HttpTaskResult.failure(502, "bad gateway", "HTTP_502", "err");

            assertThat(result.errorType()).isEqualTo("5xx");
        }

        @Test
        @DisplayName("should return 4xx for status >= 400 and < 500")
        void shouldReturn4xxErrorType() {
            HttpTaskResult result = HttpTaskResult.failure(404, "not found", "HTTP_404", "err");

            assertThat(result.errorType()).isEqualTo("4xx");
        }

        @Test
        @DisplayName("should return UNKNOWN for status < 400 with errorClass")
        void shouldReturnUnknownErrorType() {
            HttpTaskResult result = HttpTaskResult.failure(0, null, "CUSTOM", "custom error");

            assertThat(result.errorType()).isEqualTo("UNKNOWN");
        }
    }

    @Nested
    @DisplayName("timeout()")
    class Timeout {

        @Test
        @DisplayName("should create timeout result with TIMEOUT error class")
        void shouldCreateTimeoutResult() {
            HttpTaskResult result = HttpTaskResult.timeout("Read timed out");

            assertThat(result.success()).isFalse();
            assertThat(result.statusCode()).isEqualTo(0);
            assertThat(result.body()).isNull();
            assertThat(result.errorClass()).isEqualTo("TIMEOUT");
            assertThat(result.errorMessage()).isEqualTo("Read timed out");
        }

        @Test
        @DisplayName("should return TIMEOUT errorType")
        void shouldReturnTimeoutErrorType() {
            HttpTaskResult result = HttpTaskResult.timeout("Connection timed out");

            assertThat(result.errorType()).isEqualTo("TIMEOUT");
        }
    }

    @Nested
    @DisplayName("networkError()")
    class NetworkError {

        @Test
        @DisplayName("should create network error result with NETWORK_ERROR class")
        void shouldCreateNetworkErrorResult() {
            HttpTaskResult result = HttpTaskResult.networkError("Connection refused");

            assertThat(result.success()).isFalse();
            assertThat(result.statusCode()).isEqualTo(0);
            assertThat(result.body()).isNull();
            assertThat(result.errorClass()).isEqualTo("NETWORK_ERROR");
            assertThat(result.errorMessage()).isEqualTo("Connection refused");
        }

        @Test
        @DisplayName("should return NETWORK_ERROR errorType")
        void shouldReturnNetworkErrorErrorType() {
            HttpTaskResult result = HttpTaskResult.networkError("Unknown host");

            assertThat(result.errorType()).isEqualTo("NETWORK_ERROR");
        }
    }
}
