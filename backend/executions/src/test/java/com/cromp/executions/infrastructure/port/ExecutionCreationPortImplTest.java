package com.cromp.executions.infrastructure.port;

import com.cromp.executions.application.service.ExecutionService;
import com.cromp.executions.api.dto.request.CreateExecutionRequest;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutionCreationPortImplTest {

    @Mock private ExecutionService executionService;

    @Test
    void shouldMapJobsExecutionCreationContractToApplicationRequest() {
        ExecutionCreationPortImpl port = new ExecutionCreationPortImpl(executionService);
        UUID execUuid = UUID.randomUUID();
        when(executionService.createExecution(any(CreateExecutionRequest.class))).thenReturn(execUuid);

        UUID returned = port.createExecution(
                10L, 20L, 30L, "API", 11L, Map.of("k", "v"), UUID.randomUUID(), 5,
                Instant.parse("2024-01-01T00:00:00Z"), "{\"policy\":true}"
        );

        ArgumentCaptor<CreateExecutionRequest> captor = ArgumentCaptor.forClass(CreateExecutionRequest.class);
        verify(executionService).createExecution(captor.capture());
        assertThat(captor.getValue().source()).isEqualTo(ExecutionSource.API);
        assertThat(captor.getValue().executionPolicySnapshot()).isEqualTo("{\"policy\":true}");
        assertThat(returned).isEqualTo(execUuid);
    }
}
