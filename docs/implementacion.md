# Guía de implementación de Pet Finder

Esta guía lleva a alguien que conoce Docker y Git, pero no el proyecto, desde cuentas vacías hasta Pet Finder funcionando en internet, con su pipeline, su base de datos y su despliegue automático. No hace falta preguntarle nada al equipo: si algo no funciona como dice aquí, la [parte 8](#8-solución-de-problemas) tiene los problemas conocidos.

Los valores que cambian según quien instala van entre ángulos: `<usuario-github>`, `<url-de-render>`. Todos los comandos funcionan en macOS, Linux y **Git Bash** de Windows; donde PowerShell es distinto, se indica.

Precios y límites revisados el **2026-11-15**.

## 1. Qué se va a implementar

Pet Finder conecta a quien perdió una mascota con quien la vio o la encontró: se publica un caso (por formulario, por voz o con ayuda de un asistente), aparece en un mapa y otras personas reportan avistamientos. Es una aplicación Spring Boot (Java 17) con arquitectura hexagonal que sirve la API REST y la página web (instalable como app, PWA).

En producción son tres piezas:

| Componente | Dónde corre | Qué hace |
|---|---|---|
| Imagen `ghcr.io/<usuario-github>/pet-finder:<SHA>` | GitHub Container Registry (paquete público) | La construye y publica el pipeline en cada commit de `main`, etiquetada con el SHA del commit (y con la versión en cada tag `v*`). Nunca `latest` |
| Servicio web `pet-finder` | Render, en modo "imagen existente", con HTTPS | Corre la imagen. El workflow `CD` le pide cada versión nueva con un *deploy hook* |
| Base `pet-finder-db` | PostgreSQL de Render, misma región, URL interna | Guarda reportes y avistamientos. Flyway crea y migra el esquema al arrancar la app |

Servicios externos opcionales: la API de Claude (Anthropic) para el asistente, y CARTO para las teselas del mapa. Sin ellos la app funciona igual: el asistente usa reglas y el mapa usa OpenStreetMap.

El diagrama está en la **vista de despliegue**: [docs/arquitectura.md, sección 8](arquitectura.md#8-vista-de-despliegue-corte-3). El porqué de esta elección está en el [ADR-004](adr/ADR-004-estrategia-de-despliegue.md).

## 2. Requisitos previos

**Cuentas** (todas gratis para empezar):

| Cuenta | Para qué |
|---|---|
| GitHub | El repositorio (fork), GitHub Actions (pipeline) y GitHub Container Registry (GHCR, la imagen). GHCR no necesita cuenta aparte: va con la de GitHub |
| Render (https://render.com) | El servicio web y la base PostgreSQL. Se puede entrar con la cuenta de GitHub |
| Anthropic (opcional) | Clave de la API de Claude para el asistente |
| CARTO (opcional) | Clave gratuita de mapas base (https://carto.com/basemaps/apikey) |

**Software en tu computador:**

| Programa | Versión | Para qué | Comprobar |
|---|---|---|---|
| Git | 2.40 o más | Clonar y subir cambios | `git --version` |
| Docker con Compose | Docker 25 o más, Compose v2 | Probar todo en local antes de desplegar | `docker compose version` |
| GitHub CLI (opcional) | 2.40 o más | Crear secretos y lanzar workflows desde la terminal | `gh --version` |
| JDK 17 | Solo para desarrollar | Compilar y correr las pruebas. El wrapper (`./mvnw`) descarga Maven 3.9.16 solo: no instales Maven | `./mvnw -v` debe decir `Java version: 17` |
| curl y sh | Vienen con macOS, Linux y Git Bash | Prueba de humo | `curl --version` |

**Recursos mínimos:**
- En local: 2 CPU y 4 GB de RAM libres para Docker (la app usa hasta el 75 % de la memoria del contenedor y PostgreSQL unos 100 MB). La primera construcción de la imagen descarga unos 300 MB de dependencias.
- En Render: el plan gratis (512 MB) arranca la app, pero con poca CPU; ver la parte 3.

**Red:** la app escucha en el puerto **8080** (en Render, en el que diga la variable `PORT`, que Render pone sola). **HTTPS** lo da Render en la URL `*.onrender.com`; no hay que configurar certificados.

## 3. Costos estimados

| Concepto | Para probar (lo que usa el proyecto) | Recomendado para operar de verdad |
|---|---|---|
| Servicio web en Render | **Free**: 0 USD. Se duerme tras 15 minutos sin visitas y la primera visita tarda cerca de un minuto en despertarlo | **Starter**: **7 USD al mes**, 0,5 CPU, sin dormirse (decisión D2 del plan). El cobro es por segundo |
| PostgreSQL en Render | **Free**: 0 USD, 1 GB. **Vence a los 30 días** de creada; hay 14 días de gracia para pasarla a un plan pago y después Render **borra la base y sus datos** | Plan pago más pequeño (Basic): **[Comprobar en https://render.com/pricing#postgresql el día de la instalación]**, del orden de 6 USD al mes más el almacenamiento. Es obligatorio si el negocio no puede perder sus casos |
| GitHub Actions | 0 USD en repositorio público | Igual |
| GitHub Container Registry | 0 USD para paquetes públicos | Igual |
| API de Claude (opcional) | Se paga por uso. `PETFINDER_IA_TOPE_DIARIO` (200 por defecto) pone un tope de llamadas por día; al llegar al tope, el asistente sigue con reglas | Ajustar el tope al presupuesto. Sin clave, 0 USD |
| CARTO (opcional) | 0 USD (clave gratuita de mapas base) | Igual |
| **Total** | **0 USD** | **Unos 13 USD al mes** más el uso de Claude, si se usa |

## 4. Configuración

Ninguna de estas variables está en el repositorio con un valor real. En local van en `.env` (copia de `.env.example`, que Git ignora); en producción, en el panel de Render; y el deploy hook, en GitHub.

**Cómo generar los valores sensibles** (funciona en Git Bash, macOS y Linux):
```bash
openssl rand -hex 32        # contraseña de PostgreSQL local
openssl rand -base64 24     # código de administrador (32 caracteres)
```

| Variable | Para qué sirve | Valor de ejemplo | ¿Obligatoria? | Dónde se configura |
|---|---|---|---|---|
| `POSTGRES_USER` | Usuario de PostgreSQL en Compose | `petfinder` | Sí, en Compose | `.env` |
| `POSTGRES_PASSWORD` | Contraseña de PostgreSQL en Compose. No puede ir vacía | `cambie-esta-clave` (cámbiala con `openssl rand -hex 32`) | Sí, en Compose | `.env` |
| `POSTGRES_DB` | Nombre de la base en Compose | `petfinder` | No (por defecto `petfinder`) | `.env` |
| `SPRING_DATASOURCE_URL` | Dónde está la base, en formato JDBC. Sin ella la app usa H2 en memoria y pierde los datos al reiniciar | `jdbc:postgresql://<host-interno>:5432/<base>` | Sí, en producción | Panel de Render. En Compose la arma `docker-compose.yml` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base | El que muestra Render | Sí, en producción | Panel de Render |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base | La que muestra Render | Sí, en producción | Panel de Render |
| `SPRING_H2_CONSOLE_ENABLED` | Consola web de H2. Siempre apagada fuera del computador de quien desarrolla (amenaza T9) | `false` | No (ya está en `false`) | `docker-compose.yml` |
| `PORT` | Puerto HTTP de la app | `8080` | No (8080 por defecto) | Render la pone sola; en local, `.env` |
| `ANTHROPIC_API_KEY` | Clave de la API de Claude para el asistente. Vacía = asistente con reglas | vacía | No | `.env` y panel de Render |
| `PETFINDER_IA_MODELO` | Modelo de Claude que usa el asistente | `claude-sonnet-5` | No | `.env` y panel de Render |
| `PETFINDER_IA_TOPE_DIARIO` | Máximo de llamadas a Claude por día; protege el presupuesto | `200` | No | `.env` y panel de Render |
| `PETFINDER_CLAVE_CARTO` | Clave pública de CARTO para el mapa. Se lee al arrancar, nunca va dentro de la imagen. Vacía = OpenStreetMap | vacía | No | `.env` y panel de Render |
| `PETFINDER_CODIGO_ADMIN` | Código de administrador: puede resolver o cerrar cualquier caso. Vacío = sin administrador. Si se define, 20 caracteres o más, y nunca se escribe en el log ni en el repositorio | salida de `openssl rand -base64 24` | No, pero recomendado en producción | `.env` y panel de Render |
| `PETFINDER_LISTA_MAXIMO` | Cuántos casos activos devuelve la lista, los más recientes primero | `200` | No | `.env` y panel de Render |
| `PETFINDER_DATOSEJEMPLO` | Carga cuatro casos de ejemplo al arrancar, solo si la base está vacía. Es la propiedad `petfinder.datos-ejemplo` escrita como variable (Spring quita el guion) | `true`; en producción real, `false` | No | Panel de Render |
| `PETFINDER_LIMITE_ASISTENTE_IP` | Límite de peticiones al asistente por IP (S10) | `10` | No | `.env` y panel de Render |
| `PETFINDER_LIMITE_ASISTENTE_GLOBAL` | Límite global de peticiones al asistente (S10); protege el gasto en Claude | `60` | No | `.env` y panel de Render |
| `PETFINDER_LIMITE_VOZ_IP` | Límite de peticiones a `/api/voz` por IP (S10) | `3000` | No | `.env` y panel de Render |
| `PETFINDER_LIMITE_REPORTES_IP` | Límite de peticiones a `/api/reportes` por IP (S10) | `3000` | No | `.env` y panel de Render |
| `PETFINDER_COMMIT` y `PETFINDER_VERSION` | Commit y versión que muestra `/actuator/info` | Los pone el pipeline al construir la imagen | No: nunca se configuran a mano | `cd.yml` |
| `RENDER_DEPLOY_HOOK_URL` | URL secreta con la que el workflow `CD` le pide a Render desplegar una imagen. Quien la tenga puede desplegar cualquier imagen | `https://api.render.com/deploy/srv-...?key=...` | Sí, para el despliegue continuo | GitHub → Settings → Environments → `produccion` → Environment secrets |
| `GITHUB_TOKEN` | Permiso del pipeline para publicar la imagen en GHCR | Lo crea GitHub solo en cada corrida | — | Nada que hacer |

Los límites de S10 se miden por ventana de tiempo; **[Comprobar la ventana en el código de S10 cuando entre a `main`]**. Si la prueba de carga corta choca con ellos, súbelos (el pipeline usa `100000`).

## 5. Instalación paso a paso

### 5.1 Copia del repositorio

1. En https://github.com/DrearSanti/pet-finder-dyas, botón **Fork** → en tu cuenta. Resultado: `https://github.com/<usuario-github>/pet-finder-dyas`.
2. Clónalo:
   ```bash
   git clone https://github.com/<usuario-github>/pet-finder-dyas.git
   cd pet-finder-dyas
   ```
3. En tu fork, pestaña **Actions** → **I understand my workflows, go ahead and enable them**. Los forks llegan con Actions apagado.

### 5.2 Probar en local con Docker (recomendado antes de desplegar)

```bash
cp .env.example .env
```
Abre `.env` y cambia `POSTGRES_PASSWORD` por la salida de `openssl rand -hex 32`.
```bash
docker compose up -d --build --wait
docker compose ps
```
Resultado esperado: la primera vez tarda unos minutos (descarga dependencias). Al final, `app` y `db` aparecen como `(healthy)`. Abre http://localhost:8080: se ve el mapa con cuatro casos de ejemplo.

```bash
sh scripts/humo-flujo.sh http://localhost:8080
```
Resultado esperado: seis líneas `ok` y `Prueba de humo del flujo: todo en orden`. Para apagar: `docker compose down` (con `-v` también se borra la base local).

### 5.3 Ajustar los workflows a tu URL

`cd.yml` y `revertir.yml` tienen escrita la URL del proyecto original. Cuando tengas la tuya (paso 5.6), reemplázala:
```bash
grep -rn "pet-finder-akac.onrender.com" .github/workflows/
```
Cambia cada `https://pet-finder-akac.onrender.com` por `https://<url-de-render>` en `URL_APP` y en `environment.url`, y súbelo:
```bash
git add .github/workflows/cd.yml .github/workflows/revertir.yml
git commit -m "Usa la URL de mi servicio en Render"
git push
```

### 5.4 Primera imagen en GHCR

1. Cualquier push a `main` (por ejemplo el del paso 5.3, o en **Actions → CD → Run workflow**) corre el workflow `CD`. Su job **Publicar imagen** construye y publica `ghcr.io/<usuario-github>/pet-finder:<SHA>`.
   El job **Desplegar en Render y verificar** va a fallar todavía con `Falta el secreto RENDER_DEPLOY_HOOK_URL`: es lo esperado hasta el paso 5.7.
2. Haz público el paquete para que Render lo descargue sin credenciales: en tu perfil de GitHub → **Packages** → `pet-finder` → **Package settings** → **Change visibility** → **Public**. La imagen no lleva secretos.
3. Anota el SHA del commit (`git rev-parse HEAD`). Resultado esperado: `docker pull ghcr.io/<usuario-github>/pet-finder:<SHA>` funciona sin `docker login`.

### 5.5 Base de datos en Render

1. En https://dashboard.render.com → **New** → **Postgres**.
2. Nombre `pet-finder-db`, región (anótala: el servicio web va en la misma), plan **Free** para probar o el pago recomendado (parte 3).
3. **Create Database**. Cuando diga *Available*, abre **Connections** y anota: *Hostname* interno, *Port*, *Database*, *Username* y *Password*.

   La "Internal Database URL" que muestra Render empieza con `postgresql://usuario:clave@host/base`. La app necesita el formato JDBC y el usuario y la clave aparte:
   ```text
   SPRING_DATASOURCE_URL=jdbc:postgresql://<hostname-interno>:5432/<database>
   SPRING_DATASOURCE_USERNAME=<username>
   SPRING_DATASOURCE_PASSWORD=<password>
   ```

### 5.6 Servicio web en Render

1. **New** → **Web Service** → **Existing Image**.
2. Image URL: `ghcr.io/<usuario-github>/pet-finder:<SHA>` (el del paso 5.4). Sin credenciales: el paquete es público.
3. Nombre `pet-finder`, **la misma región de la base**, plan Free o Starter.
4. **Environment Variables**: agrega las tres `SPRING_DATASOURCE_*` del paso 5.5 y, si las usas, `PETFINDER_CODIGO_ADMIN`, `ANTHROPIC_API_KEY`, `PETFINDER_IA_TOPE_DIARIO`, `PETFINDER_CLAVE_CARTO` (parte 4). No pongas `PORT`.
5. **Advanced** → **Health Check Path**: `/actuator/health/readiness`.
6. **Deploy Web Service**. **[Comprobar los nombres exactos de los botones en el panel de Render]**.

Resultado esperado: en **Logs** aparecen `Successfully applied 2 migrations` (Flyway creó las tablas) y `Started PetFinderApplication`. La URL `https://<url-de-render>` abre el mapa con los cuatro casos de ejemplo. Vuelve al paso 5.3 con esa URL si no lo hiciste.

### 5.7 Deploy hook y despliegue continuo

1. En Render, servicio `pet-finder` → **Settings** → **Deploy Hook** → copia la URL. Es un secreto: no la pegues en chats, issues ni commits.
2. En GitHub, tu fork → **Settings** → **Environments** → **New environment** → nombre exacto `produccion` → **Add environment secret** → nombre `RENDER_DEPLOY_HOOK_URL`, valor la URL copiada. Con GitHub CLI:
   ```bash
   gh secret set RENDER_DEPLOY_HOOK_URL --env produccion --repo <usuario-github>/pet-finder-dyas
   ```
   (pide el valor sin mostrarlo).
3. **Actions** → **CD** → **Run workflow** sobre `main`.

Resultado esperado: el job **Desplegar en Render y verificar** pide el despliegue, espera a que `https://<url-de-render>/actuator/info` muestre el SHA nuevo y corre la prueba de humo y la carga corta. Termina en verde con el resumen `Imagen ... con humo y carga corta en verde`. Desde aquí, cada merge a `main` se despliega solo.

### 5.8 Protección de `main` (recomendado)

**Settings** → **Rules** → **Rulesets** → **New branch ruleset** para `main`: pull request obligatorio con 1 aprobación y estos checks obligatorios (aparecen después de su primera corrida): `construccion`, `pruebas-unitarias`, `reglas-arquitectura`, `pruebas-integracion`, `Secretos`, `SAST (java-kotlin)`, `SAST (javascript-typescript)`.

### 5.9 Datos iniciales y primer administrador

- **Datos iniciales:** con `PETFINDER_DATOSEJEMPLO` sin definir, la app carga cuatro casos de ejemplo la primera vez, solo si la base está vacía. Para empezar sin ejemplos, define `PETFINDER_DATOSEJEMPLO=false` **antes** del primer arranque. Para quitarlos después, ciérralos con el código de administrador.
- **Primer administrador:** genera el código y guárdalo en un gestor de contraseñas:
  ```bash
  openssl rand -base64 24
  ```
  Ponlo en `PETFINDER_CODIGO_ADMIN` en el panel de Render y guarda los cambios: Render reinicia el servicio. Con ese código se puede resolver o cerrar cualquier caso. Cada caso publicado recibe además su propio código de gestión, que la app le muestra una sola vez a quien lo publica.

## 6. Verificación

1. **Salud y versión:**
   ```bash
   curl -fsS https://<url-de-render>/actuator/health/readiness
   curl -fsS https://<url-de-render>/actuator/info
   ```
   Esperado: `{"status":"UP"}`, y en `info`, `commit` igual al último SHA de `main`. En el plan gratis, si la app dormía, la primera respuesta tarda cerca de un minuto.
2. **Prueba de humo del flujo** (salud, consola H2 cerrada, publicar, ver detalle, ver en la lista y cerrar):
   ```bash
   sh scripts/humo-flujo.sh https://<url-de-render>
   ```
   Esperado: seis líneas `ok` y salida 0. Si la app estaba dormida, sube la espera: `HUMO_ESPERA=180 sh scripts/humo-flujo.sh https://<url-de-render>`.
3. **Recorrido corto en el navegador:**
   - Abre `https://<url-de-render>` → **Reportar** → publica un caso de prueba. Guarda el código de gestión que muestra la app.
   - El caso aparece en el mapa y en la lista, con el contacto enmascarado.
   - Ábrelo y ciérralo con su código de gestión (o con `PETFINDER_CODIGO_ADMIN`). Deja de aparecer en el mapa. **[Comprobar el recorrido de cierre en la interfaz cuando entre A9]**.
4. **Datos persistentes:** en Render → servicio → **Manual Deploy** → **Restart service**. El caso de prueba sigue ahí (cerrado): la base es PostgreSQL, no memoria.

## 7. Operación

**Publicar una versión nueva.** Todo cambio entra por pull request a `main`; al integrarlo, `CD` construye, publica y despliega solo. Para marcar una versión:
```bash
git switch main && git pull
git tag -a v3.0.1 -m "v3.0.1: <qué cambia>"
git push origin v3.0.1
```
Esperado: la imagen queda también como `ghcr.io/<usuario-github>/pet-finder:v3.0.1`. Anota el cambio en `CHANGELOG.md`.

**Revertir a una versión anterior.** **Actions** → **Revertir** → **Run workflow** → en `version`, el tag (`v3.0.0`) o el SHA completo de un commit de `main`. Con GitHub CLI:
```bash
gh workflow run revertir.yml -f version=v3.0.0 --repo <usuario-github>/pet-finder-dyas
```
No reconstruye nada: despliega la imagen que ya existe en GHCR, espera a que `/actuator/info` muestre su commit y corre humo y carga corta. Solo funciona con versiones que tienen imagen publicada (las anteriores al pipeline no la tienen).

**Copia de seguridad y restauración en local (Compose)** [sin probar todavía]:
```bash
docker compose exec -T db sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' > respaldo-$(date +%F).dump
docker compose exec -T db sh -c 'pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --clean --if-exists' < respaldo-AAAA-MM-DD.dump
```
En PowerShell, `>` y `<` alteran archivos binarios: corre estos dos comandos desde Git Bash.

**Copia de seguridad en Render.** En la base → **Connections**, copia la **External Database URL** (`postgresql://...`). Con un cliente de PostgreSQL 18 (por ejemplo la misma imagen de Docker):
```bash
docker run --rm -v "$PWD:/respaldo" postgres:18.6-alpine pg_dump "<external-database-url>" -Fc -f /respaldo/render-$(date +%F).dump
docker run --rm -v "$PWD:/respaldo" postgres:18.6-alpine pg_restore -d "<external-database-url>" --clean --if-exists /respaldo/render-AAAA-MM-DD.dump
```
La URL externa lleva la contraseña: no la guardes en el historial de la terminal ni en archivos del repositorio. En los planes pagos, Render además hace copias automáticas.

**Cambiar un secreto.**
- Variable de la app (por ejemplo `PETFINDER_CODIGO_ADMIN` o la contraseña de la base): Render → servicio → **Environment** → editar → **Save Changes**. Render reinicia el servicio solo.
- Deploy hook filtrado: en Render, **Settings** → **Deploy Hook** → **Regenerate**, y actualiza `RENDER_DEPLOY_HOOK_URL` en el entorno `produccion` de GitHub (paso 5.7). **[Comprobar el nombre del botón]**.
- Clave de Anthropic filtrada: revócala en la consola de Anthropic, crea otra y cámbiala en Render.

**Logs y reinicio.**
- Producción: Render → servicio → **Logs** (en vivo). Reinicio: **Manual Deploy** → **Restart service**.
- Local: `docker compose logs -f app` y `docker compose restart app`.
- Qué versión corre: `curl -fsS https://<url-de-render>/actuator/info`.

## 8. Solución de problemas

| Síntoma | Causa | Solución |
|---|---|---|
| `docker compose up` dice `Defina POSTGRES_USER (ver .env.example)` | No existe `.env` | `cp .env.example .env` y cambia `POSTGRES_PASSWORD` |
| `docker compose up` falla con `bind: address already in use` o `port is already allocated` en 8080 | Otro programa usa el puerto (otra app, `./mvnw spring-boot:run`, otro Compose) | Detén el otro programa, o cambia el puerto publicado en `docker-compose.yml` (`"8081:8080"`) y usa `http://localhost:8081` |
| `container pet-finder-app-1 is unhealthy` y en los logs `password authentication failed for user "petfinder"` | El volumen de PostgreSQL se creó antes con otra contraseña: PostgreSQL solo la toma la primera vez | `docker compose down -v` (borra la base local) y vuelve a levantar |
| La app no arranca y Flyway dice que encontró tablas que no creó (`non-empty schema` o fallo de validación) | Volumen local creado antes de Flyway, cuando Hibernate creaba las tablas | `docker compose down -v` y vuelve a levantar. En Render, nunca crees tablas a mano: deja que Flyway las cree |
| La prueba de humo o el job de despliegue fallan con `readiness respondió 000` contra Render | El plan gratis estaba dormido y no alcanzó a despertar | Repite con `HUMO_ESPERA=180`; si pasa seguido, plan Starter |
| Al cerrar o resolver un caso la API responde 403 | Falta el código de gestión del caso o el de administrador, o es incorrecto | Usa el código que mostró la app al publicar, o `PETFINDER_CODIGO_ADMIN` |
| El asistente o la voz responden 429 | Se pasó el límite de peticiones de S10 | Espera la ventana del límite. Si es uso legítimo, sube `PETFINDER_LIMITE_*` en Render |
| El mapa se ve, pero con el estilo de OpenStreetMap y no el de CARTO | `PETFINDER_CLAVE_CARTO` vacía o mal escrita | Ponla en el panel de Render (o en `.env`) y reinicia. `curl https://<url-de-render>/js/config-local.js` debe mostrar `window.PETFINDER_CLAVE_CARTO = '...'` |
| La app en Render no arranca: `Connection refused` o `Unable to obtain connection from database`, después de un mes funcionando | La base gratis venció a los 30 días | Pasa la base a un plan pago dentro de los 14 días de gracia. Si ya se borró, crea otra (5.5), actualiza `SPRING_DATASOURCE_*` y restaura la última copia (parte 7) |
| `SPRING_DATASOURCE_URL` puesta y la app falla con `Driver ... claims to not accept jdbcUrl` | Se copió la URL `postgresql://...` de Render tal cual | Usa el formato `jdbc:postgresql://<host>:5432/<base>` y el usuario y la clave en sus variables (5.5) |
| El pipeline queda en rojo en `SCA` o en `imagen-y-escaneo` sin que nadie tocara el código | Salió una vulnerabilidad nueva en una dependencia o en la imagen base (la base de Trivy se actualiza cada día) | Abre el reporte (`reporte-sca` o `reporte-imagen`), sube la versión corregida en `pom.xml` o en el `Dockerfile` y abre un PR. Nunca bajes el umbral |
| Un check falla con `429 Too Many Requests` de `repo.maven.apache.org` | Maven Central limitó al runner de GitHub, no es un problema del código | **Re-run failed jobs** en la corrida, o `gh run rerun <id> --failed` |
| `pull access denied` o `unauthorized` al desplegar la imagen en Render | El paquete de GHCR sigue privado | Hazlo público (paso 5.4) |

## 9. Recomendaciones para producción

- **Credenciales:** nunca uses los valores de ejemplo de `.env.example` fuera de tu computador. Genera la contraseña de la base y el código de administrador con `openssl rand`, guárdalos en un gestor de contraseñas y cámbialos si alguien deja el equipo. El deploy hook vive solo en el entorno `produccion` de GitHub.
- **HTTPS:** úsalo siempre (Render lo da). Si pones un dominio propio, configúralo en Render y deja que emita el certificado; la app ya manda `Strict-Transport-Security`.
- **Copias de seguridad:** con la base gratis no hay copias. En producción, plan pago de PostgreSQL (con copias automáticas de Render) y además una copia propia programada con `pg_dump` (parte 7) guardada fuera de Render, y una restauración de prueba cada mes.
- **Tamaño de los recursos:** el plan gratis se duerme y tiene poca CPU; el SLO (p95 menor a 500 ms) queda justo. Para operar, servicio **Starter** o mayor y base de pago, en la misma región. Si crece el uso, sube primero el servicio web y mira el p95 con `k6 run -e BASE_URL=https://<url-de-render> perf/scripts/corta.js`.
- **Datos de ejemplo:** en un despliegue real, `PETFINDER_DATOSEJEMPLO=false` desde el primer arranque.
- **Asistente con Claude:** pon `PETFINDER_IA_TOPE_DIARIO` de acuerdo al presupuesto y vigila el consumo en la consola de Anthropic.
- **Riesgos aceptados:** los que el equipo decidió asumir y cómo se mitigan están en [`docs/seguridad.md`](seguridad.md) **[Comprobar el enlace cuando entre S11]**. Léelos antes de abrir el sistema a público real.
- **Dependabot:** revisa y fusiona sus PR cada semana; pasan por el mismo pipeline.
