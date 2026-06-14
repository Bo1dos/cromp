"""Integration tests for ML service REST API endpoints."""
from __future__ import annotations

from unittest.mock import patch

import pytest
from fastapi.testclient import TestClient

from app.main import app
from app.models import HealthResponse

client = TestClient(app)


class TestHealthEndpoint:
    def test_health_returns_ok(self):
        response = client.get("/health")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "ok"
        assert "models_loaded" in data
        assert "timestamp" in data

    def test_health_models_loaded_field_is_boolean(self):
        response = client.get("/health")
        data = response.json()
        assert isinstance(data["models_loaded"], bool)


class TestPredictionsEndpoint:
    def test_predictions_with_empty_history(self):
        payload = {
            "org_id": 1,
            "job_id": 1,
            "history": []
        }
        response = client.post("/predictions", json=payload)
        assert response.status_code == 200
        data = response.json()
        assert data["predictions"] == []

    def test_predictions_with_history(self):
        payload = {
            "org_id": 1,
            "job_id": 1,
            "history": [
                {
                    "job_id": 1,
                    "execution_id": 1001,
                    "finished_at": "2024-01-01T10:00:00Z",
                    "duration_ms": 500,
                    "status": "SUCCEEDED",
                    "error_class": None,
                    "attempt_number": 1
                },
                {
                    "job_id": 1,
                    "execution_id": 1002,
                    "finished_at": "2024-01-02T10:00:00Z",
                    "duration_ms": 550,
                    "status": "SUCCEEDED",
                    "error_class": None,
                    "attempt_number": 1
                },
                {
                    "job_id": 1,
                    "execution_id": 1003,
                    "finished_at": "2024-01-03T10:00:00Z",
                    "duration_ms": 5000,
                    "status": "FAILED",
                    "error_class": "5xx",
                    "attempt_number": 1
                }
            ]
        }
        response = client.post("/predictions", json=payload)
        assert response.status_code == 200
        data = response.json()
        assert "predictions" in data
        preds = data["predictions"]
        assert isinstance(preds, list)
        if preds:
            pred = preds[0]
            assert "job_id" in pred
            assert "failure_probability" in pred
            assert 0.0 <= pred["failure_probability"] <= 1.0


class TestAnomaliesEndpoint:
    def test_anomalies_with_empty_history(self):
        payload = {
            "org_id": 1,
            "job_id": 1,
            "from_": "2024-01-01T00:00:00Z",
            "to": "2024-02-01T00:00:00Z",
            "history": []
        }
        response = client.post("/anomalies", json=payload)
        assert response.status_code == 200
        data = response.json()
        assert data["anomalies"] == []

    def test_anomalies_with_history(self):
        payload = {
            "org_id": 1,
            "job_id": 1,
            "from_": "2024-01-01T00:00:00Z",
            "to": "2024-02-01T00:00:00Z",
            "history": [
                {
                    "job_id": 1, "execution_id": 1001,
                    "finished_at": "2024-01-15T10:00:00Z",
                    "duration_ms": 500, "status": "SUCCEEDED",
                    "error_class": None, "attempt_number": 1
                },
                {
                    "job_id": 1, "execution_id": 1002,
                    "finished_at": "2024-01-15T11:00:00Z",
                    "duration_ms": 30000, "status": "FAILED",
                    "error_class": "TIMEOUT", "attempt_number": 1
                }
            ]
        }
        response = client.post("/anomalies", json=payload)
        assert response.status_code == 200
        data = response.json()
        assert "anomalies" in data
        assert isinstance(data["anomalies"], list)


class TestRetrainEndpoint:
    def test_retrain_returns_accepted(self):
        with patch("app.ml.train.train_all_models") as mock_train:
            mock_train.return_value = {
                "failure": True,
                "duration": True,
                "anomaly": True
            }
            response = client.post("/retrain")
            assert response.status_code == 200
            data = response.json()
            assert "models" in data


class TestValidation:
    def test_predictions_rejects_missing_fields(self):
        payload = {"org_id": 1}  # нет job_id и history
        response = client.post("/predictions", json=payload)
        assert response.status_code == 422

    def test_anomalies_rejects_invalid_date(self):
        payload = {
            "org_id": 1, "job_id": 1,
            "from_": "not-a-date",
            "to": "2024-02-01T00:00:00Z",
            "history": []
        }
        response = client.post("/anomalies", json=payload)
        assert response.status_code == 422
