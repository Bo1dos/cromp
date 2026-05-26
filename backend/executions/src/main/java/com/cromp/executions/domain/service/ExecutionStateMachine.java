package com.cromp.executions.domain.service;

import com.cromp.executions.domain.model.enums.ExecutionStatus;
import com.cromp.executions.domain.model.exceptions.InvalidStatusTransitionException;

import java.util.Map;
import java.util.Set;

public class ExecutionStateMachine {

    // Допустимые переходы: from -> set of allowed to
    private static final Map<ExecutionStatus, Set<ExecutionStatus>> ALLOWED =
            Map.of(
                    ExecutionStatus.CREATED,      Set.of(ExecutionStatus.IN_PROGRESS, ExecutionStatus.CANCELLED, ExecutionStatus.SKIPPED),
                    ExecutionStatus.IN_PROGRESS,  Set.of(ExecutionStatus.SUCCEEDED, ExecutionStatus.FAILED, ExecutionStatus.CANCELLED),
                    ExecutionStatus.SUCCEEDED,    Set.of(),
                    ExecutionStatus.FAILED,       Set.of(),
                    ExecutionStatus.CANCELLED,    Set.of(),
                    ExecutionStatus.SKIPPED,      Set.of()
            );

    public static void assertTransitionAllowed(ExecutionStatus from, ExecutionStatus to) {
        Set<ExecutionStatus> allowed = ALLOWED.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new InvalidStatusTransitionException(
                    "Execution: transition " + from + " → " + to + " is not allowed"
            );
        }
    }
}