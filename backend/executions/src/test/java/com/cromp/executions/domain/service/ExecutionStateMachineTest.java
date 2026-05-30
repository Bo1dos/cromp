package com.cromp.executions.domain.service;

import com.cromp.executions.domain.model.enums.ExecutionStatus;
import com.cromp.executions.domain.model.exceptions.InvalidStatusTransitionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExecutionStateMachineTest {

    @Test
    void shouldAllowValidTransitions() {
        ExecutionStateMachine.assertTransitionAllowed(ExecutionStatus.CREATED, ExecutionStatus.IN_PROGRESS);
        ExecutionStateMachine.assertTransitionAllowed(ExecutionStatus.CREATED, ExecutionStatus.CANCELLED);
        ExecutionStateMachine.assertTransitionAllowed(ExecutionStatus.IN_PROGRESS, ExecutionStatus.SUCCEEDED);
        ExecutionStateMachine.assertTransitionAllowed(ExecutionStatus.IN_PROGRESS, ExecutionStatus.FAILED);
    }

    @Test
    void shouldRejectInvalidTransitions() {
        assertThatThrownBy(() -> ExecutionStateMachine.assertTransitionAllowed(ExecutionStatus.SUCCEEDED, ExecutionStatus.CANCELLED))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("Execution: transition SUCCEEDED → CANCELLED is not allowed");
    }
}
