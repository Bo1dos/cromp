package com.cromp.notifications.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Notification domain model")
class NotificationTest {

    @Test
    @DisplayName("should create notification with required fields")
    void shouldCreateNotification() {
        var n = Notification.create(
                1L, 10L, NotificationEventType.JOB_EXECUTION_FAILED,
                NotificationChannel.EMAIL, "Job failed", "Details here", null
        );

        assertThat(n.getNotificationUuid()).isNotNull();
        assertThat(n.getUserId()).isEqualTo(1L);
        assertThat(n.getOrganizationId()).isEqualTo(10L);
        assertThat(n.getEventType()).isEqualTo(NotificationEventType.JOB_EXECUTION_FAILED);
        assertThat(n.getChannel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(n.getTitle()).isEqualTo("Job failed");
        assertThat(n.getBody()).isEqualTo("Details here");
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        assertThat(n.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("markEmailSent should record email delivery")
    void shouldMarkEmailSent() {
        var n = Notification.create(1L, 10L, NotificationEventType.JOB_EXECUTION_FAILED,
                NotificationChannel.EMAIL, "Failed", "Details", null);

        n.markEmailSent("user@example.com");

        assertThat(n.getEmailSentTo()).isEqualTo("user@example.com");
        assertThat(n.getEmailSentAt()).isNotNull();
    }

    @Test
    @DisplayName("should mark as read")
    void shouldMarkAsRead() {
        var n = Notification.create(1L, 10L, NotificationEventType.JOB_EXECUTION_FAILED,
                NotificationChannel.EMAIL, "Failed", "Details", null);
        n.markEmailSent("user@example.com");
        n.markRead();

        assertThat(n.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(n.getReadAt()).isNotNull();
    }

    @Test
    @DisplayName("should allow reconstitution from database")
    void shouldReconstitute() {
        UUID uuid = UUID.randomUUID();
        Instant now = Instant.now();

        var n = Notification.reconstitute(
                1L, uuid, 2L, 20L, NotificationEventType.INVITATION_CREATED,
                NotificationChannel.EMAIL, "Invite", "You are invited", null,
                NotificationStatus.UNREAD, null, null, null, now
        );

        assertThat(n.getNotificationUuid()).isEqualTo(uuid);
        assertThat(n.getUserId()).isEqualTo(2L);
        assertThat(n.getOrganizationId()).isEqualTo(20L);
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.UNREAD);
    }
}
