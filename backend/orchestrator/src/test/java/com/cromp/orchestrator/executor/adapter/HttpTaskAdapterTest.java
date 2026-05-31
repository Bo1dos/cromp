package com.cromp.orchestrator.executor.adapter;

import com.cromp.jobs.domain.model.JobTarget;
import com.cromp.orchestrator.config.OrchestratorProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("HttpTaskAdapter")
@ExtendWith(MockitoExtension.class)
class HttpTaskAdapterTest {

    @Mock
    private RestClient restClient;
    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private RestClient.RequestBodySpec requestBodySpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    private OrchestratorProperties properties;
    private HttpTaskAdapter adapter;

    private static final UUID ATTEMPT_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        properties = new OrchestratorProperties();
        properties.setDryRun(false);
        adapter = new HttpTaskAdapter(restClient, properties);
    }

    @Nested
    @DisplayName("dry-run mode")
    class DryRun {

        @Test
        @DisplayName("should return synthetic success when dryRun=true")
        void shouldReturnSyntheticSuccess() {
            properties.setDryRun(true);
            JobTarget target = JobTarget.forHttp("http://example.com/api", "POST",
                    Map.of(), "{}");

            HttpTaskResult result = adapter.execute(ATTEMPT_UUID, target, Map.of());

            assertThat(result.success()).isTrue();
            assertThat(result.statusCode()).isEqualTo(200);
            assertThat(result.body()).contains("dry-run");
        }

        @Test
        @DisplayName("should not make real HTTP call when dryRun=true")
        void shouldNotCallHttpWhenDryRun() {
            properties.setDryRun(true);
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of(), null);

            adapter.execute(ATTEMPT_UUID, target, Map.of());

            // RestClient should not be invoked
            verify(restClient, org.mockito.Mockito.never()).method(any());
        }
    }

    @Nested
    @DisplayName("request building")
    class RequestBuilding {

        @BeforeEach
        void setupMocks() {
            when(restClient.method(any(HttpMethod.class))).thenReturn(requestBodyUriSpec);
            lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
            lenient().when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            lenient().when(requestBodySpec.body(anyString())).thenReturn(requestBodySpec);
            lenient().when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(String.class)).thenReturn(
                    org.springframework.http.ResponseEntity.ok("{\"ok\":true}"));
        }

        @Test
        @DisplayName("should set HTTP method and URI from target")
        void shouldSetMethodAndUri() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "POST",
                    Map.of(), null);

            adapter.execute(ATTEMPT_UUID, target, Map.of());

            verify(restClient).method(HttpMethod.POST);
            verify(requestBodyUriSpec).uri("http://example.com/api");
        }

        @Test
        @DisplayName("should add X-Execution-Id header")
        void shouldAddExecutionIdHeader() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of(), null);

            adapter.execute(ATTEMPT_UUID, target, Map.of());

            verify(requestBodySpec).header("X-Execution-Id", ATTEMPT_UUID.toString());
        }

        @Test
        @DisplayName("should add target headers to request")
        void shouldAddTargetHeaders() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of("Authorization", "Bearer token", "X-Custom", "value"), null);

            adapter.execute(ATTEMPT_UUID, target, Map.of());

            verify(requestBodySpec).header("Authorization", "Bearer token");
            verify(requestBodySpec).header("X-Custom", "value");
        }

        @Test
        @DisplayName("should add resolved secrets as headers")
        void shouldAddResolvedSecretsAsHeaders() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of(), null);
            Map<String, String> secrets = Map.of("API_KEY", "secret123", "TOKEN", "abc");

            adapter.execute(ATTEMPT_UUID, target, secrets);

            verify(requestBodySpec).header("API_KEY", "secret123");
            verify(requestBodySpec).header("TOKEN", "abc");
        }

        @Test
        @DisplayName("should not add body when null or blank")
        void shouldNotAddBodyWhenBlank() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of(), null);

            adapter.execute(ATTEMPT_UUID, target, Map.of());

            verify(requestBodySpec, org.mockito.Mockito.never()).body(anyString());
        }

        @Test
        @DisplayName("should add body and Content-Type when body present")
        void shouldAddBodyAndContentType() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "POST",
                    Map.of(), "{\"key\":\"value\"}");

            adapter.execute(ATTEMPT_UUID, target, Map.of());

            verify(requestBodySpec).header("Content-Type", "application/json");
            verify(requestBodySpec).body("{\"key\":\"value\"}");
        }
    }

    @Nested
    @DisplayName("response handling")
    class ResponseHandling {

        @Test
        @DisplayName("should return success for 2xx response")
        void shouldReturnSuccessFor2xx() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of(), null);
            mockRestCall();
            when(responseSpec.toEntity(String.class)).thenReturn(
                    org.springframework.http.ResponseEntity.ok("{\"ok\":true}"));

            HttpTaskResult result = adapter.execute(ATTEMPT_UUID, target, Map.of());

            assertThat(result.success()).isTrue();
            assertThat(result.statusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("should return failure for 4xx response")
        void shouldReturnFailureFor4xx() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of(), null);
            mockRestCall();
            when(responseSpec.toEntity(String.class)).thenReturn(
                    org.springframework.http.ResponseEntity.status(404).body("not found"));

            HttpTaskResult result = adapter.execute(ATTEMPT_UUID, target, Map.of());

            assertThat(result.success()).isFalse();
            assertThat(result.errorClass()).isEqualTo("HTTP_404");
        }

        @Test
        @DisplayName("should return failure for 5xx response")
        void shouldReturnFailureFor5xx() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of(), null);
            mockRestCall();
            when(responseSpec.toEntity(String.class)).thenReturn(
                    org.springframework.http.ResponseEntity.status(500).body("error"));

            HttpTaskResult result = adapter.execute(ATTEMPT_UUID, target, Map.of());

            assertThat(result.success()).isFalse();
            assertThat(result.errorClass()).isEqualTo("HTTP_500");
        }
    }

    @Nested
    @DisplayName("error classification")
    class ErrorClassification {

        @Test
        @DisplayName("should classify HttpStatusCodeException as failure")
        void shouldClassifyHttpStatusCodeException() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of(), null);
            mockRestCall();
            // Simulate HttpStatusCodeException via retrieve()
            when(responseSpec.toEntity(String.class))
                    .thenThrow(new HttpStatusCodeException(HttpStatusCode.valueOf(502), "Bad Gateway") {});

            HttpTaskResult result = adapter.execute(ATTEMPT_UUID, target, Map.of());

            assertThat(result.success()).isFalse();
            assertThat(result.errorClass()).isEqualTo("HTTP_502");
        }

        @Test
        @DisplayName("should classify ResourceAccessException with timeout message as timeout")
        void shouldClassifyTimeoutError() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of(), null);
            mockRestCall();
            when(responseSpec.toEntity(String.class))
                    .thenThrow(new ResourceAccessException("Read timed out"));

            HttpTaskResult result = adapter.execute(ATTEMPT_UUID, target, Map.of());

            assertThat(result.success()).isFalse();
            assertThat(result.errorClass()).isEqualTo("TIMEOUT");
        }

        @Test
        @DisplayName("should classify ResourceAccessException without timeout as network error")
        void shouldClassifyNetworkError() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of(), null);
            mockRestCall();
            when(responseSpec.toEntity(String.class))
                    .thenThrow(new ResourceAccessException("Connection refused"));

            HttpTaskResult result = adapter.execute(ATTEMPT_UUID, target, Map.of());

            assertThat(result.success()).isFalse();
            assertThat(result.errorClass()).isEqualTo("NETWORK_ERROR");
        }

        @Test
        @DisplayName("should classify unexpected exception as network error")
        void shouldClassifyUnexpectedException() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of(), null);
            mockRestCall();
            when(responseSpec.toEntity(String.class))
                    .thenThrow(new RuntimeException("Something unexpected"));

            HttpTaskResult result = adapter.execute(ATTEMPT_UUID, target, Map.of());

            assertThat(result.success()).isFalse();
            assertThat(result.errorClass()).isEqualTo("NETWORK_ERROR");
        }

        @Test
        @DisplayName("should classify ResourceAccessException with 'timed out' message")
        void shouldClassifyTimedOutMessage() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of(), null);
            mockRestCall();
            when(responseSpec.toEntity(String.class))
                    .thenThrow(new ResourceAccessException("Connection timed out"));

            HttpTaskResult result = adapter.execute(ATTEMPT_UUID, target, Map.of());

            assertThat(result.errorClass()).isEqualTo("TIMEOUT");
        }
    }

    @Nested
    @DisplayName("response truncation")
    class Truncation {

        @Test
        @DisplayName("should truncate long response body to 4096 chars")
        void shouldTruncateLongBody() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of(), null);
            mockRestCall();
            String longBody = "x".repeat(5000);
            when(responseSpec.toEntity(String.class)).thenReturn(
                    org.springframework.http.ResponseEntity.ok(longBody));

            HttpTaskResult result = adapter.execute(ATTEMPT_UUID, target, Map.of());

            assertThat(result.body()).hasSize(4096 + "...[truncated]".length());
            assertThat(result.body()).endsWith("...[truncated]");
        }

        @Test
        @DisplayName("should not truncate short body")
        void shouldNotTruncateShortBody() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of(), null);
            mockRestCall();
            when(responseSpec.toEntity(String.class)).thenReturn(
                    org.springframework.http.ResponseEntity.ok("short"));

            HttpTaskResult result = adapter.execute(ATTEMPT_UUID, target, Map.of());

            assertThat(result.body()).isEqualTo("short");
        }

        @Test
        @DisplayName("should handle null body")
        void shouldHandleNullBody() {
            JobTarget target = JobTarget.forHttp("http://example.com/api", "GET",
                    Map.of(), null);
            mockRestCall();
            when(responseSpec.toEntity(String.class)).thenReturn(
                    org.springframework.http.ResponseEntity.ok(null));

            HttpTaskResult result = adapter.execute(ATTEMPT_UUID, target, Map.of());

            assertThat(result.success()).isTrue();
            assertThat(result.body()).isNull();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void mockRestCall() {
        when(restClient.method(any(HttpMethod.class))).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.body(anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }
}
