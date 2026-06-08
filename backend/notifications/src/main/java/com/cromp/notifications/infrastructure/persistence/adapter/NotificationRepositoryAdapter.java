package com.cromp.notifications.infrastructure.persistence.adapter;

import com.cromp.notifications.domain.model.Notification;
import com.cromp.notifications.domain.model.NotificationStatus;
import com.cromp.notifications.domain.repository.NotificationRepositoryPort;
import com.cromp.notifications.infrastructure.persistence.jpa.entity.NotificationJpaEntity;
import com.cromp.notifications.infrastructure.persistence.jpa.repository.NotificationJpaRepository;
import com.cromp.notifications.infrastructure.persistence.mapper.NotificationPersistenceMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationPersistenceMapper mapper;

    public NotificationRepositoryAdapter(NotificationJpaRepository jpaRepository,
                                          NotificationPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = mapper.toJpa(notification);
        NotificationJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Notification> findByNotificationUuid(UUID notificationUuid) {
        return jpaRepository.findByNotificationUuid(notificationUuid).map(mapper::toDomain);
    }

    @Override
    public List<Notification> findByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable)
                .map(mapper::toDomain).getContent();
    }

    @Override
    public List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status, Pageable pageable) {
        return jpaRepository.findByUserIdAndStatus(userId, status.name(), pageable)
                .map(mapper::toDomain).getContent();
    }

    @Override
    public long countByUserIdAndStatus(Long userId, NotificationStatus status) {
        return jpaRepository.countByUserIdAndStatus(userId, status.name());
    }

    @Override
    public void markAllReadByUserId(Long userId) {
        jpaRepository.markAllReadByUserId(userId);
    }
}
