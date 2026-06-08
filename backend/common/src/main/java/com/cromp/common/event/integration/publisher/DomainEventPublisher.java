package com.cromp.common.event.integration.publisher;

import com.cromp.common.event.DomainEvent;

import java.util.List;

public interface DomainEventPublisher {

    void publish(DomainEvent event);

    void publishAll(List<DomainEvent> events);
}
