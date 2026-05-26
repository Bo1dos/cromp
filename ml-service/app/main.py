from __future__ import annotations

import logging
import os
from datetime import datetime, timezone

from fastapi import FastAPI, HTTPException, Query
from fastapi.responses import JSONResponse

from app.models import (
    AnomalyRequest,
    AnomalyResponse,
    HealthResponse,
    PredictionRequest,
    PredictionResponse,
    RetrainResponse,
)
from app.ml.predict import Predictor

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
)
logger = logging.getLogger(__name__)

# ── App ───────────────────────────────────────────────────────────────────────

app = FastAPI(
    title="CronMP ML Service",
    description="Прогнозы и обнаружение аномалий для Cron as a Service",
    version="1.0.0",
)

# Глобальный predictor — загружает модели при старте
predictor = Predictor()


@app.on_event("startup")
async def startup() -> None:
    logger.info("Loading ML models...")
    predictor.load_models()
    logger.info("ML service started")


# ── Endpoints ─────────────────────────────────────────────────────────────────

@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(
        status="ok",
        models_loaded=predictor.models_loaded,
        timestamp=datetime.now(timezone.utc).isoformat(),
    )


@app.post("/predictions", response_model=PredictionResponse)
def predictions(request: PredictionRequest) -> PredictionResponse:
    """
    Прогнозирует вероятность сбоя и ожидаемую длительность для задач.
    Принимает историю выполнений от Java-бэкенда.
    """
    logger.info("Predictions request org_id=%s job_id=%s history_rows=%d",
                request.org_id, request.job_id, len(request.history))

    if not request.history:
        logger.warning("Empty history for org_id=%s, returning empty predictions", request.org_id)
        return PredictionResponse(predictions=[])

    try:
        result = predictor.predict(request)
        logger.info("Returning %d prediction(s) for org_id=%s", len(result.predictions), request.org_id)
        return result
    except Exception as e:
        logger.error("Prediction failed for org_id=%s: %s", request.org_id, e, exc_info=True)
        # Деградированный ответ — не 500, возвращаем пустой список
        return PredictionResponse(predictions=[])


@app.post("/anomalies", response_model=AnomalyResponse)
def anomalies(request: AnomalyRequest) -> AnomalyResponse:
    """
    Обнаруживает аномалии в метриках выполнений за период.
    """
    logger.info("Anomaly request org_id=%s job_id=%s from=%s to=%s history_rows=%d",
                request.org_id, request.job_id, request.from_, request.to, len(request.history))

    if not request.history:
        logger.warning("Empty history for org_id=%s, returning empty anomalies", request.org_id)
        return AnomalyResponse(anomalies=[])

    try:
        result = predictor.detect_anomalies(request)
        logger.info("Returning %d anomal(ies) for org_id=%s", len(result.anomalies), request.org_id)
        return result
    except Exception as e:
        logger.error("Anomaly detection failed for org_id=%s: %s", request.org_id, e, exc_info=True)
        return AnomalyResponse(anomalies=[])


@app.post("/retrain", response_model=RetrainResponse)
def retrain(org_id: int = Query(None, description="ID организации. Если не указан — переобучаем для всех")) -> RetrainResponse:
    """
    Ручной запуск переобучения моделей.
    Для автоматического — используется Celery Beat.
    """
    from app.tasks.tasks import retrain_models_task

    logger.info("Manual retrain triggered for org_id=%s", org_id)
    retrain_models_task.delay(org_id=org_id)

    return RetrainResponse(
        status="scheduled",
        org_id=org_id,
        message=f"Retrain task scheduled for org_id={org_id or 'all'}",
    )


# ── Error handlers ────────────────────────────────────────────────────────────

@app.exception_handler(Exception)
async def global_exception_handler(request, exc):
    logger.error("Unhandled exception: %s", exc, exc_info=True)
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal server error"},
    )