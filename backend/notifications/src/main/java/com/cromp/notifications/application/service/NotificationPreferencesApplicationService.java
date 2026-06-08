package com.cromp.notifications.application.service;

import com.cromp.notifications.api.dto.request.UpdateNotificationPreferencesRequest;
import com.cromp.notifications.api.dto.response.NotificationPreferencesResponse;
import com.cromp.notifications.api.mapper.NotificationApiMapper;
import com.cromp.notifications.api.service.NotificationPreferencesFacade;
import com.cromp.notifications.domain.model.NotificationChannel;
import com.cromp.notifications.domain.model.NotificationEventType;
import com.cromp.notifications.domain.model.NotificationPreferences;
import com.cromp.notifications.domain.repository.NotificationPreferencesRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class NotificationPreferencesApplicationService implements NotificationPreferencesFacade {

    private final NotificationPreferencesRepositoryPort preferencesRepository;
    private final NotificationApiMapper mapper;

    public NotificationPreferencesApplicationService(NotificationPreferencesRepositoryPort preferencesRepository,
                                                      NotificationApiMapper mapper) {
        this.preferencesRepository = preferencesRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public NotificationPreferencesResponse get(Long userId) {
        NotificationPreferences prefs = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreferences defaults = NotificationPreferences.createDefault(userId);
                    return preferencesRepository.save(defaults);
                });
        return mapper.toPreferencesResponse(prefs);
    }

    @Override
    public NotificationPreferencesResponse update(Long userId, UpdateNotificationPreferencesRequest request) {
        NotificationPreferences prefs = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> NotificationPreferences.createDefault(userId));

        if (request.channels() != null) {
            Map<NotificationChannel, Boolean> channelMap = new HashMap<>();
            for (var entry : request.channels().entrySet()) {
                channelMap.put(NotificationChannel.valueOf(entry.getKey()), entry.getValue());
            }
            prefs.updateChannels(channelMap);
        }

        if (request.eventTypes() != null) {
            Map<NotificationEventType, Boolean> eventMap = new HashMap<>();
            for (var entry : request.eventTypes().entrySet()) {
                eventMap.put(NotificationEventType.valueOf(entry.getKey()), entry.getValue());
            }
            prefs.updateEventTypes(eventMap);
        }

        prefs = preferencesRepository.save(prefs);
        return mapper.toPreferencesResponse(prefs);
    }
}
