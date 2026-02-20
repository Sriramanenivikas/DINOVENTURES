#!/bin/bash
# ==========================================
# Wallet Service — EC2 Deployment Script
# ==========================================
# Usage: ./deploy.sh
#
# Prerequisites:
#   1. Docker installed on EC2 instance
#   2. RDS PostgreSQL instance running
#   3. Environment variables set (see .env.example)
# ==========================================

set -euo pipefail

APP_NAME="wallet-service"
IMAGE_TAG="${APP_NAME}:latest"
CONTAINER_NAME="${APP_NAME}"
PORT=8080

echo "🦕 Dino Ventures — Wallet Service Deployment"
echo "============================================="

# Check required environment variables
for var in DATABASE_URL DATABASE_USERNAME DATABASE_PASSWORD; do
    if [ -z "${!var:-}" ]; then
        echo "❌ Error: $var is not set"
        echo "   Set it in your environment or create a .env file"
        exit 1
    fi
done

# Step 1: Build Docker image
echo ""
echo "📦 Building Docker image..."
docker build -t "${IMAGE_TAG}" .

# Step 2: Stop existing container (if running)
if docker ps -q -f name="${CONTAINER_NAME}" | grep -q .; then
    echo "⏹️  Stopping existing container..."
    docker stop "${CONTAINER_NAME}" || true
    docker rm "${CONTAINER_NAME}" || true
fi

# Step 3: Run new container
echo "🚀 Starting new container..."
docker run -d \
    --name "${CONTAINER_NAME}" \
    --restart unless-stopped \
    -p "${PORT}:8080" \
    -e "SPRING_PROFILES_ACTIVE=prod" \
    -e "DATABASE_URL=${DATABASE_URL}" \
    -e "DATABASE_USERNAME=${DATABASE_USERNAME}" \
    -e "DATABASE_PASSWORD=${DATABASE_PASSWORD}" \
    -e "JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC" \
    "${IMAGE_TAG}"

# Step 4: Wait for health check
echo "⏳ Waiting for application to start..."
for i in $(seq 1 30); do
    if curl -s "http://localhost:${PORT}/actuator/health" | grep -q '"status":"UP"'; then
        echo ""
        echo "✅ Wallet Service is UP and HEALTHY!"
        echo "   🌐 API:       http://localhost:${PORT}/api/v1/"
        echo "   ❤️  Health:    http://localhost:${PORT}/actuator/health"
        echo ""
        docker logs --tail 5 "${CONTAINER_NAME}"
        exit 0
    fi
    sleep 2
    printf "."
done

echo ""
echo "❌ Health check failed after 60 seconds."
echo "   Check logs: docker logs ${CONTAINER_NAME}"
exit 1
