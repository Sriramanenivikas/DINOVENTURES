#!/bin/bash

set -euo pipefail

APP_NAME="wallet-service"
IMAGE_TAG="${APP_NAME}:latest"
CONTAINER_NAME="${APP_NAME}"
PORT=8080

for var in SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD; do
    if [ -z "${!var:-}" ]; then
        echo "Error: $var is not set"
        exit 1
    fi
done

docker build -t "${IMAGE_TAG}" .

if docker ps -q -f name="${CONTAINER_NAME}" | grep -q .; then
    docker stop "${CONTAINER_NAME}" || true
    docker rm "${CONTAINER_NAME}" || true
fi

docker run -d \
    --name "${CONTAINER_NAME}" \
    --restart unless-stopped \
    -p "${PORT}:8080" \
    -e "SPRING_PROFILES_ACTIVE=prod" \
    -e "SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}" \
    -e "SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}" \
    -e "SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}" \
    -e "SPRING_CLOUD_AWS_SECRETSMANAGER_ENABLED=false" \
    -e "SPRING_CONFIG_IMPORT=optional:" \
    -e "JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC" \
    "${IMAGE_TAG}"

for i in $(seq 1 30); do
    if curl -s "http://localhost:${PORT}/actuator/health" | grep -q '"status":"UP"'; then
        echo "Wallet Service is running on port ${PORT}"
        exit 0
    fi
    sleep 2
done

echo "Health check failed after 60 seconds"
docker logs "${CONTAINER_NAME}"
exit 1
