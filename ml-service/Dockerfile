FROM python:3.11-slim AS base

# Системные зависимости для psycopg2 и scikit-learn
RUN apt-get update && apt-get install -y --no-install-recommends \
        libpq-dev \
        gcc \
        curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Зависимости отдельным слоем — кэшируются если requirements.txt не изменился
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Исходный код
COPY app/ ./app/

# Директория для моделей
RUN mkdir -p /app/models

# Не запускаем от root
RUN useradd -m -u 1001 mluser && chown -R mluser:mluser /app
USER mluser

# ── API ───────────────────────────────────────────────────────────────────────
FROM base AS api
EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--workers", "2"]

# ── Celery worker ─────────────────────────────────────────────────────────────
FROM base AS worker
CMD ["celery", "-A", "app.tasks.celery.celery_app", "worker", "--loglevel=info", "--concurrency=2"]

# ── Celery beat ───────────────────────────────────────────────────────────────
FROM base AS beat
CMD ["celery", "-A", "app.tasks.celery.celery_app", "beat", "--loglevel=info"]