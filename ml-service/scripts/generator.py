"""
Генератор синтетических данных для обучения ML-моделей.

Заполняет mv_job_execution_daily строками с реалистичными профилями задач.

Использование:
    python scripts/generate_data.py --output csv
    python scripts/generate_data.py --output postgres
    python scripts/generate_data.py --orgs 5 --jobs-per-org 20 --days 90 --seed 42
"""
from __future__ import annotations

import argparse
import csv
import os
import sys
from dataclasses import dataclass
from datetime import date, timedelta
from pathlib import Path
from typing import Iterator

import numpy as np

# ── Профили задач ─────────────────────────────────────────────────────────────

@dataclass
class DayRow:
    """Одна строка mv_job_execution_daily. Все инварианты проверяются в __post_init__."""
    job_id:                  int
    organization_id:         int
    day:                     date
    total_executions:        int
    succeeded:               int
    failed:                  int
    avg_attempt_duration_ms: float
    p50_duration_ms:         float
    p95_duration_ms:         float
    max_duration_ms:         float
    total_attempts:          int

    def __post_init__(self) -> None:
        # Инварианты схемы — нарушение = баг в генераторе
        assert self.failed >= 0,                           f"failed < 0: {self}"
        assert self.succeeded >= 0,                        f"succeeded < 0: {self}"
        assert self.succeeded + self.failed <= self.total_executions, \
            f"succeeded+failed > total: {self}"
        assert self.total_attempts >= self.total_executions, \
            f"attempts < executions: {self}"
        assert self.p50_duration_ms <= self.p95_duration_ms, \
            f"p50 > p95: {self}"
        assert self.p95_duration_ms <= self.max_duration_ms, \
            f"p95 > max: {self}"
        assert self.avg_attempt_duration_ms >= 1.0,        f"avg < 1: {self}"


def _clamp(value: float, lo: float, hi: float) -> float:
    return max(lo, min(hi, value))


def _durations_from_samples(samples: np.ndarray) -> tuple[float, float, float, float]:
    """Вычисляет avg, p50, p95, max из набора семплов длительностей."""
    samples = np.clip(samples, 1.0, None)   # duration_ms >= 1
    return (
        float(np.mean(samples)),
        float(np.percentile(samples, 50)),
        float(np.percentile(samples, 95)),
        float(np.max(samples)),
    )


def _make_row(
    rng: np.random.Generator,
    job_id: int,
    org_id: int,
    day: date,
    total: int,
    error_rate: float,
    duration_samples: np.ndarray,
    retry_rate: float = 0.05,
) -> DayRow:
    """
    Собирает DayRow из параметров дня.
    retry_rate — доля запусков с дополнительной попыткой (retry).
    """
    error_rate   = _clamp(error_rate, 0.0, 1.0)
    total        = max(1, total)

    failed       = int(round(total * error_rate))
    succeeded    = total - failed
    # Не бывает отрицательных
    failed       = max(0, failed)
    succeeded    = max(0, succeeded)
    # Поправка если округление дало succeeded+failed > total
    if succeeded + failed > total:
        succeeded = total - failed

    # Попытки = запуски + retry у failed
    retries      = int(round(failed * retry_rate))
    total_attempts = total + retries

    avg, p50, p95, mx = _durations_from_samples(duration_samples)

    return DayRow(
        job_id=job_id,
        organization_id=org_id,
        day=day,
        total_executions=total,
        succeeded=succeeded,
        failed=failed,
        avg_attempt_duration_ms=round(avg, 2),
        p50_duration_ms=round(p50, 2),
        p95_duration_ms=round(p95, 2),
        max_duration_ms=round(mx, 2),
        total_attempts=total_attempts,
    )


# ── Профили ───────────────────────────────────────────────────────────────────

def profile_stable(
    rng: np.random.Generator,
    job_id: int,
    org_id: int,
    days: list[date],
) -> list[DayRow]:
    """
    Стабильная задача: низкий error rate, предсказуемая длительность.
    Типичный HTTP-вебхук: уведомления, синхронизация.
    """
    rows = []
    for day in days:
        total    = int(rng.integers(10, 51))
        err_rate = _clamp(rng.normal(0.02, 0.01), 0.0, 0.08)
        n        = max(total, 30)
        samples  = rng.normal(loc=400, scale=80, size=n).clip(50, 1500)
        rows.append(_make_row(rng, job_id, org_id, day, total, err_rate, samples))
    return rows


def profile_degrading(
    rng: np.random.Generator,
    job_id: int,
    org_id: int,
    days: list[date],
) -> list[DayRow]:
    """
    Деградирующая задача: длительность и error rate растут линейно.
    Симулирует утечку памяти или разрастание БД.
    """
    n_days = len(days)
    rows   = []
    for i, day in enumerate(days):
        progress = i / max(n_days - 1, 1)      # 0.0 → 1.0

        total    = int(rng.integers(20, 101))
        err_rate = _clamp(rng.normal(0.02 + 0.18 * progress, 0.02), 0.0, 0.95)

        base_dur = 300 + 2700 * progress        # 300ms → 3000ms
        samples  = rng.normal(loc=base_dur, scale=base_dur * 0.15,
                               size=max(total, 30)).clip(50, 30000)
        rows.append(_make_row(rng, job_id, org_id, day, total, err_rate, samples,
                               retry_rate=0.1))
    return rows


