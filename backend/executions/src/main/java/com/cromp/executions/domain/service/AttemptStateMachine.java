package com.cromp.executions.domain.service;

import com.cromp.executions.domain.model.enums.AttemptStatus;
import com.cromp.executions.domain.model.exceptions.InvalidStatusTransitionException;

import java.util.Map;
import java.util.Set;

public class AttemptStateMachine {

    private static final Map<AttemptStatus, Set<AttemptStatus>> ALLOWED =
            Map.of(
                    AttemptStatus.PENDING,     Set.of(AttemptStatus.DISPATCHED, AttemptStatus.CANCELLED),
                    AttemptStatus.DISPATCHED,  Set.of(AttemptStatus.RUNNING, AttemptStatus.SUCCEEDED, AttemptStatus.FAILED, AttemptStatus.TIMEOUT, AttemptStatus.CANCELLED),
                    AttemptStatus.RUNNING,     Set.of(AttemptStatus.SUCCEEDED, AttemptStatus.FAILED, AttemptStatus.TIMEOUT, AttemptStatus.CANCELLED),
                    AttemptStatus.RETRYING,    Set.of(AttemptStatus.PENDING),
                    AttemptStatus.SUCCEEDED,   Set.of(),
                    AttemptStatus.FAILED,      Set.of(),
                    AttemptStatus.TIMEOUT,     Set.of(),
                    AttemptStatus.CANCELLED,   Set.of()
            );

    public static void assertTransitionAllowed(AttemptStatus from, AttemptStatus to) {
        Set<AttemptStatus> allowed = ALLOWED.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new InvalidStatusTransitionException(
                    "Attempt: transition " + from + " → " + to + " is not allowed"
            );
        }
    }
}