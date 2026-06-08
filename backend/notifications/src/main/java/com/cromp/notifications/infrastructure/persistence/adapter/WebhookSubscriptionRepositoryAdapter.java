package com.cromp.notifications.infrastructure.persistence.adapter;

import com.cromp.notifications.domain.model.WebhookSubscription;
import com.cromp.notifications.domain.repository.WebhookSubscriptionRepositoryPort;
import com.cromp.notifications.infrastructure.persistence.jpa.entity.WebhookSubscriptionJpaEntity;
import com.cromp.notifications.infrastructure.persistence.jpa.repository.WebhookSubscriptionJpaRepository;
import com.cromp.notifications.infrastructure.persistence.mapper.WebhookSubscriptionPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class WebhookSubscriptionRepositoryAdapter implements WebhookSubscriptionRepositoryPort {

    private final WebhookSubscriptionJpaRepository jpaRepository;
    private final WebhookSubscriptionPersistenceMapper mapper;

    public WebhookSubscriptionRepositoryAdapter(WebhookSubscriptionJpaRepository jpaRepository,
                                                 WebhookSubscriptionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public WebhookSubscription save(WebhookSubscription subscription) {
        WebhookSubscriptionJpaEntity entity = mapper.toJpa(subscription);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<WebhookSubscription> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<WebhookSubscription> findBySubscriptionUuid(UUID subscriptionUuid) {
        return jpaRepository.findBySubscriptionUuid(subscriptionUuid).map(mapper::toDomain);
    }

    @Override
    public List<WebhookSubscription> findByOrganizationId(Long organizationId) {
        return jpaRepository.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public void delete(WebhookSubscription subscription) {
        jpaRepository.deleteById(subscription.getId());
    }
}
