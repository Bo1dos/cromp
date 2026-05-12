package com.cromp.schedules.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUpdateScheduleRequest(
        @NotBlank @Size(max = 128) String cronExpression,
        @NotBlank @Size(max = 64) String timezone,
        String rules  // nullable, пока задел без валидации
) {}