package com.cromp.executions.application.port;

import com.cromp.executions.domain.model.ExecutionAttempt;

import java.util.List;

/**
 * Порт для orchestrator'а: поиск и завершение зависших попыток.
 *
 * Реализация живёт в {@code executions/infrastructure/port/StaleAttemptPortImpl}.
 */
public interface StaleAttemptPort {

    /**
     * Возвращает все попытки в статусе RUNNING, у которых
     * {@code updated_at < now() - olderThanMinutes}.
     *
     * @param olderThanMinutes порог в минутах (например, 5)
     * @return список зависших попыток
     */
    List<ExecutionAttempt> findStaleAttempts(int olderThanMinutes);

    /**
     * Переводит каждую попытку из списка в статус TIMEOUT и сохраняет.
     * Триггер БД {@code fn_update_execution_final_status} автоматически
     * обновит {@code Execution.finalStatus = FAILED}.
     *
     * @param stale список зависших попыток, возвращённых {@link #findStaleAttempts}
     */
    void markStaleAsTimeout(List<ExecutionAttempt> stale);
}
