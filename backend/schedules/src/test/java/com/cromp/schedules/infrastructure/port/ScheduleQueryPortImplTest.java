package com.cromp.schedules.infrastructure.port;

import com.cromp.schedules.domain.model.Schedule;
import com.cromp.schedules.domain.model.enums.ScheduleStatus;
import com.cromp.schedules.domain.repository.ScheduleRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleQueryPortImplTest {

    @Mock
    private ScheduleRepositoryPort scheduleRepository;

    @Test
    void findActiveSchedulesReadyForRunShouldReturnOnlyActiveSchedulesWithPastNextRun() {
        Instant now = Instant.now();
        Schedule activePast = schedule(now.minusSeconds(60), ScheduleStatus.ACTIVE);
        Schedule activeNow = schedule(now, ScheduleStatus.ACTIVE);
        Schedule activeFuture = schedule(now.plusSeconds(60), ScheduleStatus.ACTIVE);
        Schedule nullNextRun = Schedule.reconstitute(5L, 10L, "*/5 * * * *", "UTC", null, null,
                ScheduleStatus.ACTIVE, now, now, null);

        when(scheduleRepository.findAllByStatus(ScheduleStatus.ACTIVE))
                .thenReturn(List.of(activePast, activeNow, activeFuture, nullNextRun));

        List<Schedule> ready = new ScheduleQueryPortImpl(scheduleRepository).findActiveSchedulesReadyForRun();

        assertThat(ready).extracting(Schedule::getNextRunAt)
                .contains(activePast.getNextRunAt(), activeNow.getNextRunAt())
                .doesNotContain(activeFuture.getNextRunAt())
                .doesNotContainNull();
        assertThat(ready).allMatch(schedule -> schedule.getStatus() == ScheduleStatus.ACTIVE);
    }

    private static Schedule schedule(Instant nextRunAt, ScheduleStatus status) {
        return Schedule.reconstitute(1L, 10L, "*/5 * * * *", "UTC", null, nextRunAt, status,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"), null);
    }
}
