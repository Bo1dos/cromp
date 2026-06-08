package com.cromp.notifications.application.event.listener;

import com.cromp.common.event.domain.invitation.*;
import com.cromp.common.application.port.EmailSenderPort;
import com.cromp.iam.domain.model.User;
import com.cromp.iam.domain.repository.UserRepositoryPort;
import com.cromp.notifications.application.service.InAppNotificationService;
import com.cromp.notifications.domain.model.NotificationEventType;
import com.cromp.notifications.domain.service.NotificationRoutingService;
import com.cromp.notifications.domain.service.NotificationTemplateService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.List;
import com.cromp.notifications.domain.model.NotificationChannel;

@Component
public class InvitationNotificationListener {

    private final NotificationRoutingService routingService;
    private final InAppNotificationService inAppService;
    private final EmailSenderPort emailSenderPort;
    private final NotificationTemplateService templateService;
    private final UserRepositoryPort userRepository;

    public InvitationNotificationListener(NotificationRoutingService routingService,
                                           InAppNotificationService inAppService,
                                           EmailSenderPort emailSenderPort,
                                           NotificationTemplateService templateService,
                                           UserRepositoryPort userRepository) {
        this.routingService = routingService;
        this.inAppService = inAppService;
        this.emailSenderPort = emailSenderPort;
        this.templateService = templateService;
        this.userRepository = userRepository;
    }

    @EventListener
    public void onInvitationCreated(InvitationCreatedEvent event) {
        Long orgId = event.organizationId();
        // Пригласившему: "Invitation sent to X"
        if (event.invitedByUserId() != null) {
            inAppService.send(event.invitedByUserId(), orgId,
                    NotificationEventType.INVITATION_CREATED,
                    Map.of("email", event.email(), "roleName", event.roleName(),
                            "invitationUuid", event.invitationUuid().toString(),
                            "own", true));
        }
        // Приглашённому (если зарегистрирован): "You've been invited to Org"
        userRepository.findByEmail(event.email())
                .map(User::getId)
                .ifPresent(invitedUserId -> {
                    if (!invitedUserId.equals(event.invitedByUserId())) {
                        inAppService.send(invitedUserId, orgId,
                                NotificationEventType.INVITATION_CREATED,
                                Map.of("email", event.email(), "roleName", event.roleName(),
                                        "invitationUuid", event.invitationUuid().toString(),
                                        "own", false));
                    }
                });
    }

    @EventListener
    public void onInvitationAccepted(InvitationAcceptedEvent event) {
        notifyRecipients(event, NotificationEventType.INVITATION_ACCEPTED,
                Map.of("userName", event.acceptedByName()));
    }

    @EventListener
    public void onInvitationRejected(InvitationRejectedEvent event) {
        notifyRecipients(event, NotificationEventType.INVITATION_REJECTED,
                Map.of());
    }

    @EventListener
    public void onInvitationRevoked(InvitationRevokedEvent event) {
        notifyRecipients(event, NotificationEventType.INVITATION_REVOKED,
                Map.of("email", event.email()));
    }

    @EventListener
    public void onInvitationExpired(InvitationExpiredEvent event) {
        notifyRecipients(event, NotificationEventType.INVITATION_EXPIRED,
                Map.of("email", event.email()));
    }

    private void notifyRecipients(Object event, NotificationEventType eventType,
                                   Map<String, Object> context) {
        List<Long> recipients = routingService.resolveRecipients(
                (com.cromp.common.event.DomainEvent) event);
        Long orgId = ((com.cromp.common.event.DomainEvent) event).organizationId();

        for (Long userId : recipients) {
            Set<NotificationChannel> channels = routingService.determineChannels(userId, eventType);

            if (channels.contains(NotificationChannel.IN_APP)) {
                inAppService.send(userId, orgId, eventType, context);
            }
        }
    }
}
