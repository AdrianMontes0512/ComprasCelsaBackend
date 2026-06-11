#!/usr/bin/env bash
# ============================================================
# deploy.sh
# Construye la imagen del backend y la despliega a Cloud Run.
# Todas las env vars (incluidos secretos) vienen de env.production.yaml.
#
# Uso:  bash deploy.sh
#
# Variables opcionales (defaults entre paréntesis):
#   PROJECT_ID    (gcloud config get-value project)
#   REGION        (us-central1)
#   SERVICE_NAME  (compras-celsa-backend)
#   AR_REPO       (celsa-backend)
#   SQL_INSTANCE  (celsa-db)
# ============================================================
set -euo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ENV_FILE="${SCRIPT_DIR}/env.production.yaml"

# ---- Validaciones ----
[ ! -f "$ENV_FILE" ] && { echo "✗ No existe env.production.yaml. Cópialo desde env.production.example.yaml."; exit 1; }

PROJECT_ID="${PROJECT_ID:-$(gcloud config get-value project 2>/dev/null)}"
[ -z "$PROJECT_ID" ] && { echo "✗ No hay PROJECT_ID. gcloud config set project TU_ID"; exit 1; }

REGION="${REGION:-us-central1}"
SERVICE_NAME="${SERVICE_NAME:-compras-celsa-backend}"
AR_REPO="${AR_REPO:-celsa-backend}"
SQL_INSTANCE="${SQL_INSTANCE:-celsa-db}"

SQL_CONNECTION="$(gcloud sql instances describe "$SQL_INSTANCE" --project "$PROJECT_ID" --format='value(connectionName)' 2>/dev/null || true)"
[ -z "$SQL_CONNECTION" ] && { echo "✗ No se encontró la instancia Cloud SQL '$SQL_INSTANCE'."; exit 1; }

# Verifica que el connection name del env file coincida (warning si no)
EXPECTED_CONN="$(grep -E '^\s*INSTANCE_CONNECTION_NAME:' "$ENV_FILE" | sed 's/^[^:]*:[[:space:]]*//;s/[[:space:]]*$//')"
if [ "$EXPECTED_CONN" != "$SQL_CONNECTION" ]; then
  echo "⚠ env.production.yaml dice INSTANCE_CONNECTION_NAME=$EXPECTED_CONN"
  echo "  pero gcloud sql describe devuelve: $SQL_CONNECTION"
  echo "  → corrige el yaml o cambia la SQL_INSTANCE antes de continuar."
fi

IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/${AR_REPO}/backend:latest"

echo "→ Proyecto:     $PROJECT_ID"
echo "→ Región:       $REGION"
echo "→ Servicio:     $SERVICE_NAME"
echo "→ Imagen:       $IMAGE"
echo "→ Cloud SQL:    $SQL_CONNECTION"
echo ""

# ---- Build ----
echo "→ Construyendo imagen con Cloud Build..."
gcloud builds submit \
  --project "$PROJECT_ID" \
  --tag "$IMAGE" \
  "$SCRIPT_DIR"

# ---- Deploy ----
echo ""
echo "→ Desplegando a Cloud Run..."
gcloud run deploy "$SERVICE_NAME" \
  --project "$PROJECT_ID" \
  --image "$IMAGE" \
  --region "$REGION" \
  --platform managed \
  --allow-unauthenticated \
  --add-cloudsql-instances "$SQL_CONNECTION" \
  --memory 512Mi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 3 \
  --timeout 300 \
  --env-vars-file "$ENV_FILE"

# ---- Salida ----
URL="$(gcloud run services describe "$SERVICE_NAME" --project "$PROJECT_ID" --region "$REGION" --format='value(status.url)')"
echo ""
echo "✓ Deploy completado."
echo "  URL: $URL"
