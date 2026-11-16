# ADR-003: Estrategia de seguridad del pipeline

| | |
|---|---|
| **Estado** | Aceptada |
| **Fecha de registro** | 2026-11-16 (las herramientas y umbrales entraron entre el 11 y el 15 de noviembre con S5, S6, A5, A6 y A8; el código de gestión es la decisión D1 del [plan del Corte 3](../plan-corte-3.md)) |
| **Decide** | Equipo Pet Finder (Santiago Escobar, Antonio Benítez, Mateo Ramírez) |
| **Relacionada con** | [ADR-001](ADR-001-estilo-arquitectonico.md) (las reglas de arquitectura que corren en el pipeline), [ADR-004](ADR-004-estrategia-de-despliegue.md) (dónde y cómo se despliega) y [`docs/seguridad.md`](../seguridad.md) (modelo de amenazas y registro de hallazgos) |

## Contexto

Hasta el Corte 2 nada revisaba la seguridad de forma automática. La auditoría del 4 de noviembre encontró, con el sistema corriendo:

- **Cualquiera cerraba el caso de otra familia** con un `POST` sin credenciales (amenaza T1).
- **Una sola petición de 100 KB al asistente ocupaba un núcleo 17 segundos** por una expresión regular cuadrática (T6), y miles de turnos seguidos no tenían ningún límite (T7).
- **La consola de H2 respondía 200 en Render** (T9), y la página no tenía CSP, HSTS ni integridad en Leaflet (T3).
- **Tomcat y Jackson tenían vulnerabilidades conocidas**, que solo vimos porque alguien corrió Trivy a mano.

El enunciado del Corte 3 pide cuatro controles que detengan el pipeline (SAST, SCA, secretos e imagen) con un umbral mínimo cada uno, y permite endurecerlos pero no relajarlos. Las restricciones son las del curso: sin presupuesto, repositorio público en GitHub, una semana, y nada que dependa de cuentas o tokens de terceros que alguien tenga que crear y cuidar.

## Opciones consideradas

| Control | Elegida | Alternativas descartadas y por qué |
|---|---|---|
| Secretos | **Gitleaks** | **TruffleHog:** verifica las llaves contra los proveedores, lo que exige red hacia servicios externos en cada corrida; Gitleaks corre sin red, recorre todo el historial y saca SARIF |
| SAST | **CodeQL** (`security-extended`) | **Semgrep:** la edición comunitaria corre sin cuenta, pero no sigue los datos entre archivos, y ese análisis exige cuenta y token de su plataforma. **SonarCloud:** cuenta y token externos |
| SCA | **Trivy fs** | **OWASP Dependency-Check:** descarga lenta de la base NVD y necesita clave de API. **Dependabot:** solo reporta y abre PR; el enunciado aclara que no reemplaza el control de SCA |
| Imagen | **Trivy image** | **Grype:** cumple lo mismo, pero Trivy ya estaba en SCA: una sola herramienta, una sola base de datos de vulnerabilidades y un solo formato de reporte |
| Quién cierra un caso | **Código de gestión por caso** (D1) | **Cuentas de usuario con contraseña:** registro, recuperación de contraseña y sesiones, una funcionalidad entera que el enunciado no pide |

## Decisión

Cuatro controles en GitHub Actions, cada uno con su check en el PR y su umbral **copiado tal cual del workflow**:

| Control | Herramienta y versión fijada | Umbral que detiene el pipeline | Workflow y check |
|---|---|---|---|
| Secretos | Gitleaks 8.30.1, binario validado con su SHA-256 | **Cero secretos**, en todo el historial (`fetch-depth: 0`, `--exit-code 1`) | `seguridad.yml`, **Secretos** |
| SAST | CodeQL v4.39.2, `security-extended`, Java y JavaScript | **Ningún hallazgo con `security-severity` de 7.0 o más sin tratar**: CodeQL solo reporta, así que `scripts/umbral-sarif.py` lee el SARIF y falla (`umbral-sarif.py sarif-results 7.0`) | `seguridad.yml`, **SAST (java-kotlin)** y **SAST (javascript-typescript)** |
| SCA | Trivy v0.76.1 (`scan-type: fs`) | **HIGH o CRITICAL con corrección disponible** (`severity: HIGH,CRITICAL`, `ignore-unfixed: true`, `exit-code: '1'`) | `seguridad.yml`, **SCA** |
| Imagen | Trivy v0.76.1 sobre la imagen construida en el pipeline | **CRITICAL con corrección disponible** (`severity: CRITICAL`, `ignore-unfixed: true`, `exit-code: '1'`); un segundo paso reporta también los HIGH sin detener (`exit-code: '0'`) | `ci.yml`, **imagen-y-escaneo** |

