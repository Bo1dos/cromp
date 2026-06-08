package com.cromp.notifications.domain.service;

import com.cromp.notifications.domain.model.NotificationEventType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationTemplateService {

    public String renderTitle(NotificationEventType type, Map<String, Object> context) {
        return switch (type) {
            case INVITATION_CREATED -> {
                boolean own = Boolean.TRUE.equals(context.get("own"));
                if (own) {
                    yield "Invitation sent to " + context.getOrDefault("email", "unknown");
                } else {
                    yield "You've been invited to an organization";
                }
            }
            case INVITATION_ACCEPTED -> context.getOrDefault("userName", "Someone") + " accepted your invitation";
            case JOB_EXECUTION_FAILED -> "Job '" + context.getOrDefault("jobName", "unknown") + "' failed";
            case JOB_EXECUTION_SUCCEEDED -> "Job '" + context.getOrDefault("jobName", "unknown") + "' succeeded";
            case JOB_DISABLED -> "Job '" + context.getOrDefault("jobName", "unknown") + "' was disabled";
            case JOB_EXECUTION_TIMEOUT -> "Job '" + context.getOrDefault("jobName", "unknown") + "' timed out";
            case MEMBER_ADDED -> context.getOrDefault("userName", "Someone") + " joined the organization";
            case MEMBER_REMOVED -> context.getOrDefault("userName", "Someone") + " was removed";
            case MEMBER_ROLE_CHANGED -> context.getOrDefault("userName", "Someone") + "'s role changed";
            case SECRET_EXPIRING -> "Secret '" + context.getOrDefault("secretName", "unknown") + "' is expiring";
            case SECRET_ROTATED -> "Secret '" + context.getOrDefault("secretName", "unknown") + "' was rotated";
            default -> type.name();
        };
    }

    public String renderBody(NotificationEventType type, Map<String, Object> context) {
        return renderTitle(type, context);
    }

    public String renderEmailHtml(NotificationEventType type, Map<String, Object> context) {
        String title = renderTitle(type, context);
        String body = renderBody(type, context);
        return "<html><body style=\"font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;\">"
                + "<h2>" + title + "</h2>"
                + "<p>" + body + "</p>"
                + "<hr style=\"margin-top:30px;border-color:#eee;\">"
                + "<p style=\"color:#999;font-size:12px;\">This is an automated message from Cron-as-a-Service.</p>"
                + "</body></html>";
    }
}