def profile_spiky(
    rng: np.random.Generator,
    job_id: int,
    org_id: int,
    days: list[date],
) -> list[DayRow]:
    """
    Задача с выбросами: обычно быстрая, раз в ~7-10 дней резкий spike.
    Симулирует зависимость от нестабильного внешнего API.
    """
    rows = []
    for i, day in enumerate(days):
        total     = int(rng.integers(5, 31))
        is_spike  = (i % int(rng.integers(7, 11))) == 0

        if is_spike:
            err_rate = _clamp(rng.normal(0.45, 0.10), 0.25, 0.75)
            samples  = rng.normal(loc=15000, scale=5000,
                                   size=max(total, 30)).clip(5000, 60000)
        else:
            err_rate = _clamp(rng.normal(0.015, 0.01), 0.0, 0.06)
            samples  = rng.normal(loc=300, scale=60,
                                   size=max(total, 30)).clip(50, 800)

        rows.append(_make_row(rng, job_id, org_id, day, total, err_rate, samples,
                               retry_rate=0.15))
    return rows


def profile_failing(
    rng: np.random.Generator,
    job_id: int,
    org_id: int,
    days: list[date],
) -> list[DayRow]:
    """
    Умирающая задача: error rate 70-95%, быстро падает.
    Нужна для обучения модели на ярко-негативных примерах.
    """
    rows = []
    for day in days:
        total    = int(rng.integers(5, 21))
        err_rate = _clamp(rng.normal(0.82, 0.08), 0.65, 0.98)
        # Быстро фейлится — короткая длительность
        samples  = rng.normal(loc=150, scale=40,
                               size=max(total, 30)).clip(10, 500)
        rows.append(_make_row(rng, job_id, org_id, day, total, err_rate, samples,
                               retry_rate=0.3))
    return rows


def profile_recovering(
    rng: np.random.Generator,
    job_id: int,
    org_id: int,
    days: list[date],
) -> list[DayRow]:
    """
    Задача после фикса: первая половина периода — хаос, вторая — норма.
    Создаёт резкий перелом для обнаружения аномалий.
    """
    n_days   = len(days)
    mid      = n_days // 2
    rows     = []

    for i, day in enumerate(days):
        total = int(rng.integers(10, 51))

        if i < mid:
            # До фикса: высокий error rate, медленно
            err_rate = _clamp(rng.normal(0.50, 0.10), 0.30, 0.75)
            samples  = rng.normal(loc=3500, scale=800,
                                   size=max(total, 30)).clip(500, 15000)
        else:
            # После фикса: низкий error rate, быстро
            err_rate = _clamp(rng.normal(0.02, 0.01), 0.0, 0.06)
            samples  = rng.normal(loc=350, scale=70,
                                   size=max(total, 30)).clip(50, 800)

        rows.append(_make_row(rng, job_id, org_id, day, total, err_rate, samples,
                               retry_rate=0.05))
    return rows


# ── Генерация ─────────────────────────────────────────────────────────────────

PROFILES = [
    profile_stable,
    profile_degrading,
    profile_spiky,
    profile_failing,
    profile_recovering,
]


def generate(
    n_orgs: int,
    jobs_per_org: int,
    days: int,
    seed: int,
    start_job_id: int = 1,
    start_org_id: int = 1,
) -> list[DayRow]:
    """
    Генерирует все строки для всех организаций.

    Профили распределяются равномерно по задачам внутри организации.
    При jobs_per_org=10 и 5 профилях — по 2 задачи на профиль.
    """
    rng  = np.random.default_rng(seed)
    end  = date.today()
    day_list = [end - timedelta(days=days - 1 - i) for i in range(days)]

    rows     = []
    job_id   = start_job_id
    org_id   = start_org_id

    for _ in range(n_orgs):
        for j in range(jobs_per_org):
            profile_fn = PROFILES[j % len(PROFILES)]
            job_rows   = profile_fn(rng, job_id, org_id, day_list)
            rows.extend(job_rows)
            job_id += 1
        org_id += 1

    return rows


# ── Вывод ─────────────────────────────────────────────────────────────────────

