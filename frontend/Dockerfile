# ═══════════════════════════════════════════════════════════════════════════════
# Cron as a Service — Frontend Dockerfile (React + Vite, dev mode)
# ═══════════════════════════════════════════════════════════════════════════════
#
# Build context: ./frontend
#   docker build -t cromp-frontend -f frontend/Dockerfile frontend
# ═══════════════════════════════════════════════════════════════════════════════

FROM node:20-alpine

WORKDIR /app

# Кэшируем npm ci
COPY package*.json ./
RUN npm install --legacy-peer-deps

COPY . .

EXPOSE 5173
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
