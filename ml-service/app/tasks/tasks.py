from __future__ import annotations

import logging
from typing import Optional

from celery import shared_task

from app.db import fetch_all_org_ids, fetch_execution_history
from app.ml.train import train_all
from app.tasks.celery import celery_app

logger = logging.getLogger(__name__)


@celery_app.task(
    bind=True,
    name="app.tasks.tasks.retrain_models_task",
    max_retries=3,
    default_retry_delay=300,    # повтор через 5 минут при ошибке
    soft_time_limit=1800,       # 30 минут — мягкий лимит (SoftTimeLimitExceeded)
    time_limit=2100,            # 35 минут — жёсткий лимит (принудительное завершение)
)
def retrain_models_task(self, org_id: Optional[int] = None) -> dict:
    """
    Переобучает ML-модели на исторических данных из PostgreSQL.

    Если org_id=None — переобучает общую модель на данных всех организаций.
    Если org_id задан — переобучает только для конкретной организации
    (используется при ручном запуске через POST /retrain?orgId=...).

    После успешного обучения перезагружает модели в Predictor'е.
    """
    logger.info("[retrain] task started org_id=%s", org_id)

    try:
        if org_id is not None:
            result = _retrain_for_org(org_id)
        else:
            result = _retrain_global()

        # Перезагружаем модели в памяти после обучения
        _reload_predictor()

        logger.info("[retrain] task completed org_id=%s result=%s", org_id, result)
        return {"status": "success", "org_id": org_id, "models": result}

    except Exception as exc:
        logger.error("[retrain] task failed org_id=%s: %s", org_id, exc, exc_info=True)
        # Celery повторит задачу до max_retries раз
        raise self.retry(exc=exc)


# ── Private ───────────────────────────────────────────────────────────────────

def _retrain_global() -> dict:
    """
    Обучает общую модель на данных всех организаций.
    Для MVP это самый простой подход — одна модель на всех.
    """
    import pandas as pd

    logger.info("[retrain] fetching data for all organizations")
    org_ids = fetch_all_org_ids()

    if not org_ids:
        logger.warning("[retrain] no organizations found in mv_job_execution_daily")
        return {}

    # Собираем данные всех организаций в один DataFrame
    frames = []
    for oid in org_ids:
        df = fetch_execution_history(org_id=oid, days=90)
        if not df.empty:
            df["org_id"] = oid
            frames.append(df)

    if not frames:
        logger.warning("[retrain] no execution history found for any organization")
        return {}

    combined = pd.concat(frames, ignore_index=True)
    logger.info("[retrain] combined dataset: %d rows from %d org(s)",
                len(combined), len(frames))

    paths = train_all(combined, org_id=None)
    return {k: str(v) for k, v in paths.items() if v is not None}


def _retrain_for_org(org_id: int) -> dict:
    """Обучает модель на данных конкретной организации."""
    logger.info("[retrain] fetching data for org_id=%s", org_id)

    df = fetch_execution_history(org_id=org_id, days=90)
    if df.empty:
        logger.warning("[retrain] no data for org_id=%s", org_id)
        return {}

    logger.info("[retrain] training on %d rows for org_id=%s", len(df), org_id)
    paths = train_all(df, org_id=org_id)
    return {k: str(v) for k, v in paths.items() if v is not None}


def _reload_predictor() -> None:
    """
    Стучится в POST /reload на ml-service API, чтобы перезагрузить модели
    в процессе FastAPI (worker и API — разные процессы/контейнеры).
    Использует urllib из stdlib — не требует дополнительных зависимостей.
    """
    import json
    import urllib.request

    api_url = "http://ml-service:8000/reload"
    try:
        req = urllib.request.Request(api_url, method="POST")
        with urllib.request.urlopen(req, timeout=10) as resp:
            body = json.loads(resp.read().decode())
            logger.info(
                "[retrain] predictor reload response: status=%s models_loaded=%s",
                body.get("status"), body.get("models_loaded"),
            )
    except Exception as e:
        logger.error("[retrain] failed to call reload API at %s: %s", api_url, e)