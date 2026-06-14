package com.cromp.orchestrator.executor;

import com.cromp.common.event.integration.publisher.DomainEventPublisher;
import com.cromp.executions.api.dto.request.CompleteAttemptRequest;
import com.cromp.executions.api.dto.response.ClaimAttemptResult;
import com.cromp.executions.application.port.AttemptClaimPort;
import com.cromp.executions.application.port.AttemptCompletionPort;
import com.cromp.executions.domain.model.enums.AttemptStatus;
import com.cromp.jobs.domain.model.JobConfig;
import com.cromp.jobs.domain.model.JobSecretRef;
import com.cromp.jobs.domain.model.JobTarget;
import com.cromp.jobs.domain.model.RetryPolicy;
import com.cromp.orchestrator.config.OrchestratorProperties;
import com.cromp.orchestrator.executor.adapter.HttpTaskAdapter;
import com.cromp.orchestrator.executor.adapter.HttpTaskResult;
import com.cromp.orchestrator.executor.secret.SecretResolverService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ExecutorService")
@ExtendWith(MockitoExtension.class)
class ExecutorServiceTest {

    @Mock
    private AttemptClaimPort claimPort;
    @Mock
    private AttemptCompletionPort completionPort;
    @Mock
    private HttpTaskAdapter httpTaskAdapter;
    @Mock
    private SecretResolverService secretResolverService;
    @Mock
    private DomainEventPublisher domainEventPublisher;

    private OrchestratorProperties properties;
    private ObjectMapper objectMapper;
    private MeterRegistry meterRegistry;
    private Executor taskExecutor;

    private ExecutorService service;

    private static final UUID ATTEMPT_UUID = UUID.randomUUID();
    private static final Long ORG_ID = 42L;
    private static final Long JOB_ID = 99L;

