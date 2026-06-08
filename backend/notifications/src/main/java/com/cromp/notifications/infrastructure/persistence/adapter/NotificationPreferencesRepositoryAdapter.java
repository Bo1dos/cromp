package com.cromp.notifications.infrastructure.persistence.adapter;

import com.cromp.notifications.domain.model.NotificationPreferences;
import com.cromp.notifications.domain.repository.NotificationPreferencesRepositoryPort;
import com.cromp.notifications.infrastructure.persistence.jpa.entity.NotificationPreferencesJpaEntity;
import com.cromp.notifications.infrastructure.persistence.jpa.repository.NotificationPreferencesJpaRepository;
import com.cromp.notifications.infrastructure.persistence.mapper.NotificationPreferencesPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class NotificationPreferencesRepositoryAdapter implements NotificationPreferencesRepositoryPort {

    private final NotificationPreferencesJpaRepository jpaRepository;
    private final NotificationPreferencesPersistenceMapper mapper;

    public NotificationPreferencesRepositoryAdapter(NotificationPreferencesJpaRepository jpaRepository,
                                                     NotificationPreferencesPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public NotificationPreferences save(NotificationPreferences preferences) {
        NotificationPreferencesJpaEntity entity = mapper.toJpa(preferences);
        NotificationPreferencesJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<NotificationPreferences> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }
}
