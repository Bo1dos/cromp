# ═══════════════════════════════════════════════════════════════════════════════
# Cron as a Service — Backend Dockerfile (multi‑stage)
# ═══════════════════════════════════════════════════════════════════════════════
#
# Build context: project root  (.)
#   docker build -t cromp-backend -f backend/Dockerfile .
#
# Targets:
#   builder  — компилирует jar с помощью Gradle
#   runtime  — минимальный образ только с JRE + jar
# ═══════════════════════════════════════════════════════════════════════════════

# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Копируем Gradle wrapper и конфигурацию зависимостей (кэширование слоёв)
COPY gradlew gradlew.bat settings.gradle build.gradle gradle.properties ./
COPY gradle/            gradle/
COPY backend/           backend/

# Собираем только orchestrator (точка входа модульного монолита), без тестов
RUN ./gradlew :backend:orchestrator:bootJar --no-daemon -x test --parallel

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# wget нужен для HEALTHCHECK
RUN apk add --no-cache wget

# Копируем собранный fat‑jar
COPY --from=builder /workspace/backend/orchestrator/build/libs/cron-service.jar ./app.jar

# Spring Boot Actuator health endpoint доступен внутри контейнера
EXPOSE 8080

HEALTHCHECK \
    --interval=30s \
    --timeout=5s \
    --start-period=60s \
    --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Не запускаем от root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]