    @BeforeEach
    void setUp() {
        properties = new OrchestratorProperties();
        objectMapper = new ObjectMapper();
        meterRegistry = new SimpleMeterRegistry();
        taskExecutor = Runnable::run;
        service = new ExecutorService(
                claimPort, completionPort, httpTaskAdapter,
                secretResolverService, properties, objectMapper,
                taskExecutor, meterRegistry, domainEventPublisher);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ClaimAttemptResult createClaimResult(String jobConfigJson) {
        return new ClaimAttemptResult(1L, ATTEMPT_UUID, 100L, ORG_ID, JOB_ID, jobConfigJson);
    }

    private String jobConfigJson() throws JsonProcessingException {
        JobConfig config = new JobConfig(
                JobTarget.forHttp("http://test.local/api", "POST",
                        Map.of("X-Custom", "val"), "{\"data\":1}"),
                new RetryPolicy(3, 1000, 2.0, List.of("5xx", "TIMEOUT")),
                30000,
                List.of(new JobSecretRef(UUID.randomUUID(), "API_KEY"))
        );
        return objectMapper.writeValueAsString(config);
    }

    @Nested
    @DisplayName("run()")
    class Run {

        @Test
        @DisplayName("should claim up to batchSize times")
        void shouldClaimUpToBatchSize() {
            when(claimPort.claimNextAttempt(any())).thenReturn(Optional.empty());

            service.run();

            verify(claimPort, atLeast(properties.getExecutor().getBatchSize()))
                    .claimNextAttempt(any());
        }

        @Test
        @DisplayName("should stop early when claim returns empty")
        void shouldStopEarlyOnEmptyClaim() {
            // First claim succeeds, second returns empty
            when(claimPort.claimNextAttempt(any()))
                    .thenReturn(Optional.of(createClaimResult("{}")))
                    .thenReturn(Optional.empty());

            service.run();

            // Should only claim 2 times (one success, one empty)
            verify(claimPort, timeout(1000).times(2)).claimNextAttempt(any());
        }

        @Test
        @DisplayName("should increment startedCounter for each claimed attempt")
        void shouldIncrementStartedCounter() throws Exception {
            String json = jobConfigJson();
            when(claimPort.claimNextAttempt(any()))
                    .thenReturn(Optional.of(createClaimResult(json)))
                    .thenReturn(Optional.empty());
            when(httpTaskAdapter.execute(any(), any(), anyMap()))
                    .thenReturn(HttpTaskResult.success(200, "ok"));

            service.run();

            double count = meterRegistry.get("orchestrator.executor.attempts.started")
                    .counter().count();
            assertThat(count).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should not create futures when no attempts claimed")
        void shouldNotCreateFuturesWhenEmpty() {
            when(claimPort.claimNextAttempt(any())).thenReturn(Optional.empty());

            service.run();

            verify(httpTaskAdapter, never()).execute(any(), any(), anyMap());
        }
    }

    @Nested
    @DisplayName("executeAttempt() — success path")
    class ExecuteAttemptSuccess {

        @Test
        @DisplayName("should parse jobConfig and execute HTTP task")
        void shouldParseAndExecute() throws Exception {
            String json = jobConfigJson();
            ClaimAttemptResult claimed = createClaimResult(json);
            when(claimPort.claimNextAttempt(any()))
                    .thenReturn(Optional.of(claimed), Optional.empty());
            when(httpTaskAdapter.execute(any(), any(), anyMap()))
                    .thenReturn(HttpTaskResult.success(200, "{\"result\":\"ok\"}"));
            when(secretResolverService.resolve(any(), anyList()))
                    .thenReturn(Map.of("API_KEY", "secret-value"));

            service.run();

            verify(httpTaskAdapter).execute(eq(ATTEMPT_UUID), any(), anyMap());
        }

        @Test
        @DisplayName("should resolve secrets before HTTP call")
        void shouldResolveSecrets() throws Exception {
            String json = jobConfigJson();
            ClaimAttemptResult claimed = createClaimResult(json);
            when(claimPort.claimNextAttempt(any()))
                    .thenReturn(Optional.of(claimed), Optional.empty());
            when(httpTaskAdapter.execute(any(), any(), anyMap()))
                    .thenReturn(HttpTaskResult.success(200, "ok"));
            when(secretResolverService.resolve(any(), anyList()))
                    .thenReturn(Map.of("API_KEY", "val"));

            service.run();

            verify(secretResolverService).resolve(any(), anyList());
        }

        @Test
        @DisplayName("should complete attempt with SUCCEEDED on success")
        void shouldCompleteWithSucceeded() throws Exception {
            String json = jobConfigJson();
            ClaimAttemptResult claimed = createClaimResult(json);
            when(claimPort.claimNextAttempt(any()))
                    .thenReturn(Optional.of(claimed), Optional.empty());
            when(httpTaskAdapter.execute(any(), any(), anyMap()))
                    .thenReturn(HttpTaskResult.success(200, "ok"));

            service.run();

            ArgumentCaptor<CompleteAttemptRequest> captor =
                    ArgumentCaptor.forClass(CompleteAttemptRequest.class);
            verify(completionPort, timeout(2000)).completeAttempt(
                    eq(ATTEMPT_UUID), captor.capture());
            assertThat(captor.getValue().status()).isEqualTo(AttemptStatus.SUCCEEDED);
        }

        @Test
        @DisplayName("should build output summary JSON on success")
        void shouldBuildOutputSummary() throws Exception {
            String json = jobConfigJson();
            ClaimAttemptResult claimed = createClaimResult(json);
            when(claimPort.claimNextAttempt(any()))
                    .thenReturn(Optional.of(claimed), Optional.empty());
            when(httpTaskAdapter.execute(any(), any(), anyMap()))
                    .thenReturn(HttpTaskResult.success(200, "{\"key\":\"val\"}"));

            service.run();

            ArgumentCaptor<CompleteAttemptRequest> captor =
                    ArgumentCaptor.forClass(CompleteAttemptRequest.class);
            verify(completionPort, timeout(2000)).completeAttempt(
                    eq(ATTEMPT_UUID), captor.capture());
            assertThat(captor.getValue().outputSummary()).contains("200").contains("key");
        }

        @Test
        @DisplayName("should increment successCounter on success")
        void shouldIncrementSuccessCounter() throws Exception {
            String json = jobConfigJson();
            ClaimAttemptResult claimed = createClaimResult(json);
            when(claimPort.claimNextAttempt(any()))
                    .thenReturn(Optional.of(claimed), Optional.empty());
            when(httpTaskAdapter.execute(any(), any(), anyMap()))
                    .thenReturn(HttpTaskResult.success(200, "ok"));

            service.run();

            double count = meterRegistry.get("orchestrator.executor.attempts.success")
                    .counter().count();
            assertThat(count).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("executeAttempt() — failure path")
    class ExecuteAttemptFailure {

        @Test
        @DisplayName("should fail attempt when jobConfig is unparseable")
        void shouldFailOnBadJobConfig() {
            ClaimAttemptResult claimed = createClaimResult("not-valid-json{{{");
            when(claimPort.claimNextAttempt(any()))
                    .thenReturn(Optional.of(claimed), Optional.empty());

            service.run();

            ArgumentCaptor<CompleteAttemptRequest> captor =
                    ArgumentCaptor.forClass(CompleteAttemptRequest.class);
            verify(completionPort, timeout(2000)).completeAttempt(
                    eq(ATTEMPT_UUID), captor.capture());
            assertThat(captor.getValue().status()).isEqualTo(AttemptStatus.FAILED);
            assertThat(captor.getValue().errorClass()).isEqualTo("CONFIG_PARSE_ERROR");
        }

        @Test
        @DisplayName("should complete with FAILED on non-retryable error")
        void shouldCompleteWithFailed() throws Exception {
            String json = jobConfigJson();
            ClaimAttemptResult claimed = createClaimResult(json);
            when(claimPort.claimNextAttempt(any()))
                    .thenReturn(Optional.of(claimed), Optional.empty());
            when(httpTaskAdapter.execute(any(), any(), anyMap()))
                    .thenReturn(HttpTaskResult.failure(400, "bad request",
                            "HTTP_400", "Bad Request"));

            service.run();

            ArgumentCaptor<CompleteAttemptRequest> captor =
                    ArgumentCaptor.forClass(CompleteAttemptRequest.class);
            verify(completionPort, timeout(2000)).completeAttempt(
                    eq(ATTEMPT_UUID), captor.capture());
            assertThat(captor.getValue().status()).isEqualTo(AttemptStatus.FAILED);
        }

        @Test
        @DisplayName("should complete with TIMEOUT for timeout errors")
        void shouldCompleteWithTimeout() throws Exception {
            String json = jobConfigJson();
            ClaimAttemptResult claimed = createClaimResult(json);
            when(claimPort.claimNextAttempt(any()))
                    .thenReturn(Optional.of(claimed), Optional.empty());
            when(httpTaskAdapter.execute(any(), any(), anyMap()))
                    .thenReturn(HttpTaskResult.timeout("Read timed out"));

            service.run();

            ArgumentCaptor<CompleteAttemptRequest> captor =
                    ArgumentCaptor.forClass(CompleteAttemptRequest.class);
            verify(completionPort, timeout(2000)).completeAttempt(
                    eq(ATTEMPT_UUID), captor.capture());
            assertThat(captor.getValue().status()).isEqualTo(AttemptStatus.TIMEOUT);
        }

        @Test
        @DisplayName("should increment failedCounter on failure")
        void shouldIncrementFailedCounter() throws Exception {
            String json = jobConfigJson();
            ClaimAttemptResult claimed = createClaimResult(json);
            when(claimPort.claimNextAttempt(any()))
                    .thenReturn(Optional.of(claimed), Optional.empty());
            when(httpTaskAdapter.execute(any(), any(), anyMap()))
                    .thenReturn(HttpTaskResult.failure(500, "error",
                            "HTTP_500", "Server Error"));

            service.run();

            double count = meterRegistry.get("orchestrator.executor.attempts.failed")
                    .counter().count();
            assertThat(count).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should handle unhandled exception in worker")
        void shouldHandleUnhandledException() throws Exception {
            String json = jobConfigJson();
            ClaimAttemptResult claimed = createClaimResult(json);
            when(claimPort.claimNextAttempt(any()))
                    .thenReturn(Optional.of(claimed), Optional.empty());
            when(httpTaskAdapter.execute(any(), any(), anyMap()))
                    .thenThrow(new RuntimeException("Unexpected crash"));

            service.run();

            ArgumentCaptor<CompleteAttemptRequest> captor =
                    ArgumentCaptor.forClass(CompleteAttemptRequest.class);
            verify(completionPort, timeout(2000)).completeAttempt(
                    eq(ATTEMPT_UUID), captor.capture());
            assertThat(captor.getValue().status()).isEqualTo(AttemptStatus.FAILED);
            assertThat(captor.getValue().errorClass()).isEqualTo("INTERNAL_ERROR");
        }
    }

    @Nested
    @DisplayName("completeAttempt()")
    class CompleteAttempt {

        @Test
        @DisplayName("should build CompleteAttemptRequest with correct fields")
        void shouldBuildCorrectRequest() throws Exception {
            String json = jobConfigJson();
            ClaimAttemptResult claimed = createClaimResult(json);
            when(claimPort.claimNextAttempt(any()))
                    .thenReturn(Optional.of(claimed), Optional.empty());
            HttpTaskResult result = HttpTaskResult.failure(503, "unavailable",
                    "HTTP_503", "Service Unavailable");
            when(httpTaskAdapter.execute(any(), any(), anyMap()))
                    .thenReturn(result);

            service.run();

            ArgumentCaptor<CompleteAttemptRequest> captor =
                    ArgumentCaptor.forClass(CompleteAttemptRequest.class);
            verify(completionPort, timeout(2000)).completeAttempt(
                    eq(ATTEMPT_UUID), captor.capture());

            CompleteAttemptRequest req = captor.getValue();
            assertThat(req.status()).isEqualTo(AttemptStatus.FAILED);
            assertThat(req.errorClass()).isEqualTo("HTTP_503");
            assertThat(req.statusReason()).isEqualTo("Service Unavailable");
            assertThat(req.outputSummary()).isNotNull();
        }

        @Test
        @DisplayName("should survive completionPort exception")
        void shouldSurviveCompletionException() throws Exception {
            String json = jobConfigJson();
            ClaimAttemptResult claimed = createClaimResult(json);
            when(claimPort.claimNextAttempt(any()))
                    .thenReturn(Optional.of(claimed), Optional.empty());
            when(httpTaskAdapter.execute(any(), any(), anyMap()))
                    .thenReturn(HttpTaskResult.success(200, "ok"));
            doThrow(new RuntimeException("Completion failed"))
                    .when(completionPort).completeAttempt(any(), any());

            // Should not throw — just log
            service.run();

            verify(completionPort).completeAttempt(any(), any());
        }
    }

    @Nested
    @DisplayName("failAttempt()")
    class FailAttempt {

        @Test
        @DisplayName("should complete with FAILED status and error info")
        void shouldCompleteWithFailed() {
            ClaimAttemptResult claimed = createClaimResult("{}");
            when(claimPort.claimNextAttempt(any()))
                    .thenReturn(Optional.of(claimed), Optional.empty());

            service.run();

            ArgumentCaptor<CompleteAttemptRequest> captor =
                    ArgumentCaptor.forClass(CompleteAttemptRequest.class);
            verify(completionPort, timeout(2000)).completeAttempt(
                    eq(ATTEMPT_UUID), captor.capture());
            assertThat(captor.getValue().status()).isEqualTo(AttemptStatus.FAILED);
        }
    }
}
