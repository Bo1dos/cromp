package com.cromp.notifications.domain.service;

import com.cromp.notifications.domain.model.NotificationEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationTemplateService")
class NotificationTemplateServiceTest {

    private final NotificationTemplateService service = new NotificationTemplateService();

    @Test
    @DisplayName("should render invitation title for recipient")
    void shouldRenderInvitationTitleForRecipient() {
        var ctx = Map.<String, Object>of("own", false, "email", "user@example.com");
        String title = service.renderTitle(NotificationEventType.INVITATION_CREATED, ctx);
        assertThat(title).isEqualTo("You've been invited to an organization");
    }

    @Test
    @DisplayName("should render invitation title for sender")
    void shouldRenderInvitationTitleForSender() {
        var ctx = Map.<String, Object>of("own", true, "email", "user@example.com");
        String title = service.renderTitle(NotificationEventType.INVITATION_CREATED, ctx);
        assertThat(title).contains("Invitation sent to user@example.com");
    }

    @Test
    @DisplayName("should render job failure title with job name")
    void shouldRenderJobFailureTitle() {
        var ctx = Map.<String, Object>of("jobName", "daily-backup");
        String title = service.renderTitle(NotificationEventType.JOB_EXECUTION_FAILED, ctx);
        assertThat(title).isEqualTo("Job 'daily-backup' failed");
    }

    @Test
    @DisplayName("should render job success title")
    void shouldRenderJobSuccessTitle() {
        var ctx = Map.<String, Object>of("jobName", "sync");
        String title = service.renderTitle(NotificationEventType.JOB_EXECUTION_SUCCEEDED, ctx);
        assertThat(title).isEqualTo("Job 'sync' succeeded");
    }

    @Test
    @DisplayName("should render secret expiring title")
    void shouldRenderSecretExpiringTitle() {
        var ctx = Map.<String, Object>of("secretName", "api-key");
        String title = service.renderTitle(NotificationEventType.SECRET_EXPIRING, ctx);
        assertThat(title).isEqualTo("Secret 'api-key' is expiring");
    }

    @Test
    @DisplayName("should render member added title")
    void shouldRenderMemberAddedTitle() {
        var ctx = Map.<String, Object>of("userName", "Ivan");
        String title = service.renderTitle(NotificationEventType.MEMBER_ADDED, ctx);
        assertThat(title).isEqualTo("Ivan joined the organization");
    }

    @Test
    @DisplayName("should generate valid HTML email body")
    void shouldGenerateValidHtmlEmail() {
        var ctx = Map.<String, Object>of("jobName", "test-job");
        String html = service.renderEmailHtml(NotificationEventType.JOB_EXECUTION_FAILED, ctx);

        assertThat(html).contains("<html>");
        assertThat(html).contains("Job 'test-job' failed");
        assertThat(html).contains("Cron-as-a-Service");
    }

    @Test
    @DisplayName("should fallback to event type name for unknown type")
    void shouldFallbackForUnknownType() {
        String title = service.renderTitle(NotificationEventType.JOB_DISABLED, Map.of("jobName", "x"));
        assertThat(title).isEqualTo("Job 'x' was disabled");
    }
}
