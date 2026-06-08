package com.cromp.notifications.application.event.listener;

import com.cromp.common.event.domain.membership.*;
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
public class MembershipNotificationListener {

    private final NotificationRoutingService routingService;
    private final InAppNotificationService inAppService;
    private final EmailSenderPort emailSenderPort;
    private final NotificationTemplateService templateService;

    public MembershipNotificationListener(NotificationRoutingService routingService,
                                           InAppNotificationService inAppService,
                                           EmailSenderPort emailSenderPort,
                                           NotificationTemplateService templateService) {
        this.routingService = routingService;
        this.inAppService = inAppService;
        this.emailSenderPort = emailSenderPort;
        this.templateService = templateService;
    }

    @EventListener
    public void onMemberAdded(MemberAddedEvent event) {
        notifyRecipients(event, NotificationEventType.MEMBER_ADDED,
                Map.of("userName", event.userName(), "roleName", event.roleName()));
    }

    @EventListener
    public void onMemberRemoved(MemberRemovedEvent event) {
        notifyRecipients(event, NotificationEventType.MEMBER_REMOVED,
                Map.of("userName", event.userName()));
    }

    @EventListener
    public void onMemberRoleChanged(MemberRoleChangedEvent event) {
        notifyRecipients(event, NotificationEventType.MEMBER_ROLE_CHANGED,
                Map.of("userName", event.userName(), "oldRole", event.oldRole(), "newRole", event.newRole()));
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
