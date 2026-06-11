# Deploy del backend a Google Cloud

Despliegue de Compras Celsa Backend en **Cloud Run** + **Cloud SQL** + **Artifact Registry**.

> Flujo: completas un archivo (`env.production.yaml`) y corres un script (`deploy.sh`). Todo lo demás se inferiere automáticamente.

---

## Archivos del proyecto

| Archivo | Git | Para qué |
|---|---|---|
| `env.production.example.yaml` | ✅ commit | Plantilla con los nombres de envs |
| `env.production.yaml` | 🚫 .gitignore | Tu copia con valores reales (incluye secretos en texto plano) |
| `deploy.sh` | ✅ commit | Build + deploy a Cloud Run |
| `application-cloud.properties` | ✅ commit | Activa Cloud SQL via Spring Cloud GCP |

> **Nota de seguridad:** todos los valores (incluidos `JWT_SECRET`, `DB_PASSWORD`, `MAIL_PASSWORD`) se inyectan como env vars planas en Cloud Run. Quien tenga rol `roles/run.viewer` puede leerlos en la consola. Para hardenizar más adelante, mueve los sensibles a Secret Manager con `--set-secrets`.

---

## 0. Setup inicial (una vez)

```bash
# Reemplaza con tu project ID real
gcloud config set project TU_PROJECT_ID
gcloud config set run/region us-central1
gcloud config set artifacts/location us-central1

# APIs necesarias
gcloud services enable \
  run.googleapis.com \
  sqladmin.googleapis.com \
  artifactregistry.googleapis.com \
  cloudbuild.googleapis.com
```

---

## 1. Crear Cloud SQL Postgres (una vez)

```bash
gcloud sql instances create celsa-db \
  --database-version=POSTGRES_16 \
  --tier=db-f1-micro \
  --region=us-central1 \
  --storage-size=10GB \
  --backup-start-time=03:00

# Anota el password de la DB:
DB_PASS="$(openssl rand -base64 24)"
echo "DB password (úsalo en env.production.yaml): $DB_PASS"

gcloud sql databases create celsa --instance=celsa-db
gcloud sql users create celsa_app --instance=celsa-db --password="$DB_PASS"
```

---

## 2. Crear el repositorio en Artifact Registry (una vez)

```bash
gcloud artifacts repositories create celsa-backend \
  --repository-format=docker \
  --location=us-central1 \
  --description="Imágenes del backend de Compras Celsa"
```

---

## 3. Completar `env.production.yaml`

Cópialo desde la plantilla:

```bash
cp env.production.example.yaml env.production.yaml
```

Y edítalo con tus valores reales:

```yaml
SPRING_PROFILES_ACTIVE: cloud
INSTANCE_CONNECTION_NAME: tu-project-id:us-central1:celsa-db
DB_NAME: celsa
DB_USERNAME: celsa_app
DB_PASSWORD: el_password_que_generaste_arriba
JWT_SECRET: 545335351d1bfaad5204c6245a5d917740a9d9568d2f4747b63ea84fa89b0ba2
MAIL_USERNAME: sistemcomprascelsa@gmail.com
MAIL_HOST: smtp.gmail.com
MAIL_PORT: "587"
MAIL_PASSWORD: la_app_password_de_gmail
JPA_DDL_AUTO: update
JPA_SHOW_SQL: "false"
```

Para obtener el `INSTANCE_CONNECTION_NAME` exacto:
```bash
gcloud sql instances describe celsa-db --format='value(connectionName)'
```

---

## 4. Build & deploy

```bash
bash deploy.sh
```

Esto:
- Construye la imagen Docker via Cloud Build (no necesita Docker corriendo en local)
- La sube a Artifact Registry
- Despliega a Cloud Run con:
  - `--env-vars-file env.production.yaml` → todas las env vars del archivo
  - `--add-cloudsql-instances` → conexión privada a la BD
- Imprime la URL final

Tarda ~3-5 min la primera vez (Maven baja dependencias). Las siguientes son más rápidas.

---

## 5. Re-deploy cuando cambias código

```bash
bash deploy.sh
```

Si solo cambiaste código fuente, las env vars persisten — `deploy.sh` reconstruye la imagen y actualiza el servicio.

Si cambiaste el `env.production.yaml`, también basta correr `deploy.sh`.

---

## 6. Rotar el JWT secret, password de BD o mail

