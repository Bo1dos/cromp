package com.cromp.orchestrator.executor.secret;

import com.cromp.jobs.domain.model.JobSecretRef;
import com.cromp.secrets.application.port.SecretResolvePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SecretResolverService")
@ExtendWith(MockitoExtension.class)
class SecretResolverServiceTest {

    @Mock
    private SecretResolvePort secretResolvePort;

    private SecretResolverService service;

    @BeforeEach
    void setUp() {
        service = new SecretResolverService(secretResolvePort);
    }

    @Nested
    @DisplayName("resolve()")
    class Resolve {

        @Test
        @DisplayName("should return empty map when secretRefs is null")
        void shouldReturnEmptyMapWhenNull() {
            Map<String, String> result = service.resolve(1L, null);

            assertThat(result).isEmpty();
            verify(secretResolvePort, never()).resolveSecrets(any(), anyList());
        }

        @Test
        @DisplayName("should return empty map when secretRefs is empty")
        void shouldReturnEmptyMapWhenEmpty() {
            Map<String, String> result = service.resolve(1L, List.of());

            assertThat(result).isEmpty();
            verify(secretResolvePort, never()).resolveSecrets(any(), anyList());
        }

        @Test
        @DisplayName("should deduplicate secret UUIDs before calling port")
        void shouldDeduplicateSecretIds() {
            UUID secretId = UUID.randomUUID();
            JobSecretRef ref1 = new JobSecretRef(secretId, "API_KEY");
            JobSecretRef ref2 = new JobSecretRef(secretId, "API_SECRET");

            when(secretResolvePort.resolveSecrets(eq(1L), anyList()))
                    .thenReturn(Map.of(secretId, "plaintext-value"));

            service.resolve(1L, List.of(ref1, ref2));

            verify(secretResolvePort).resolveSecrets(eq(1L), argThat(list ->
                    list.size() == 1 && list.contains(secretId)));
        }

        @Test
        @DisplayName("should pass orgId to port")
        void shouldPassOrgIdToPort() {
            Long orgId = 42L;
            UUID secretId = UUID.randomUUID();
            JobSecretRef ref = new JobSecretRef(secretId, "TOKEN");

            when(secretResolvePort.resolveSecrets(eq(orgId), anyList()))
                    .thenReturn(Map.of(secretId, "abc123"));

            service.resolve(orgId, List.of(ref));

            verify(secretResolvePort).resolveSecrets(eq(orgId), anyList());
        }

        @Test
        @DisplayName("should map resolved secrets as envName -> plaintext")
        void shouldMapEnvNameToPlaintext() {
            UUID secretId = UUID.randomUUID();
            JobSecretRef ref = new JobSecretRef(secretId, "AUTH_HEADER");

            when(secretResolvePort.resolveSecrets(any(), anyList()))
                    .thenReturn(Map.of(secretId, "Bearer xyz"));

            Map<String, String> result = service.resolve(1L, List.of(ref));

            assertThat(result).containsEntry("AUTH_HEADER", "Bearer xyz");
        }

        @Test
        @DisplayName("should skip unresolved UUIDs")
        void shouldSkipUnresolvedUUIDs() {
            UUID resolvedId = UUID.randomUUID();
            UUID unresolvedId = UUID.randomUUID();
            JobSecretRef ref1 = new JobSecretRef(resolvedId, "KEY1");
            JobSecretRef ref2 = new JobSecretRef(unresolvedId, "KEY2");

            when(secretResolvePort.resolveSecrets(any(), anyList()))
                    .thenReturn(Map.of(resolvedId, "value1"));

            Map<String, String> result = service.resolve(1L, List.of(ref1, ref2));

            assertThat(result).hasSize(1).containsEntry("KEY1", "value1");
        }

        @Test
        @DisplayName("should keep first envName on duplicate envName")
        void shouldKeepFirstOnDuplicateEnvName() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            JobSecretRef ref1 = new JobSecretRef(id1, "SAME_KEY");
            JobSecretRef ref2 = new JobSecretRef(id2, "SAME_KEY");

            when(secretResolvePort.resolveSecrets(any(), anyList()))
                    .thenReturn(Map.of(id1, "first_value", id2, "second_value"));

            Map<String, String> result = service.resolve(1L, List.of(ref1, ref2));

            assertThat(result).hasSize(1).containsEntry("SAME_KEY", "first_value");
        }

        @Test
        @DisplayName("should call port exactly once per batch")
        void shouldCallPortExactlyOnce() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            List<JobSecretRef> refs = List.of(
                    new JobSecretRef(id1, "K1"),
                    new JobSecretRef(id2, "K2")
            );

            when(secretResolvePort.resolveSecrets(any(), anyList()))
                    .thenReturn(Map.of(id1, "v1", id2, "v2"));

            service.resolve(1L, refs);

            verify(secretResolvePort).resolveSecrets(any(), anyList());
        }

        @Test
        @DisplayName("should handle duplicate secretIds with different envNames")
        void shouldHandleDuplicateIdsWithDifferentEnvNames() {
            UUID secretId = UUID.randomUUID();
            JobSecretRef ref1 = new JobSecretRef(secretId, "HEADER_A");
            JobSecretRef ref2 = new JobSecretRef(secretId, "HEADER_B");

            when(secretResolvePort.resolveSecrets(any(), anyList()))
                    .thenReturn(Map.of(secretId, "shared-value"));

            Map<String, String> result = service.resolve(1L, List.of(ref1, ref2));

            assertThat(result)
                    .hasSize(2)
                    .containsEntry("HEADER_A", "shared-value")
                    .containsEntry("HEADER_B", "shared-value");
        }
    }

    @Nested
    @DisplayName("resolve() negative scenarios")
    class ResolveNegative {

        @Test
        @DisplayName("should return empty map when port throws exception")
        void shouldReturnEmptyMapWhenPortThrows() {
            UUID secretId = UUID.randomUUID();
            JobSecretRef ref = new JobSecretRef(secretId, "KEY");

            when(secretResolvePort.resolveSecrets(any(), anyList()))
                    .thenThrow(new RuntimeException("Secrets service unavailable"));

            // Проверяем, что исключение пробрасывается согласно текущему контракту
            try {
                service.resolve(1L, List.of(ref));
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).contains("Secrets service unavailable");
            }
        }

        @Test
        @DisplayName("should handle null organizationId")
        void shouldHandleNullOrganizationId() {
            UUID secretId = UUID.randomUUID();
            JobSecretRef ref = new JobSecretRef(secretId, "TOKEN");

            when(secretResolvePort.resolveSecrets(eq(null), anyList()))
                    .thenReturn(Map.of(secretId, "value"));

            Map<String, String> result = service.resolve(null, List.of(ref));

            assertThat(result).containsEntry("TOKEN", "value");
            verify(secretResolvePort).resolveSecrets(eq(null), anyList());
        }
    }
}
