package com.cromp.notifications.domain.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NotificationPreferences {

    private Long id;

    @EqualsAndHashCode.Include
    private Long userId;

    private Map<NotificationChannel, Boolean> channels;
    private Map<NotificationEventType, Boolean> eventTypes;
    private Instant createdAt;
    private Instant updatedAt;

    private NotificationPreferences(Long id,
                                    Long userId,
                                    Map<NotificationChannel, Boolean> channels,
                                    Map<NotificationEventType, Boolean> eventTypes,
                                    Instant createdAt,
                                    Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.channels = new HashMap<>(channels);
        this.eventTypes = new HashMap<>(eventTypes);
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? Instant.now() : updatedAt;
    }

    public static NotificationPreferences createDefault(Long userId) {
        Map<NotificationChannel, Boolean> defaultChannels = new HashMap<>();
        defaultChannels.put(NotificationChannel.IN_APP, true);
        defaultChannels.put(NotificationChannel.EMAIL, true);
        defaultChannels.put(NotificationChannel.WEBHOOK, false);

        Map<NotificationEventType, Boolean> defaultEvents = new HashMap<>();
        for (NotificationEventType type : NotificationEventType.values()) {
            defaultEvents.put(type, true);
        }

        return new NotificationPreferences(
                null, userId, defaultChannels, defaultEvents,
                Instant.now(), Instant.now()
        );
    }

    public static NotificationPreferences reconstitute(Long id,
                                                        Long userId,
                                                        Map<NotificationChannel, Boolean> channels,
                                                        Map<NotificationEventType, Boolean> eventTypes,
                                                        Instant createdAt,
                                                        Instant updatedAt) {
        return new NotificationPreferences(id, userId, channels, eventTypes, createdAt, updatedAt);
    }

    public boolean isChannelEnabled(NotificationChannel channel) {
        return channels.getOrDefault(channel, false);
    }

    public boolean isEventEnabled(NotificationEventType eventType) {
        return eventTypes.getOrDefault(eventType, true);
    }

    public void updateChannels(Map<NotificationChannel, Boolean> newChannels) {
        this.channels = new HashMap<>(newChannels);
        this.updatedAt = Instant.now();
    }

    public void updateEventTypes(Map<NotificationEventType, Boolean> newEventTypes) {
        this.eventTypes = new HashMap<>(newEventTypes);
        this.updatedAt = Instant.now();
    }
}
