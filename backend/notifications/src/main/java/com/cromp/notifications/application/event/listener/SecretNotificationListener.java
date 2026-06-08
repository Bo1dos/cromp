package com.cromp.notifications.application.event.listener;

import com.cromp.common.event.domain.secret.*;
import com.cromp.common.application.port.EmailSenderPort;
import com.cromp.notifications.application.service.InAppNotificationService;
import com.cromp.notifications.domain.model.NotificationChannel;
import com.cromp.notifications.domain.model.NotificationEventType;
import com.cromp.notifications.domain.service.NotificationRoutingService;
import com.cromp.notifications.domain.service.NotificationTemplateService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class SecretNotificationListener {

    private final NotificationRoutingService routingService;
    private final InAppNotificationService inAppService;
    private final EmailSenderPort emailSenderPort;
    private final NotificationTemplateService templateService;

    public SecretNotificationListener(NotificationRoutingService routingService,
                                       InAppNotificationService inAppService,
                                       EmailSenderPort emailSenderPort,
                                       NotificationTemplateService templateService) {
        this.routingService = routingService;
        this.inAppService = inAppService;
        this.emailSenderPort = emailSenderPort;
        this.templateService = templateService;
    }

    @EventListener
    public void onSecretExpiring(SecretExpiringEvent event) {
        notifyRecipients(event, NotificationEventType.SECRET_EXPIRING,
                Map.of("secretName", event.secretName(),
                        "daysRemaining", String.valueOf(event.daysRemaining())));
    }

    @EventListener
    public void onSecretRotated(SecretRotatedEvent event) {
        notifyRecipients(event, NotificationEventType.SECRET_ROTATED,
                Map.of("secretName", event.secretName()));
    }

    private void notifyRecipients(Object event, NotificationEventType eventType,
                                   Map<String, Object> context) {
        var domainEvent = (com.cromp.common.event.DomainEvent) event;
        List<Long> recipients = routingService.resolveRecipients(domainEvent);

        for (Long userId : recipients) {
            Set<NotificationChannel> channels = routingService.determineChannels(userId, eventType);

            if (channels.contains(NotificationChannel.IN_APP)) {
                inAppService.send(userId, domainEvent.organizationId(), eventType, context);
            }
        }
    }
}
