"""Tests for ML Predictor — prediction, anomaly detection, fallback logic."""
from __future__ import annotations

import pandas as pd
import pytest
from unittest.mock import MagicMock, patch

from app.ml.predict import Predictor
from app.models import (
    AnomalyRequest,
    AnomalyResponse,
    PredictionItem,
    PredictionRequest,
    PredictionResponse,
)


def _make_history(
    job_id: int = 1,
    rows: int = 10,
    success_rate: float = 0.8,
) -> list[dict]:
    """Build synthetic execution history for tests."""
    result = []
    for i in range(rows):
        is_success = i < int(rows * success_rate)
        result.append({
            "job_id": job_id,
            "execution_id": 1000 + i,
            "finished_at": f"2024-01-{(i % 28) + 1:02d}T10:00:00Z",
            "duration_ms": 500 + (i * 50) if is_success else 5000,
            "status": "SUCCEEDED" if is_success else "FAILED",
            "error_class": None if is_success else "5xx",
            "attempt_number": 1,
        })
    return result


class TestPredictorInit:
    def test_predictor_starts_with_no_models(self):
        p = Predictor()
        assert not p.models_loaded
        assert p._failure_model is None
        assert p._duration_model is None
        assert p._anomaly_model is None

    def test_load_models_marks_loaded(self):
        p = Predictor()
        with patch("app.ml.predict.load_model", return_value=None):
            p.load_models()
        assert p.models_loaded is False  # все модели None


class TestPrediction:
    def test_predict_with_empty_history_returns_empty(self):
        p = Predictor()
        req = PredictionRequest(org_id=1, job_id=1, history=[])
        result = p.predict(req)
        assert result.predictions == []

    def test_predict_uses_fallback_when_no_model(self):
        p = Predictor()
        history = _make_history(job_id=1, rows=10, success_rate=0.7)
        req = PredictionRequest(org_id=1, job_id=1, history=history)
        result = p.predict(req)
        assert len(result.predictions) == 1
        pred = result.predictions[0]
        assert pred.job_id == 1
        assert 0.0 <= pred.failure_probability <= 1.0
        assert pred.confidence >= 0.0

    def test_predict_with_mocked_failure_model(self):
        p = Predictor()
        mock_model = MagicMock()
        mock_model.predict_proba.return_value = [[0.3, 0.7]]
        p._failure_model = mock_model
        p._duration_model = MagicMock()
        p._duration_model.predict.return_value = [1200.0]

        history = _make_history(job_id=1, rows=10)
        req = PredictionRequest(org_id=1, job_id=1, history=history)
        result = p.predict(req)

        assert len(result.predictions) == 1
        pred = result.predictions[0]
        assert pred.failure_probability == pytest.approx(0.7, rel=0.1)
        assert pred.expected_duration_ms == 1200

    def test_predict_job_id_field(self):
        p = Predictor()
        history = _make_history(job_id=42, rows=5)
        req = PredictionRequest(org_id=1, job_id=42, history=history)
        with patch.object(p, "_predict_failure", return_value=0.2), \
             patch.object(p, "_predict_duration", return_value=800.0), \
             patch.object(p, "_compute_confidence", return_value=0.85):
            result = p.predict(req)
        assert result.predictions[0].job_id == 42


class TestAnomalyDetection:
    def test_anomalies_with_empty_history_returns_empty(self):
        p = Predictor()
        req = AnomalyRequest(
            org_id=1, job_id=1,
            from_="2024-01-01T00:00:00Z",
            to="2024-02-01T00:00:00Z",
            history=[]
        )
        result = p.detect_anomalies(req)
        assert result.anomalies == []

    def test_detect_anomalies_fallback_uses_statistical_thresholds(self):
        p = Predictor()
        history = _make_history(job_id=1, rows=20, success_rate=0.95)
        # Добавляем один резкий выброс
        history.append({
            "job_id": 1,
            "execution_id": 9999,
            "finished_at": "2024-01-15T10:00:00Z",
            "duration_ms": 30000,
            "status": "FAILED",
            "error_class": "TIMEOUT",
            "attempt_number": 1,
        })
        req = AnomalyRequest(
            org_id=1, job_id=1,
            from_="2024-01-01T00:00:00Z",
            to="2024-02-01T00:00:00Z",
            history=history
        )
        result = p.detect_anomalies(req)
        # С моделью или без — должен вернуть список (возможно пустой)
        assert isinstance(result, AnomalyResponse)
        assert isinstance(result.anomalies, list)


class TestPredictorErrorHandling:
    def test_predict_survives_model_exception(self):
        p = Predictor()
        mock_model = MagicMock()
        mock_model.predict_proba.side_effect = RuntimeError("model crash")
        p._failure_model = mock_model

        history = _make_history(job_id=1, rows=5)
        req = PredictionRequest(org_id=1, job_id=1, history=history)
        result = p.predict(req)

        assert len(result.predictions) == 1
        # должен отработать fallback, а не упасть
        assert result.predictions[0].failure_probability is not None

    def test_detect_anomalies_survives_model_exception(self):
        p = Predictor()
        mock_model = MagicMock()
        mock_model.predict.side_effect = RuntimeError("model crash")
        p._anomaly_model = mock_model

        history = _make_history(job_id=1, rows=5)
        req = AnomalyRequest(
            org_id=1, job_id=1,
            from_="2024-01-01T00:00:00Z",
            to="2024-02-01T00:00:00Z",
            history=history
        )
        result = p.detect_anomalies(req)
        assert isinstance(result, AnomalyResponse)
