from __future__ import annotations

import logging
import os
from datetime import datetime, timezone
from pathlib import Path
from typing import Optional

import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import IsolationForest, RandomForestRegressor
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler

logger = logging.getLogger(__name__)

# ── Пути к моделям ────────────────────────────────────────────────────────────

MODELS_DIR = Path(os.getenv("MODELS_DIR", "/app/models"))


def _model_path(model_type: str, org_id: Optional[int] = None) -> Path:
    """
    Путь к файлу модели с версионированием по дате обучения.
    Пример: /app/models/failure_2024-01-15T10:30:00.joblib
    При org_id=None — общая модель.
    """
    prefix = f"org{org_id}_" if org_id else ""
    timestamp = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H-%M-%S")
    return MODELS_DIR / f"{prefix}{model_type}_{timestamp}.joblib"


def _latest_model_path(model_type: str, org_id: Optional[int] = None) -> Optional[Path]:
    """Возвращает путь к самой свежей версии модели (по имени файла)."""
    MODELS_DIR.mkdir(parents=True, exist_ok=True)
    prefix = f"org{org_id}_" if org_id else ""
    pattern = f"{prefix}{model_type}_*.joblib"
    candidates = sorted(MODELS_DIR.glob(pattern), reverse=True)
    return candidates[0] if candidates else None


# ── Feature engineering ───────────────────────────────────────────────────────

def build_features(df: pd.DataFrame) -> pd.DataFrame:
    """
    Строит признаки из сырых строк истории выполнений.

    Входной DataFrame содержит колонки:
        job_id, day, total, succeeded, failed, avg_duration_ms, p95_duration_ms

    Выходные признаки на уровне job_id:
        - error_rate_7d    — средний error rate за последние 7 дней
        - error_rate_30d   — средний error rate за последние 30 дней
        - avg_duration_ms  — средняя длительность
        - p95_duration_ms  — 95-й перцентиль длительности
        - total_runs       — всего запусков
        - days_with_data   — количество дней с данными
        - duration_trend   — наклон тренда длительности (линейная регрессия)
    """
    if df.empty:
        return pd.DataFrame()

    df = df.copy()
    df["day"] = pd.to_datetime(df["day"])
    df["error_rate"] = df["failed"] / df["total"].clip(lower=1)
    df = df.sort_values(["job_id", "day"])

    cutoff_7d  = df["day"].max() - pd.Timedelta(days=7)
    cutoff_30d = df["day"].max() - pd.Timedelta(days=30)

    features = []
    for job_id, group in df.groupby("job_id"):
        g7  = group[group["day"] >= cutoff_7d]
        g30 = group[group["day"] >= cutoff_30d]

        # Тренд длительности — коэффициент линейной регрессии по дням
        duration_trend = 0.0
        valid = group.dropna(subset=["avg_duration_ms"])
        if len(valid) >= 3:
            x = np.arange(len(valid))
            y = valid["avg_duration_ms"].values
            coeffs = np.polyfit(x, y, 1)
            duration_trend = float(coeffs[0])

        features.append({
            "job_id":           job_id,
            "error_rate_7d":    g7["error_rate"].mean()  if not g7.empty  else 0.0,
            "error_rate_30d":   g30["error_rate"].mean() if not g30.empty else 0.0,
            "avg_duration_ms":  group["avg_duration_ms"].mean(),
            "p95_duration_ms":  group["p95_duration_ms"].quantile(0.95)
                                if group["p95_duration_ms"].notna().any() else np.nan,
            "total_runs":       group["total"].sum(),
            "days_with_data":   len(group),
            "duration_trend":   duration_trend,
        })

    result = pd.DataFrame(features).set_index("job_id")
    return result.fillna(0.0)


FAILURE_FEATURES  = ["error_rate_7d", "error_rate_30d", "total_runs", "days_with_data"]
DURATION_FEATURES = ["avg_duration_ms", "p95_duration_ms", "duration_trend", "total_runs"]
ANOMALY_FEATURES  = ["error_rate", "avg_duration_ms"]


