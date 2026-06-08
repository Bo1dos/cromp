package com.cromp.notifications.application.event.listener;

import com.cromp.common.event.domain.job.*;
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
public class JobExecutionNotificationListener {

    private final NotificationRoutingService routingService;
    private final InAppNotificationService inAppService;
    private final EmailSenderPort emailSenderPort;
    private final NotificationTemplateService templateService;

    public JobExecutionNotificationListener(NotificationRoutingService routingService,
                                             InAppNotificationService inAppService,
                                             EmailSenderPort emailSenderPort,
                                             NotificationTemplateService templateService) {
        this.routingService = routingService;
        this.inAppService = inAppService;
        this.emailSenderPort = emailSenderPort;
        this.templateService = templateService;
    }

    @EventListener
    public void onJobFailed(JobExecutionFailedEvent event) {
        notifyRecipients(event, NotificationEventType.JOB_EXECUTION_FAILED,
                Map.of("jobName", event.jobName(), "errorMessage",
                        event.errorMessage() != null ? event.errorMessage() : "Unknown error"));
    }

    @EventListener
    public void onJobSucceeded(JobExecutionSucceededEvent event) {
        notifyRecipients(event, NotificationEventType.JOB_EXECUTION_SUCCEEDED,
                Map.of("jobName", event.jobName(), "durationMs", String.valueOf(event.durationMs())));
    }

    @EventListener
    public void onJobDisabled(JobDisabledEvent event) {
        notifyRecipients(event, NotificationEventType.JOB_DISABLED,
                Map.of("jobName", event.jobName()));
    }

    @EventListener
    public void onJobTimeout(JobExecutionTimeoutEvent event) {
        notifyRecipients(event, NotificationEventType.JOB_EXECUTION_TIMEOUT,
                Map.of("jobName", event.jobName(), "timeoutMs", String.valueOf(event.timeoutMs())));
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
