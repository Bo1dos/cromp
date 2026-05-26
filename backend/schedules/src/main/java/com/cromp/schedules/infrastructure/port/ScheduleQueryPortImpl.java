package com.cromp.schedules.infrastructure.port;

import com.cromp.schedules.application.port.ScheduleQueryPort;
import com.cromp.schedules.domain.model.Schedule;
import com.cromp.schedules.domain.model.enums.ScheduleStatus;
import com.cromp.schedules.domain.repository.ScheduleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Реализация порта запросов расписаний для orchestrator'а.
 *
 * Живёт в модуле schedules — знает о его инфраструктуре,
 * но сам интерфейс ({@link ScheduleQueryPort}) принадлежит application-слою schedules,
 * поэтому граница между модулями не нарушается.
 *
 * Метод {@code findActiveSchedulesReadyForRun} нарочно читается в отдельной транзакции
 * (readOnly): orchestrator сам управляет тем, что делать с результатом,
 * и не должен держать одну большую транзакцию на весь цикл.
 */
@Component
@RequiredArgsConstructor
public class ScheduleQueryPortImpl implements ScheduleQueryPort {

    private final ScheduleRepositoryPort scheduleRepository;

    /**
     * Возвращает все ACTIVE расписания, у которых {@code next_run_at <= now()}.
     *
     * Выборка делегируется в {@link ScheduleRepositoryPort}, который уже умеет
     * фильтровать по статусу. Фильтр по времени добавляем здесь, чтобы не расширять
     * контракт репозитория ради одного сценария.
     *
     * <p><b>Важно:</b> метод не применяет пессимистическую блокировку — блокировка
     * происходит в момент обновления {@code next_run_at} через {@code advanceNextRun}
     * внутри транзакции Scheduler'а. Это достаточно для MVP с одним экземпляром.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Schedule> findActiveSchedulesReadyForRun() {
        Instant now = Instant.now();

        // Берём только ACTIVE расписания из репозитория,
        // затем фильтруем по времени в памяти — выборка невелика,
        // а дополнительный метод репозитория избыточен для MVP.
        return scheduleRepository.findAllByStatus(ScheduleStatus.ACTIVE).stream()
                .filter(s -> s.getNextRunAt() != null && !s.getNextRunAt().isAfter(now))
                .toList();
    }
}