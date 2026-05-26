from __future__ import annotations

import os

from celery import Celery
from celery.schedules import crontab

# ── Broker / Backend ──────────────────────────────────────────────────────────

REDIS_URL = os.getenv("REDIS_URL", "redis://localhost:6379/0")

celery_app = Celery(
    "ml_service",
    broker=REDIS_URL,
    backend=REDIS_URL,
    include=["app.tasks.tasks"],
)

# ── Конфигурация ──────────────────────────────────────────────────────────────

celery_app.conf.update(
    # Сериализация
    task_serializer="json",
    result_serializer="json",
    accept_content=["json"],

    # Временная зона
    timezone="UTC",
    enable_utc=True,

    # Результаты задач храним 1 час — нам не нужна долгосрочная история
    result_expires=3600,

    # Воркер не берёт больше 1 задачи за раз — обучение тяжёлое
    worker_prefetch_multiplier=1,
    task_acks_late=True,        # подтверждаем задачу только после выполнения

    # Лимит памяти: перезапускаем воркер после N задач (защита от утечек)
    worker_max_tasks_per_child=10,
)

# ── Beat расписание ───────────────────────────────────────────────────────────

celery_app.conf.beat_schedule = {
    # Переобучение раз в сутки в 03:00 UTC — минимальная нагрузка на БД
    "retrain-models-daily": {
        "task": "app.tasks.tasks.retrain_models_task",
        "schedule": crontab(hour=3, minute=0),
        "kwargs": {"org_id": None},     # None — переобучаем для всех организаций
    },
}