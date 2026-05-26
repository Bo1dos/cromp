from __future__ import annotations

import logging
from datetime import datetime, timezone
from typing import Optional

import numpy as np
import pandas as pd

from app.ml.train import (
    ANOMALY_FEATURES,
    DURATION_FEATURES,
    FAILURE_FEATURES,
    build_features,
    load_model,
)
from app.models import (
    AnomalyItem,
    AnomalyRequest,
    AnomalyResponse,
    PredictionItem,
    PredictionRequest,
    PredictionResponse,
    Severity,
)

logger = logging.getLogger(__name__)


class Predictor:
    """
    Инкапсулирует загрузку моделей и инференс.

    Загружает общие модели (без org_id) при старте.
    Все методы безопасны при отсутствии модели — возвращают
    статистические fallback-значения вместо исключений.
    """

    def __init__(self) -> None:
        self._failure_model  = None
        self._duration_model = None
        self._anomaly_model  = None
        self.models_loaded   = False

    def load_models(self) -> None:
        """Загружает последние версии всех моделей с диска."""
        self._failure_model  = load_model("failure")
        self._duration_model = load_model("duration")
        self._anomaly_model  = load_model("anomaly")
        self.models_loaded   = any([
            self._failure_model,
            self._duration_model,
            self._anomaly_model,
        ])
        logger.info(
            "Models loaded: failure=%s duration=%s anomaly=%s",
            self._failure_model  is not None,
            self._duration_model is not None,
            self._anomaly_model  is not None,
        )

    def reload_models(self) -> None:
        """Перезагружает модели после переобучения."""
        logger.info("Reloading models after retrain")
        self.load_models()

    # ── Predictions ───────────────────────────────────────────────────────────

    def predict(self, request: PredictionRequest) -> PredictionResponse:
        df = _history_to_df(request.history)
        features = build_features(df)

        if features.empty:
            return PredictionResponse(predictions=[])

        items = []
        for job_id in features.index:
            row = features.loc[[job_id]]

            failure_prob = self._predict_failure(row)
            expected_ms  = self._predict_duration(row)
            confidence   = self._compute_confidence(row)

            items.append(PredictionItem(
                job_id=int(job_id),
                next_run_at=None,       # расписание не передаётся в ML, Java знает сам
                failure_probability=round(failure_prob, 4),
                expected_duration_ms=int(expected_ms) if expected_ms is not None else None,
                confidence=round(confidence, 4),
            ))

        return PredictionResponse(predictions=items)

    def _predict_failure(self, row: pd.DataFrame) -> float:
        """
        Возвращает вероятность сбоя [0.0 .. 1.0].
        Fallback: берём error_rate_7d как грубую оценку.
        """
        if self._failure_model is not None:
            try:
                X = row[FAILURE_FEATURES].fillna(0.0)
                prob = self._failure_model.predict_proba(X)[0][1]
                return float(prob)
            except Exception as e:
                logger.warning("Failure model inference failed: %s", e)

        # Статистический fallback
        return float(row["error_rate_7d"].iloc[0])

    def _predict_duration(self, row: pd.DataFrame) -> Optional[float]:
        """
        Возвращает ожидаемую длительность (мс).
        Fallback: средняя длительность из истории.
        """
        if self._duration_model is not None:
            try:
                X = row[DURATION_FEATURES].fillna(0.0)
                return float(self._duration_model.predict(X)[0])
            except Exception as e:
                logger.warning("Duration model inference failed: %s", e)

        # Статистический fallback
        avg = row["avg_duration_ms"].iloc[0]
        return float(avg) if avg > 0 else None

    def _compute_confidence(self, row: pd.DataFrame) -> float:
        """
        Уверенность [0.0 .. 1.0] — растёт с количеством дней с данными.
        Максимум при 30+ днях.
        """
        days = float(row["days_with_data"].iloc[0])
        return min(days / 30.0, 1.0)

    # ── Anomalies ─────────────────────────────────────────────────────────────

    def detect_anomalies(self, request: AnomalyRequest) -> AnomalyResponse:
        df = _history_to_df(request.history)
        if df.empty:
            return AnomalyResponse(anomalies=[])

        df["error_rate"]    = df["failed"] / df["total"].clip(lower=1)
        df["avg_duration_ms"] = df["avg_duration_ms"].fillna(0.0)

        # Фильтруем по диапазону дат из запроса
        df["day"] = pd.to_datetime(df["day"])
        try:
            df = df[
                (df["day"] >= pd.Timestamp(request.from_)) &
                (df["day"] <= pd.Timestamp(request.to))
            ]
        except Exception:
            pass  # если даты кривые — не фильтруем

        if df.empty:
            return AnomalyResponse(anomalies=[])

        if self._anomaly_model is not None:
            return self._detect_with_model(df)
        else:
            return self._detect_with_statistics(df)

    def _detect_with_model(self, df: pd.DataFrame) -> AnomalyResponse:
        """Используем IsolationForest для обнаружения аномалий."""
        X = df[ANOMALY_FEATURES].fillna(0.0)
        try:
            predictions = self._anomaly_model.predict(X)   # -1 = аномалия, 1 = норма
            scores      = self._anomaly_model.decision_function(X)
        except Exception as e:
            logger.warning("Anomaly model inference failed: %s, falling back to stats", e)
            return self._detect_with_statistics(df)

        anomalies = []
        for i, (pred, score) in enumerate(zip(predictions, scores)):
            if pred == 1:   # норма
                continue

            row = df.iloc[i]
            severity = _score_to_severity(score)

            # Строим expected range из квантилей всей выборки
            for metric in ANOMALY_FEATURES:
                value = float(row[metric])
                col   = df[metric]
                q1, q3 = col.quantile(0.25), col.quantile(0.75)

                anomalies.append(AnomalyItem(
                    job_id=int(row["job_id"]),
                    metric=metric,
                    value=round(value, 4),
                    expected_min=round(float(q1), 4),
                    expected_max=round(float(q3), 4),
                    severity=severity,
                    detected_at=row["day"].isoformat(),
                ))

        return AnomalyResponse(anomalies=anomalies)

    def _detect_with_statistics(self, df: pd.DataFrame) -> AnomalyResponse:
        """
        Статистический fallback: IQR-метод.
        Точки за пределами [Q1 - 1.5*IQR, Q3 + 1.5*IQR] — аномалии.
        """
        anomalies = []

        for metric in ANOMALY_FEATURES:
            col = df[metric].dropna()
            if col.empty:
                continue

            q1, q3 = col.quantile(0.25), col.quantile(0.75)
            iqr    = q3 - q1
            lower  = q1 - 1.5 * iqr
            upper  = q3 + 1.5 * iqr

            outliers = df[(df[metric] < lower) | (df[metric] > upper)]
            for _, row in outliers.iterrows():
                value    = float(row[metric])
                distance = max(abs(value - lower), abs(value - upper)) / (iqr + 1e-9)
                severity = _distance_to_severity(distance)

                anomalies.append(AnomalyItem(
                    job_id=int(row["job_id"]),
                    metric=metric,
                    value=round(value, 4),
                    expected_min=round(float(lower), 4),
                    expected_max=round(float(upper), 4),
                    severity=severity,
                    detected_at=row["day"].isoformat(),
                ))

        return AnomalyResponse(anomalies=anomalies)


# ── Helpers ───────────────────────────────────────────────────────────────────

def _history_to_df(history) -> pd.DataFrame:
    """Конвертирует список ExecutionRow из запроса в DataFrame."""
    if not history:
        return pd.DataFrame()
    return pd.DataFrame([row.model_dump() for row in history])


def _score_to_severity(score: float) -> Severity:
    """
    IsolationForest decision_function: отрицательные значения — аномалии.
    Чем меньше score, тем сильнее аномалия.
    """
    if score < -0.3:
        return Severity.CRITICAL
    elif score < -0.2:
        return Severity.HIGH
    elif score < -0.1:
        return Severity.MEDIUM
    return Severity.LOW


def _distance_to_severity(distance: float) -> Severity:
    """IQR-дистанция → severity для статистического метода."""
    if distance > 5.0:
        return Severity.CRITICAL
    elif distance > 3.0:
        return Severity.HIGH
    elif distance > 2.0:
        return Severity.MEDIUM
    return Severity.LOW