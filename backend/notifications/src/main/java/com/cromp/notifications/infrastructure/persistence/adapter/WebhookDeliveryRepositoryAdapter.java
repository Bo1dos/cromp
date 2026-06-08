package com.cromp.notifications.infrastructure.persistence.adapter;

import com.cromp.notifications.domain.model.WebhookDelivery;
import com.cromp.notifications.domain.repository.WebhookDeliveryRepositoryPort;
import com.cromp.notifications.infrastructure.persistence.jpa.entity.WebhookDeliveryJpaEntity;
import com.cromp.notifications.infrastructure.persistence.jpa.repository.WebhookDeliveryJpaRepository;
import com.cromp.notifications.infrastructure.persistence.mapper.WebhookDeliveryPersistenceMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class WebhookDeliveryRepositoryAdapter implements WebhookDeliveryRepositoryPort {

    private final WebhookDeliveryJpaRepository jpaRepository;
    private final WebhookDeliveryPersistenceMapper mapper;

    public WebhookDeliveryRepositoryAdapter(WebhookDeliveryJpaRepository jpaRepository,
                                             WebhookDeliveryPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public WebhookDelivery save(WebhookDelivery delivery) {
        WebhookDeliveryJpaEntity entity = mapper.toJpa(delivery);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<WebhookDelivery> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<WebhookDelivery> findBySubscriptionId(Long subscriptionId, Pageable pageable) {
        return jpaRepository.findBySubscriptionId(subscriptionId, pageable)
                .map(mapper::toDomain).getContent();
    }

    @Override
    public List<WebhookDelivery> findPendingRetries(Instant before) {
        return jpaRepository.findByStatusAndNextRetryAtBefore("PENDING", before).stream()
                .map(mapper::toDomain).toList();
    }
}
