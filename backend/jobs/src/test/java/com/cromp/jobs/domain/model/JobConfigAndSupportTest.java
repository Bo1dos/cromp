package com.cromp.jobs.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobConfigAndSupportTest {

    @Test
    void shouldCreateHttpTargetWithTrimmedBodyAndImmutableHeaders() {
        JobTarget target = JobTarget.forHttp("https://example.com", "POST", Map.of("X-Trace", "1"), "  body  ");

        assertThat(target.type()).isEqualTo("HTTP");
        assertThat(target.body()).isEqualTo("body");
        assertThat(target.headers()).containsEntry("X-Trace", "1");
        assertThatThrownBy(() -> target.headers().put("X-New", "2"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowWhenTargetFieldsAreInvalid() {
        assertThatThrownBy(() -> new JobTarget("", "url", "POST", Map.of(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldApplyDefaultRetryPolicyAndCopySecrets() {
        JobConfig config = new JobConfig(
                JobTarget.forHttp("https://example.com", "POST", Map.of(), null),
                null,
                1_000,
                List.of(new JobSecretRef(UUID.randomUUID(), "API_KEY"))
        );

        assertThat(config.retryPolicy()).isEqualTo(RetryPolicy.defaultPolicy());
        assertThat(config.secrets()).hasSize(1);
        assertThatThrownBy(() -> config.secrets().add(new JobSecretRef(UUID.randomUUID(), "OTHER")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowWhenTimeoutIsNegative() {
        assertThatThrownBy(() -> new JobConfig(JobTarget.forHttp("https://example.com", "POST", Map.of(), null), null, -1, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("timeoutMs must be >= 0");
    }

    @Test
    void shouldThrowWhenRetryPolicyIsInvalid() {
        assertThatThrownBy(() -> new RetryPolicy(0, 1, 1.0, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RetryPolicy(1, -1, 1.0, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RetryPolicy(1, 1, 0.5, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowWhenSecretReferenceIsInvalid() {
        assertThatThrownBy(() -> new JobSecretRef(null, "API_KEY"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JobSecretRef(UUID.randomUUID(), "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldUseDefaultRetryPolicyValues() {
        RetryPolicy policy = RetryPolicy.defaultPolicy();

        assertThat(policy.maxAttempts()).isEqualTo(3);
        assertThat(policy.backoffMs()).isEqualTo(1000);
        assertThat(policy.backoffMultiplier()).isEqualTo(2.0);
    }
}
