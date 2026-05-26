from __future__ import annotations

from datetime import datetime
from enum import Enum
from typing import Optional

from pydantic import BaseModel, Field


# ── Shared ────────────────────────────────────────────────────────────────────

class ExecutionRow(BaseModel):
    """Одна строка истории выполнений (один день из mv_job_execution_daily)."""
    job_id: int
    day: str                        # ISO date: "2024-01-15"
    total: int
    succeeded: int
    failed: int
    avg_duration_ms: Optional[float] = None
    p95_duration_ms: Optional[float] = None


# ── Predictions ───────────────────────────────────────────────────────────────

class PredictionRequest(BaseModel):
    org_id: int
    job_id: Optional[int] = None    # None — прогноз для всех задач организации
    history: list[ExecutionRow] = Field(default_factory=list)


class PredictionItem(BaseModel):
    job_id: int
    next_run_at: Optional[str] = None       # ISO-8601, если известно
    failure_probability: float = Field(ge=0.0, le=1.0)
    expected_duration_ms: Optional[int] = None
    confidence: float = Field(ge=0.0, le=1.0)


class PredictionResponse(BaseModel):
    predictions: list[PredictionItem] = Field(default_factory=list)


# ── Anomalies ─────────────────────────────────────────────────────────────────

class AnomalyRequest(BaseModel):
    org_id: int
    job_id: Optional[int] = None
    from_: str = Field(alias="from")        # ISO date: "2024-01-01"
    to: str                                  # ISO date: "2024-01-31"
    history: list[ExecutionRow] = Field(default_factory=list)

    model_config = {"populate_by_name": True}


class Severity(str, Enum):
    LOW      = "LOW"
    MEDIUM   = "MEDIUM"
    HIGH     = "HIGH"
    CRITICAL = "CRITICAL"


class ExpectedRange(BaseModel):
    min: float
    max: float


class AnomalyItem(BaseModel):
    job_id: int
    metric: str                     # "duration_ms", "error_rate"
    value: float
    expected_min: float
    expected_max: float
    severity: Severity
    detected_at: str                # ISO-8601


class AnomalyResponse(BaseModel):
    anomalies: list[AnomalyItem] = Field(default_factory=list)


# ── Retrain ───────────────────────────────────────────────────────────────────

class RetrainResponse(BaseModel):
    status: str
    org_id: Optional[int] = None
    message: str


# ── Health ────────────────────────────────────────────────────────────────────

class HealthResponse(BaseModel):
    status: str
    models_loaded: bool
    timestamp: str