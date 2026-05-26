from __future__ import annotations

import os
import logging
from contextlib import contextmanager
from typing import Generator, Optional

import pandas as pd
from sqlalchemy import create_engine, text
from sqlalchemy.engine import Engine

logger = logging.getLogger(__name__)

# ── Engine ────────────────────────────────────────────────────────────────────

def _build_url() -> str:
    """Строит DATABASE_URL из env-переменных."""
    url = os.getenv("DATABASE_URL")
    if url:
        return url

    host     = os.getenv("DB_HOST", "localhost")
    port     = os.getenv("DB_PORT", "5432")
    name     = os.getenv("DB_NAME", "cromp")
    user     = os.getenv("DB_USER", "cromp")
    password = os.getenv("DB_PASSWORD", "cromp")
    return f"postgresql+psycopg2://{user}:{password}@{host}:{port}/{name}"


_engine: Optional[Engine] = None


def get_engine() -> Engine:
    global _engine
    if _engine is None:
        _engine = create_engine(
            _build_url(),
            pool_size=5,
            max_overflow=10,
            pool_pre_ping=True,     # проверяем соединение перед использованием
            echo=False,
        )
        logger.info("Database engine created")
    return _engine


@contextmanager
def get_connection() -> Generator:
    engine = get_engine()
    with engine.connect() as conn:
        yield conn


# ── Queries ───────────────────────────────────────────────────────────────────

def fetch_execution_history(
    org_id: int,
    job_id: Optional[int] = None,
    days: int = 90,
) -> pd.DataFrame:
    """
    Читает историю выполнений из mv_job_execution_daily.
    Используется для переобучения моделей — когда Java не передал историю
    в запросе (например, при ручном retrain через Celery).
    """
    sql = """
        SELECT
            job_id,
            to_char(day, 'YYYY-MM-DD')  AS day,
            total_executions            AS total,
            succeeded,
            failed,
            avg_attempt_duration_ms     AS avg_duration_ms,
            p95_duration_ms
        FROM mv_job_execution_daily
        WHERE organization_id = :org_id
          AND day >= now() - (:days * INTERVAL '1 day')
        {job_filter}
        ORDER BY job_id, day
    """.format(
        job_filter="AND job_id = :job_id" if job_id else ""
    )

    params: dict = {"org_id": org_id, "days": days}
    if job_id:
        params["job_id"] = job_id

    try:
        with get_connection() as conn:
            df = pd.read_sql(text(sql), conn, params=params)
        logger.debug("Fetched %d rows for org_id=%s job_id=%s", len(df), org_id, job_id)
        return df
    except Exception as e:
        logger.error("Failed to fetch execution history: %s", e)
        return pd.DataFrame()


def fetch_all_org_ids() -> list[int]:
    """Возвращает список всех organization_id из MV — для batch-переобучения."""
    sql = "SELECT DISTINCT organization_id FROM mv_job_execution_daily ORDER BY organization_id"
    try:
        with get_connection() as conn:
            result = conn.execute(text(sql))
            return [row[0] for row in result]
    except Exception as e:
        logger.error("Failed to fetch org ids: %s", e)
        return []