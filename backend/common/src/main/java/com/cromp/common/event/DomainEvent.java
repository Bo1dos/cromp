package com.cromp.common.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {

    UUID eventId();

    String eventType();

    Instant occurredAt();

    Long organizationId();
}