Además:

- **Dependabot** (`.github/dependabot.yml`) abre PR de actualización de Maven, Docker y Actions. Es apoyo: no reemplaza el SCA.
- **Acciones fijadas por SHA** de 40 caracteres, con el check **acciones-fijadas** (A8) que falla si una no lo está, y `permissions: contents: read` por defecto en cada workflow (amenaza T11).
- **Corrida diaria** de `seguridad.yml` a las 11:00 UTC: la base de vulnerabilidades cambia aunque nadie toque el código.
- **Excepciones con nombre y justificación:** una alerta solo deja de bloquear si queda en `.github/codeql/excepciones.txt` (o en `.gitleaksignore` / `.trivyignore`) con su fila en `docs/seguridad.md`. Descartarla en la pestaña Security no cambia el SARIF.
- **Código de gestión (D1):** al publicar se entrega una sola vez un código de 128 bits; en la base solo queda su huella SHA-256. Es autenticación por posesión y autorización por caso, sin registro ni contraseñas. Un código de administrador (`PETFINDER_CODIGO_ADMIN`) cierra casos abusivos o viejos.

### Por qué SCA usa HIGH y la imagen CRITICAL

Son los mínimos del enunciado para cada control, y la diferencia tiene una razón práctica. Las dependencias de `pom.xml` las elige el equipo: si una tiene una vulnerabilidad alta con corrección, se sube de versión. La imagen trae además los paquetes del sistema base (Ubuntu en `eclipse-temurin`), que el equipo no controla y que se corrigen cuando sale una imagen base nueva. Bloquear por cada HIGH del sistema base dejaría el pipeline en rojo por días sin nada que el equipo pueda hacer; por eso los HIGH de la imagen quedan en el reporte y los CRITICAL corregibles sí detienen.

## Consecuencias positivas

- **Cada control demostró que bloquea.** Corridas en rojo a propósito: Secretos con una llave falsa ([corrida](https://github.com/DrearSanti/pet-finder-dyas/actions/runs/43045628592)), SAST con `java/command-line-injection` de 9,8 ([corrida](https://github.com/DrearSanti/pet-finder-dyas/actions/runs/43050677998)), y el PR de evidencia que junta los cuatro controles (ver `docs/seguridad.md`).
- **Sin cuentas ni tokens de terceros:** las cuatro herramientas corren con el `GITHUB_TOKEN` del workflow. Nadie del equipo tiene que crear ni rotar credenciales para que el pipeline funcione.
- **Una sola herramienta para dependencias e imagen** (Trivy), con la misma base de datos y el mismo formato de reporte.
- **El umbral de SAST es auditable:** `umbral-sarif.py` imprime cada hallazgo que bloquea y sale 2 si no encuentra SARIF, así que una ruta mal escrita no puede dejar pasar el pipeline en verde.
- **T1 se resolvió sin cuentas:** quien publica recibe un código y nadie más puede cerrar su caso.

## Consecuencias negativas

- **CodeQL es el job más lento** (unos 2,5 minutos en Java). Corre en paralelo con el CI, así que no alarga la espera del PR más allá del job más lento.
- **El SCA consulta Maven Central y la base de Trivy en cada corrida.** Un 429 de Maven Central llegó a poner SCA en rojo sin que hubiera vulnerabilidades; A5 lo corrigió descargando las dependencias primero (#73).
- **Gitleaks sin verificación activa** no sabe si una llave sigue viva: trata igual una llave real y una de ejemplo. Los valores de ejemplo se registran como falso positivo.
- **Quien pierde el código de gestión depende del administrador** para cerrar su caso.
- **Los HIGH del sistema base de la imagen no bloquean**: quedan en el reporte `trivy-imagen.txt` y se revisan a mano.

## Cuándo se revisaría

- Si una alerta de SAST, SCA o imagen tiene que quedar abierta más de una semana, se registra como riesgo aceptado en `docs/seguridad.md` y se revisa este umbral.
- Si el sistema necesita cuentas de usuario de verdad (por ejemplo, para que una fundación administre sus casos), el código de gestión se reemplaza por autenticación con cuentas y roles.
- Si el repositorio deja de ser público, CodeQL y secret scanning requieren GitHub Advanced Security y habría que revisar las herramientas.
