package com.cromp.notifications.application.service;

import com.cromp.notifications.api.dto.response.*;
import com.cromp.notifications.api.mapper.NotificationApiMapper;
import com.cromp.notifications.api.service.NotificationFacade;
import com.cromp.notifications.domain.model.Notification;
import com.cromp.notifications.domain.model.NotificationStatus;
import com.cromp.notifications.domain.model.exceptions.NotificationNotFoundException;
import com.cromp.notifications.domain.repository.NotificationRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationApplicationService implements NotificationFacade {

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationApiMapper mapper;

    public NotificationApplicationService(NotificationRepositoryPort notificationRepository,
                                           NotificationApiMapper mapper) {
        this.notificationRepository = notificationRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedNotificationResponse list(Long userId, NotificationStatus status, int page, int size) {
        var pageable = PageRequest.of(page, size);
        List<Notification> items;
        long total;

        if (status != null) {
            items = notificationRepository.findByUserIdAndStatus(userId, status, pageable);
            total = notificationRepository.countByUserIdAndStatus(userId, status);
        } else {
            items = notificationRepository.findByUserId(userId, pageable);
            total = notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD)
                    + notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.READ);
        }

        List<NotificationResponse> responses = items.stream()
                .map(mapper::toResponse)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PagedNotificationResponse(responses, page, size, total, totalPages);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount(Long userId) {
        long count = notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
        return new UnreadCountResponse(count);
    }

    @Override
    public void markRead(Long userId, UUID notificationUuid) {
        Notification notification = notificationRepository.findByNotificationUuid(notificationUuid)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + notificationUuid));
        notification.markRead();
        notificationRepository.save(notification);
    }

    @Override
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    @Override
    public void delete(Long userId, UUID notificationUuid) {
        Notification notification = notificationRepository.findByNotificationUuid(notificationUuid)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + notificationUuid));
        notification.archive();
        notificationRepository.save(notification);
    }
}