# ── Обучение ──────────────────────────────────────────────────────────────────

def train_failure_model(df: pd.DataFrame, org_id: Optional[int] = None) -> Optional[Path]:
    """
    Логистическая регрессия: предсказывает вероятность сбоя задачи.
    Целевая переменная: error_rate_7d > 0.1 → 1 (высокий риск).
    """
    features = build_features(df)
    if features.empty or len(features) < 5:
        logger.warning("[train] not enough data for failure model org_id=%s (%d rows)",
                       org_id, len(features))
        return None

    X = features[FAILURE_FEATURES]
    y = (features["error_rate_7d"] > 0.1).astype(int)

    if y.nunique() < 2:
        logger.warning("[train] only one class in failure labels, skipping org_id=%s", org_id)
        return None

    pipeline = Pipeline([
        ("scaler", StandardScaler()),
        ("model",  LogisticRegression(max_iter=500, random_state=42)),
    ])
    pipeline.fit(X, y)

    path = _model_path("failure", org_id)
    joblib.dump(pipeline, path)
    logger.info("[train] failure model saved to %s", path)
    return path


def train_duration_model(df: pd.DataFrame, org_id: Optional[int] = None) -> Optional[Path]:
    """
    RandomForest: предсказывает ожидаемую длительность выполнения (мс).
    Обучается только на задачах с достаточным количеством данных о длительности.
    """
    features = build_features(df)
    if features.empty:
        return None

    # Фильтруем задачи без данных о длительности
    valid = features[features["avg_duration_ms"] > 0].dropna(subset=["avg_duration_ms"])
    if len(valid) < 5:
        logger.warning("[train] not enough duration data org_id=%s (%d rows)", org_id, len(valid))
        return None

    X = valid[DURATION_FEATURES].fillna(0.0)
    y = valid["avg_duration_ms"]

    model = RandomForestRegressor(
        n_estimators=100,
        max_depth=8,
        random_state=42,
        n_jobs=-1,
    )
    model.fit(X, y)

    path = _model_path("duration", org_id)
    joblib.dump(model, path)
    logger.info("[train] duration model saved to %s", path)
    return path


def train_anomaly_model(df: pd.DataFrame, org_id: Optional[int] = None) -> Optional[Path]:
    """
    IsolationForest: обнаруживает аномалии в метриках выполнений.
    Работает на уровне отдельных дней (не агрегатов по задаче).
    """
    if df.empty:
        return None

    df = df.copy()
    df["error_rate"] = df["failed"] / df["total"].clip(lower=1)
    df["avg_duration_ms"] = df["avg_duration_ms"].fillna(0.0)

    X = df[ANOMALY_FEATURES].fillna(0.0)
    if len(X) < 10:
        logger.warning("[train] not enough rows for anomaly model org_id=%s (%d)", org_id, len(X))
        return None

    model = IsolationForest(
        contamination=0.05,     # ожидаем ~5% аномалий
        random_state=42,
        n_jobs=-1,
    )
    model.fit(X)

    path = _model_path("anomaly", org_id)
    joblib.dump(model, path)
    logger.info("[train] anomaly model saved to %s", path)
    return path


def train_all(df: pd.DataFrame, org_id: Optional[int] = None) -> dict[str, Optional[Path]]:
    """Обучает все три модели на одном DataFrame."""
    logger.info("[train] starting full training org_id=%s rows=%d", org_id, len(df))
    return {
        "failure":  train_failure_model(df, org_id),
        "duration": train_duration_model(df, org_id),
        "anomaly":  train_anomaly_model(df, org_id),
    }


def load_model(model_type: str, org_id: Optional[int] = None):
    """Загружает последнюю версию модели. Возвращает None если модели нет."""
    path = _latest_model_path(model_type, org_id)
    if path is None:
        logger.warning("[load] no model found type=%s org_id=%s", model_type, org_id)
        return None
    model = joblib.load(path)
    logger.info("[load] loaded model type=%s from %s", model_type, path)
    return model