1. Editas `env.production.yaml` con el nuevo valor
2. Corres `bash deploy.sh`

Para `JWT_SECRET`: ojo que cualquier sesión activa en el frontend dejará de funcionar (los tokens viejos no validan con el nuevo secret) — los usuarios deben re-loguear.

---

## 7. Apuntar el frontend

En `ComprasCelsaFrontend/src/services/api.ts` cambia:
```ts
export const API_BASE = "http://localhost:8080";
```
por:
```ts
export const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";
```

Y crea `ComprasCelsaFrontend/.env.production`:
```
VITE_API_BASE=https://compras-celsa-backend-xxxxxxxx-uc.a.run.app
```

(la URL la imprime `deploy.sh` al final, o se obtiene con `gcloud run services describe compras-celsa-backend --region=us-central1 --format='value(status.url)'`)

---

## 8. Cómo generar la app password de Gmail

> La actual estaba en el código fuente y se considera comprometida; rótala.

1. Entra a https://myaccount.google.com con `sistemcomprascelsa@gmail.com`
2. Activa 2FA si no lo está (Seguridad → Verificación en 2 pasos)
3. https://myaccount.google.com/apppasswords → "Crear" con nombre "Compras Celsa Backend"
4. Copia los 16 chars en `env.production.yaml` → `MAIL_PASSWORD: ...`
5. `bash deploy.sh`
6. Después revoca la app password vieja en la misma página

---

## Cómo funciona mentalmente

```
env.production.yaml ──► deploy.sh ──► Cloud Run service
                          │
                          │ --env-vars-file (todas las envs, incluidos secretos)
                          │ --add-cloudsql-instances (socket privado)
                          ▼
                       Cloud SQL Postgres
```

- **`env.production.yaml`** vive solo en tu máquina (gitignored).
- Cloud Run lee el archivo al hacer deploy y guarda los valores como env vars planas en la config del servicio.
- El proceso Spring Boot dentro del contenedor lee env vars normales (`System.getenv("JWT_SECRET")`) sin saber de dónde vinieron.

---

## Caveats

- `env.production.yaml` **NO se sube a git**. Confirma `cat .gitignore | grep env.production`.
- Si compartes el repo con alguien, debe completar su propio `env.production.yaml` (no le mandes el tuyo).
- El `deploy.sh` usa `--allow-unauthenticated`: cualquiera puede hacer HTTP request a tu Cloud Run. Tu capa JWT controla el acceso real a los endpoints (todos requieren token salvo `/auth/login`).
- Para reducir gasto cuando no usas: `gcloud sql instances patch celsa-db --activation-policy=NEVER` apaga la BD sin borrarla. Encender con `--activation-policy=ALWAYS`.

---

## Tabla de costos aproximada

| Recurso | Costo |
|---|---|
| Cloud Run (sin tráfico) | $0 |
| Cloud Run (tráfico bajo) | $5-15/mes |
| Cloud SQL db-f1-micro | ~$8/mes (cobra siempre encendido) |
| Artifact Registry | ~$0.10/mes |
| Cloud Build | 120 min/día gratis |

---

## Migrar a Secret Manager más adelante (opcional)

Si en algún momento quieres hardenizar los secretos:

1. Sube `DB_PASSWORD`, `JWT_SECRET`, `MAIL_PASSWORD` a Secret Manager:
   ```bash
   for name in DB_PASSWORD JWT_SECRET MAIL_PASSWORD; do
     val=$(grep "^${name}:" env.production.yaml | sed 's/^[^:]*:[[:space:]]*//')
     printf "%s" "$val" | gcloud secrets create "$name" --data-file=-
   done
   ```
2. Quita esas líneas del `env.production.yaml`
3. Agrega al `deploy.sh`, en el `gcloud run deploy`:
   ```
   --set-secrets="DB_PASSWORD=DB_PASSWORD:latest,JWT_SECRET=JWT_SECRET:latest,MAIL_PASSWORD=MAIL_PASSWORD:latest"
   ```
4. Dale rol al SA de Cloud Run:
   ```bash
   PROJECT_NUMBER=$(gcloud projects describe $(gcloud config get-value project) --format='value(projectNumber)')
   SA="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"
   for s in DB_PASSWORD JWT_SECRET MAIL_PASSWORD; do
     gcloud secrets add-iam-policy-binding "$s" --member="serviceAccount:$SA" --role="roles/secretmanager.secretAccessor"
   done
   ```
