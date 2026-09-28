# Despliegue en Render

Pet Finder está desplegado en **https://pet-finder-akac.onrender.com** con el `Dockerfile` del repositorio (construcción en dos etapas, JDK 17 para compilar y JRE 17 para correr, `TZ=America/Bogota`).

| Qué | Valor |
|---|---|
| URL | https://pet-finder-akac.onrender.com |
| Plan | Free (512 MB), región Virginia (US East) |
| Rama | `main`: cada merge se despliega |
| Health check | `/actuator/health` |
| Variables | `ANTHROPIC_API_KEY` (asistente con Claude), `PETFINDER_CLAVE_CARTO` (teselas del mapa, se lee al construir la imagen) y `PETFINDER_IA_TOPE_DIARIO=100`. Ninguna está en el repositorio |

## Verificación (2026-09-28 15:16, hora de Colombia)

```text
$ curl https://pet-finder-akac.onrender.com/actuator/health
{"groups":["liveness","readiness"],"status":"UP"}

$ curl https://pet-finder-akac.onrender.com/api/reportes   # casos activos de ejemplo
4 casos
```

El asistente responde con `"fuente":"claude"` desde el PR #25 (antes caía a regex por un esquema que la API rechazaba con error 400). Si Claude falla o se llega al tope diario, sigue respondiendo con regex.

## Límites del plan gratis

- Se duerme tras 15 minutos sin visitas; la primera visita tarda cerca de un minuto en despertarlo.
- H2 está en memoria: al despertar vuelve a los casos de ejemplo.
- Sin dominio propio: la URL es la que asigna Render.
