package com.cromp.executions.domain.service;

import com.cromp.executions.domain.model.enums.AttemptStatus;
import com.cromp.executions.domain.model.exceptions.InvalidStatusTransitionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttemptStateMachineTest {

    @Test
    void shouldAllowValidTransitions() {
        AttemptStateMachine.assertTransitionAllowed(AttemptStatus.PENDING, AttemptStatus.DISPATCHED);
        AttemptStateMachine.assertTransitionAllowed(AttemptStatus.DISPATCHED, AttemptStatus.RUNNING);
        AttemptStateMachine.assertTransitionAllowed(AttemptStatus.RUNNING, AttemptStatus.SUCCEEDED);
        AttemptStateMachine.assertTransitionAllowed(AttemptStatus.RUNNING, AttemptStatus.TIMEOUT);
    }

    @Test
    void shouldRejectInvalidTransitions() {
        assertThatThrownBy(() -> AttemptStateMachine.assertTransitionAllowed(AttemptStatus.SUCCEEDED, AttemptStatus.RUNNING))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("Attempt: transition SUCCEEDED → RUNNING is not allowed");
    }
}