def write_csv(rows: list[DayRow], path: Path) -> None:
    fields = [
        "job_id", "organization_id", "day",
        "total_executions", "succeeded", "failed",
        "avg_attempt_duration_ms", "p50_duration_ms",
        "p95_duration_ms", "max_duration_ms", "total_attempts",
    ]
    with path.open("w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fields)
        writer.writeheader()
        for row in rows:
            writer.writerow({
                "job_id":                  row.job_id,
                "organization_id":         row.organization_id,
                "day":                     row.day.isoformat(),
                "total_executions":        row.total_executions,
                "succeeded":               row.succeeded,
                "failed":                  row.failed,
                "avg_attempt_duration_ms": row.avg_attempt_duration_ms,
                "p50_duration_ms":         row.p50_duration_ms,
                "p95_duration_ms":         row.p95_duration_ms,
                "max_duration_ms":         row.max_duration_ms,
                "total_attempts":          row.total_attempts,
            })
    print(f"CSV written: {path} ({len(rows)} rows)")


def write_postgres(rows: list[DayRow]) -> None:
    """Вставляет строки в mv_job_execution_daily через прямой INSERT."""
    # Импортируем здесь — зависимость не нужна при --output csv
    from sqlalchemy import text
    sys.path.insert(0, str(Path(__file__).parent.parent))
    from app.db import get_engine

    insert_sql = text("""
        INSERT INTO mv_job_execution_daily (
            job_id, organization_id, day,
            total_executions, succeeded, failed,
            avg_attempt_duration_ms, p50_duration_ms,
            p95_duration_ms, max_duration_ms, total_attempts
        ) VALUES (
            :job_id, :organization_id, :day,
            :total_executions, :succeeded, :failed,
            :avg_attempt_duration_ms, :p50_duration_ms,
            :p95_duration_ms, :max_duration_ms, :total_attempts
        )
        ON CONFLICT (job_id, day) DO UPDATE SET
            organization_id         = EXCLUDED.organization_id,
            total_executions        = EXCLUDED.total_executions,
            succeeded               = EXCLUDED.succeeded,
            failed                  = EXCLUDED.failed,
            avg_attempt_duration_ms = EXCLUDED.avg_attempt_duration_ms,
            p50_duration_ms         = EXCLUDED.p50_duration_ms,
            p95_duration_ms         = EXCLUDED.p95_duration_ms,
            max_duration_ms         = EXCLUDED.max_duration_ms,
            total_attempts          = EXCLUDED.total_attempts
    """)

    engine = get_engine()
    batch_size = 500
    inserted   = 0

    with engine.begin() as conn:
        for i in range(0, len(rows), batch_size):
            batch = rows[i : i + batch_size]
            conn.execute(insert_sql, [
                {
                    "job_id":                  r.job_id,
                    "organization_id":         r.organization_id,
                    "day":                     r.day.isoformat(),
                    "total_executions":        r.total_executions,
                    "succeeded":               r.succeeded,
                    "failed":                  r.failed,
                    "avg_attempt_duration_ms": r.avg_attempt_duration_ms,
                    "p50_duration_ms":         r.p50_duration_ms,
                    "p95_duration_ms":         r.p95_duration_ms,
                    "max_duration_ms":         r.max_duration_ms,
                    "total_attempts":          r.total_attempts,
                }
                for r in batch
            ])
            inserted += len(batch)
            print(f"  inserted {inserted}/{len(rows)} rows...")

    print(f"PostgreSQL: {inserted} rows written to mv_job_execution_daily")


# ── CLI ───────────────────────────────────────────────────────────────────────

def main() -> None:
    parser = argparse.ArgumentParser(
        description="Generate synthetic data for ML model training"
    )
    parser.add_argument("--orgs",          type=int, default=3,
                        help="Number of organizations (default: 3)")
    parser.add_argument("--jobs-per-org",  type=int, default=10,
                        help="Jobs per organization (default: 10)")
    parser.add_argument("--days",          type=int, default=90,
                        help="History depth in days (default: 90)")
    parser.add_argument("--seed",          type=int, default=42,
                        help="Random seed for reproducibility (default: 42)")
    parser.add_argument("--start-job-id", type=int, default=1,
                        help="Starting job_id (default: 1)")
    parser.add_argument("--start-org-id", type=int, default=1,
                        help="Starting organization_id (default: 1)")
    parser.add_argument("--output",       choices=["csv", "postgres", "both"],
                        default="csv", help="Output target (default: csv)")
    parser.add_argument("--csv-path",     type=str,
                        default="synthetic_data.csv",
                        help="CSV output path (default: synthetic_data.csv)")
    args = parser.parse_args()

    print(f"Generating synthetic data:")
    print(f"  orgs={args.orgs}, jobs_per_org={args.jobs_per_org}, "
          f"days={args.days}, seed={args.seed}")
    print(f"  total rows: ~{args.orgs * args.jobs_per_org * args.days}")

    rows = generate(
        n_orgs=args.orgs,
        jobs_per_org=args.jobs_per_org,
        days=args.days,
        seed=args.seed,
        start_job_id=args.start_job_id,
        start_org_id=args.start_org_id,
    )

    print(f"Generated {len(rows)} rows, validating invariants...")
    # __post_init__ уже проверил каждую строку при создании
    print("All invariants OK")

    if args.output in ("csv", "both"):
        write_csv(rows, Path(args.csv_path))

    if args.output in ("postgres", "both"):
        write_postgres(rows)


if __name__ == "__main__":
    main()