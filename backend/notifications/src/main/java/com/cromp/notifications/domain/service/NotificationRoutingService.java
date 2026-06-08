package com.cromp.notifications.domain.service;

import com.cromp.common.event.DomainEvent;
import com.cromp.common.event.domain.invitation.InvitationCreatedEvent;
import com.cromp.common.event.domain.invitation.InvitationAcceptedEvent;
import com.cromp.common.event.domain.invitation.InvitationRejectedEvent;
import com.cromp.common.event.domain.invitation.InvitationRevokedEvent;
import com.cromp.common.event.domain.invitation.InvitationExpiredEvent;
import com.cromp.common.event.domain.membership.MemberAddedEvent;
import com.cromp.common.event.domain.membership.MemberRemovedEvent;
import com.cromp.common.event.domain.membership.MemberRoleChangedEvent;
import com.cromp.common.event.domain.job.JobExecutionFailedEvent;
import com.cromp.common.event.domain.job.JobExecutionSucceededEvent;
import com.cromp.common.event.domain.job.JobDisabledEvent;
import com.cromp.common.event.domain.job.JobExecutionTimeoutEvent;
import com.cromp.common.event.domain.secret.SecretExpiringEvent;
import com.cromp.common.event.domain.secret.SecretRotatedEvent;
import com.cromp.iam.domain.model.User;
import com.cromp.iam.domain.repository.UserRepositoryPort;
import com.cromp.notifications.domain.model.NotificationChannel;
import com.cromp.notifications.domain.model.NotificationEventType;
import com.cromp.notifications.domain.model.NotificationPreferences;
import com.cromp.notifications.domain.repository.NotificationPreferencesRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class NotificationRoutingService {

    private final NotificationPreferencesRepositoryPort preferencesRepository;
    private final UserRepositoryPort userRepository;

    public NotificationRoutingService(NotificationPreferencesRepositoryPort preferencesRepository,
                                       UserRepositoryPort userRepository) {
        this.preferencesRepository = preferencesRepository;
        this.userRepository = userRepository;
    }

    public List<Long> resolveRecipients(DomainEvent event) {
        if (event instanceof InvitationCreatedEvent e) {
            List<Long> recipients = new ArrayList<>();
            if (e.invitedByUserId() != null) recipients.add(e.invitedByUserId());
            userRepository.findByEmail(e.email())
                    .map(User::getId)
                    .ifPresent(recipients::add);
            return recipients;
        }
        if (event instanceof InvitationAcceptedEvent e) {
            return e.invitedByUserId() != null ? List.of(e.invitedByUserId()) : Collections.emptyList();
        }
        if (event instanceof InvitationRejectedEvent e) {
            return e.invitedByUserId() != null ? List.of(e.invitedByUserId()) : Collections.emptyList();
        }
        if (event instanceof InvitationExpiredEvent e) {
            return e.invitedByUserId() != null ? List.of(e.invitedByUserId()) : Collections.emptyList();
        }
        if (event instanceof MemberAddedEvent e) {
            return List.of(e.userId());
        }
        if (event instanceof MemberRemovedEvent e) {
            return List.of(e.userId());
        }
        if (event instanceof MemberRoleChangedEvent e) {
            return List.of(e.userId());
        }
        return Collections.emptyList();
    }

    public Set<NotificationChannel> determineChannels(Long userId, NotificationEventType eventType) {
        Optional<NotificationPreferences> prefsOpt = preferencesRepository.findByUserId(userId);
        if (prefsOpt.isEmpty()) {
            return Set.of(NotificationChannel.IN_APP);
        }
        NotificationPreferences prefs = prefsOpt.get();
        if (!prefs.isEventEnabled(eventType)) {
            return Collections.emptySet();
        }

        Set<NotificationChannel> channels = EnumSet.noneOf(NotificationChannel.class);
        for (NotificationChannel channel : NotificationChannel.values()) {
            if (prefs.isChannelEnabled(channel)) {
                channels.add(channel);
            }
        }
        return channels;
    }

    public NotificationEventType toEventType(DomainEvent event) {
        if (event instanceof InvitationCreatedEvent) return NotificationEventType.INVITATION_CREATED;
        if (event instanceof InvitationAcceptedEvent) return NotificationEventType.INVITATION_ACCEPTED;
        if (event instanceof InvitationRejectedEvent) return NotificationEventType.INVITATION_REJECTED;
        if (event instanceof InvitationRevokedEvent) return NotificationEventType.INVITATION_REVOKED;
        if (event instanceof InvitationExpiredEvent) return NotificationEventType.INVITATION_EXPIRED;
        if (event instanceof MemberAddedEvent) return NotificationEventType.MEMBER_ADDED;
        if (event instanceof MemberRemovedEvent) return NotificationEventType.MEMBER_REMOVED;
        if (event instanceof MemberRoleChangedEvent) return NotificationEventType.MEMBER_ROLE_CHANGED;
        if (event instanceof JobExecutionFailedEvent) return NotificationEventType.JOB_EXECUTION_FAILED;
        if (event instanceof JobExecutionSucceededEvent) return NotificationEventType.JOB_EXECUTION_SUCCEEDED;
        if (event instanceof JobDisabledEvent) return NotificationEventType.JOB_DISABLED;
        if (event instanceof JobExecutionTimeoutEvent) return NotificationEventType.JOB_EXECUTION_TIMEOUT;
        if (event instanceof SecretExpiringEvent) return NotificationEventType.SECRET_EXPIRING;
        if (event instanceof SecretRotatedEvent) return NotificationEventType.SECRET_ROTATED;
        return null;
    }
}
