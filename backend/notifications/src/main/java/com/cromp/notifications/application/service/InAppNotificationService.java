package com.cromp.notifications.application.service;

import com.cromp.notifications.domain.model.Notification;
import com.cromp.notifications.domain.model.NotificationChannel;
import com.cromp.notifications.domain.model.NotificationEventType;
import com.cromp.notifications.domain.repository.NotificationRepositoryPort;
import com.cromp.notifications.domain.service.NotificationTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Transactional
public class InAppNotificationService {

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationTemplateService templateService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InAppNotificationService(NotificationRepositoryPort notificationRepository,
                                     NotificationTemplateService templateService) {
        this.notificationRepository = notificationRepository;
        this.templateService = templateService;
    }

    public void send(Long userId, Long orgId, NotificationEventType eventType, Map<String, Object> context) {
        String title = templateService.renderTitle(eventType, context);
        String body = templateService.renderBody(eventType, context);
        String metadataJson = serializeMetadata(context);

        Notification notification = Notification.create(
                userId, orgId, eventType, NotificationChannel.IN_APP,
                title, body, metadataJson
        );
        notificationRepository.save(notification);
    }

    private String serializeMetadata(Map<String, Object> context) {
        try {
            return objectMapper.writeValueAsString(context);
        } catch (Exception e) {
            return null;
        }
    }
}
