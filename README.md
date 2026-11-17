# Pet Finder

[![CI](https://github.com/DrearSanti/pet-finder-dyas/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/DrearSanti/pet-finder-dyas/actions/workflows/ci.yml?query=branch%3Amain) [![Seguridad](https://github.com/DrearSanti/pet-finder-dyas/actions/workflows/seguridad.yml/badge.svg?branch=main)](https://github.com/DrearSanti/pet-finder-dyas/actions/workflows/seguridad.yml?query=branch%3Amain) [![CD](https://github.com/DrearSanti/pet-finder-dyas/actions/workflows/cd.yml/badge.svg?branch=main)](https://github.com/DrearSanti/pet-finder-dyas/actions/workflows/cd.yml?query=branch%3Amain)

Proyecto de Diseño y Arquitectura de Software (cortes 1, 2 y 3). Universidad de La Sabana.
Santiago Escobar · Antonio Benítez · Mateo Ramírez

Pet Finder conecta a quien perdió una mascota con quien la vio o la encontró: se cuenta lo que pasó hablando o escribiendo, el asistente arma el reporte y el caso aparece en un mapa.

**App en vivo: [pet-finder-akac.onrender.com](https://pet-finder-akac.onrender.com)** — se abre en el navegador del computador o del celular y se instala como app (PWA) desde Safari o Chrome.

> Cada commit que entra a `main` se despliega solo: el pipeline construye la imagen, la publica en GitHub Container Registry, la despliega en Render y comprueba que responde y que cumple el SLO ([cómo corre el pipeline](#cómo-corre-el-pipeline)). [`/actuator/info`](https://pet-finder-akac.onrender.com/actuator/info) dice qué commit está en producción. Los casos viven en PostgreSQL y sobreviven a los reinicios. Si la app está en el plan gratis de Render, se duerme tras 15 minutos sin uso y la primera visita tarda cerca de un minuto en despertarla.

Lo que se puede hacer: reportar una mascota perdida o encontrada con la voz o escribiendo (el asistente con Claude llena la tarjeta y pone el pin en el lugar que se nombra), ver los casos en el mapa y los que están **cerca de ti**, dejar una pista (**La vi**), avisar que la tienes (**La tengo yo**), recibir **posibles coincidencias** al reportar un hallazgo y cerrar el caso (**Ya apareció**).

## Empieza aquí

| Si quieres… | Ve a |
|---|---|
| Probar la app | [pet-finder-akac.onrender.com](https://pet-finder-akac.onrender.com) |
| Correrla en tu computador | [Cómo ejecutar](#cómo-ejecutar), con Java o con [Docker Compose y PostgreSQL](#con-docker-compose-app-y-postgresql) |
| Entender el pipeline y los despliegues | [Cómo corre el pipeline](#cómo-corre-el-pipeline) |
| Ver dónde está cada criterio del Corte 3 | [Mapa de la rúbrica](#mapa-de-la-rúbrica-del-corte-3) |
| Entender la arquitectura | [`docs/arquitectura.md`](docs/arquitectura.md) y los [ADR](docs/adr/) |
| Ver las pruebas y sus resultados | [`docs/pruebas.md`](docs/pruebas.md), [`perf/README.md`](perf/README.md) y la [tabla de trazabilidad](#tabla-de-trazabilidad-del-corte-2) |
| Leer el problema y el diseño del Corte 1 (SOLID, patrones, UML, cómic) | [`docs/corte-1/`](docs/corte-1/README.md) |
| Ver cómo nos repartimos el trabajo | [Cómo trabajamos](#cómo-trabajamos) |
| Encontrar cualquier documento | [Índice de `docs/`](docs/README.md) |

## Mapa de la rúbrica del Corte 3

Dónde está cada criterio. El reparto de tareas está en el [plan del Corte 3](docs/plan-corte-3.md) y lo que no se alcanzó, en los riesgos aceptados de [`docs/seguridad.md`](docs/seguridad.md#4-riesgos-aceptados) y en [`docs/evolucion.md`](docs/evolucion.md).

| Criterio | Puntos | Dónde se ve | Tareas |
|---|---|---|---|
| 1. Contenerización | 5 | [`Dockerfile`](Dockerfile), [`docker-compose.yml`](docker-compose.yml), [`.env.example`](.env.example) | A3, A7 |
| 2. Integración continua | 15 | [`.github/workflows/ci.yml`](.github/workflows/ci.yml), protección de `main` ([`docs/devops.md`](docs/devops.md)), [`CHANGELOG.md`](CHANGELOG.md) y las [versiones](https://github.com/DrearSanti/pet-finder-dyas/releases) | A1, A2, A6, A8, S1, S2, M11 |
| 3. Arquitectura verificada en el pipeline | 15 | [`ReglasArquitecturaTest`](src/test/java/petfinder/arquitectura/ReglasArquitecturaTest.java), [`perf/scripts/corta.js`](perf/scripts/corta.js) después de cada despliegue, [tabla de trazabilidad](#tabla-de-trazabilidad-del-corte-2) | S4, M4, M7, S15 |
| 4. DevSecOps | 20 | [`docs/seguridad.md`](docs/seguridad.md) (amenazas y hallazgos), [`.github/workflows/seguridad.yml`](.github/workflows/seguridad.yml), [ADR-003](docs/adr/ADR-003-seguridad-del-pipeline.md), el pull request bloqueado | S5, S6, A5, A6, S11, S12, S13 |
| 5. Despliegue continuo | 15 | [`.github/workflows/cd.yml`](.github/workflows/cd.yml), [`.github/workflows/revertir.yml`](.github/workflows/revertir.yml), [ADR-004](docs/adr/ADR-004-estrategia-de-despliegue.md), la [imagen en GHCR](https://github.com/DrearSanti/pet-finder-dyas/pkgs/container/pet-finder) y la app en Render | M5, M6, M7, M8, S3 |
| Documentación y repositorio | 10 | Este README, [`docs/devops.md`](docs/devops.md), [`docs/seguridad.md`](docs/seguridad.md), la [vista de despliegue](docs/arquitectura.md#8-vista-de-despliegue-corte-3), [`docs/adr/`](docs/adr/) y [`docs/evolucion.md`](docs/evolucion.md) | A12, S11, S13, S14, M12, M13 |
| Guía de implementación | 10 | [`docs/implementacion.md`](docs/implementacion.md) | A13 |
| Presentación y demostración | 10 | En vivo: un cambio que se despliega y otro que el pipeline bloquea ([guion](docs/presentacion-corte-3.md)) | M15 y los tres |
| Bono: DAST con OWASP ZAP | +5 | No se hizo: se priorizó dejar el CD en verde | M14 |
| Bono: observabilidad | +5 | No se hizo | B1 |

---

## Cómo ejecutar

Esta sección lleva de un repositorio recién clonado a la app corriendo y a cada tipo de prueba ejecutado. No hace falta instalar Maven ni una base de datos: el proyecto trae el **Maven Wrapper** (`mvnw`) y, corriendo con Java, usa H2 en memoria. Si prefieres no instalar Java, [Docker Compose](#con-docker-compose-app-y-postgresql) levanta la app con PostgreSQL, como en producción.

### Requisitos

| Qué | Para qué | Cómo comprobarlo |
|---|---|---|
| **JDK 17** | Compilar y correr la app | `java -version` debe decir 17 (con el wrapper: `./mvnw -v` → `Java version: 17`) |
| Git | Clonar el repositorio. En Windows trae **Git Bash**, la terminal recomendada | `git --version` |
| Safari, Chrome o Edge | Usar la app. La voz funciona en los tres (en Safari, con el dictado activado); en Firefox se escribe | — |
| Google Chrome (opcional) | Solo para las pruebas de interfaz con Selenium (`-Pui`) | — |
| k6 (opcional) | Solo para las pruebas de carga | `k6 version` |
| Docker Desktop (opcional) | Solo para correr con [Docker Compose](#con-docker-compose-app-y-postgresql); trae Docker Compose v2 | `docker compose version` |

La primera vez que corras `mvnw` descarga Maven 3.9.16 y las dependencias: necesita internet y tarda unos minutos. Después funciona sin conexión.

### Los mismos comandos en cada terminal

En **macOS, Linux y Git Bash** (Windows) se usa `./mvnw`. En **PowerShell** se usa `.\mvnw.cmd` con los mismos argumentos. En PowerShell, un argumento que lleva `-D` y un punto o una coma va entre comillas: `.\mvnw.cmd test "-Dtest=MapeoDtoTest"`.

### 1. Clonar y configurar

```bash
git clone https://github.com/DrearSanti/pet-finder-dyas.git
cd pet-finder-dyas
cp .env.example .env
```

En PowerShell el último paso es `Copy-Item .env.example .env`. El archivo `.env` es opcional y nunca se sube al repositorio. Solo hace falta si quieres usar el asistente con Claude: pon tu clave en `ANTHROPIC_API_KEY`. **Sin clave la app funciona completa**: el asistente entiende las frases con reglas (expresiones regulares).

### 2. Correr la app

| macOS / Git Bash | PowerShell |
|---|---|
| `./mvnw spring-boot:run` | `.\mvnw.cmd spring-boot:run` |

Cuando la consola diga `Started PetFinderApplication`, abre **http://localhost:8080** en Safari, Chrome o Edge. La app arranca con algunos casos de ejemplo. Para detenerla, `Ctrl + C`.

Para probar la voz, permite el micrófono cuando el navegador lo pida. Si tu navegador no tiene reconocimiento de voz (Firefox, por ejemplo), el campo de texto hace lo mismo.

Otras formas de correrla:

| Para qué | macOS / Git Bash | PowerShell |
|---|---|---|
| Jar empaquetado | `./mvnw -q -DskipTests package` y luego `java -jar target/pet-finder.jar` | `.\mvnw.cmd -q -DskipTests package` y luego `java -jar target\pet-finder.jar` |
| Otro puerto | `PORT=9090 ./mvnw spring-boot:run` | `$env:PORT="9090"; .\mvnw.cmd spring-boot:run` |
| Demo de consola del Corte 1 | `./mvnw -q compile exec:java` | `.\mvnw.cmd -q compile exec:java` |
| Menú de consola | `./mvnw -q compile exec:java -Dexec.args="--menu"` | `.\mvnw.cmd -q compile exec:java "-Dexec.args=--menu"` |
| Docker, solo la app con H2 | `docker build -t pet-finder .` y luego `docker run -p 8080:8080 pet-finder` | igual |
| Docker Compose, la app con PostgreSQL | Ver [la sección siguiente](#con-docker-compose-app-y-postgresql) | igual |

**Consola de la base de datos:** viene apagada, para que nadie la abra en el sistema desplegado. En tu computador se prende con el perfil `local`: `./mvnw spring-boot:run -Dspring-boot.run.profiles=local` (PowerShell: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"`). Luego abre http://localhost:8080/h2-console, JDBC URL `jdbc:h2:mem:petfinder`, usuario `sa`, sin contraseña.

### Con Docker Compose (app y PostgreSQL)

Levanta lo mismo que corre en producción: la imagen del [`Dockerfile`](Dockerfile) y una base PostgreSQL 18 con su volumen. Solo necesitas Docker Desktop abierto; no hace falta Java. Los comandos son iguales en macOS, Git Bash y PowerShell, salvo el `cp` del paso 1.

1. **Configura la base**, una sola vez. Copia la plantilla (`cp .env.example .env`; en PowerShell, `Copy-Item .env.example .env`) y en `.env` pon un usuario en `POSTGRES_USER` y una clave tuya en `POSTGRES_PASSWORD`. Si falta alguna de las dos, Compose se detiene y dice cuál. `ANTHROPIC_API_KEY` sigue siendo opcional.
2. **Construye y arranca:** `docker compose up -d --build`. La primera vez compila el proyecto dentro de Docker y tarda unos minutos.
3. **Espera a que esté sana:** `docker compose ps` debe mostrar `db` como `healthy` y `app` arriba. La app espera a la base y Flyway crea las tablas al arrancar.
4. **Ábrela** en **http://localhost:8080**. Para comprobarlo sin navegador: `curl http://localhost:8080/actuator/health` responde `{"status":"UP"}`.

| Para qué | Comando |
|---|---|
| Ver los registros de la app | `docker compose logs -f app` |
| Reiniciar (los casos se conservan) | `docker compose restart` |
| Apagar sin borrar los datos | `docker compose down` |
| Apagar y borrar la base | `docker compose down -v` |

Si el puerto 8080 está ocupado, detén la app que corre con `mvnw` o cambia `"8080:8080"` por `"9090:8080"` en [`docker-compose.yml`](docker-compose.yml) y abre http://localhost:9090.

### 3. Correr las pruebas

| Tipo | macOS / Git Bash | PowerShell | Qué corre |
|---|---|---|---|
| Unitarias, arquitectura y capa web | `./mvnw test` | `.\mvnw.cmd test` | Todas las clases `*Test`, en segundos, sin levantar servidor |
| Integración y sistema, con cobertura | `./mvnw verify` | `.\mvnw.cmd verify` | Lo anterior más las clases `*IT` (H2 real y la app completa por HTTP). Reporte de cobertura en `target/site/jacoco/index.html` |
| Interfaz (necesita Google Chrome instalado) | `./mvnw -q -Pui verify` | `.\mvnw.cmd -q -Pui verify` | Todo lo de `verify` más las clases `*UIT`: dos flujos con Selenium en un Chrome sin ventana. Selenium descarga solo el driver de Chrome la primera vez. Sin `-Pui` estas pruebas no corren, así que una máquina sin Chrome pasa igual |
| Carga | `k6 run perf/scripts/carga.js` | `k6 run perf/scripts/carga.js` | 50 usuarios contra la app, que debe estar corriendo en otra terminal |

Para repetir todo desde cero, como hace el equipo antes de cada entrega: `./mvnw clean verify`. La estrategia de cada nivel, los resultados y la matriz de casos están en [`docs/pruebas.md`](docs/pruebas.md); el detalle de carga, en [`perf/README.md`](perf/README.md). Las pruebas nunca llaman a Claude ni necesitan clave.

### Problemas comunes en Windows

| Síntoma | Causa | Qué hacer |
|---|---|---|
| `PKIX path building failed` al descargar dependencias | Un antivirus o proxy revisa las conexiones HTTPS con un certificado que Windows conoce pero Java no | Corre una vez con `MAVEN_OPTS="-Djavax.net.ssl.trustStoreType=Windows-ROOT" ./mvnw -U verify`; las siguientes ya usan lo descargado |
| La app o las pruebas `*IT` fallan al arrancar con `Unable to establish loopback connection` / `Invalid argument: connect` | Java no logra crear su socket interno en la carpeta temporal de Windows | Apunta ese socket a otra carpeta: `JAVA_TOOL_OPTIONS="-Djdk.net.unixdomain.tmpdir=C:\Users\Public"` antes del comando (en PowerShell: `$env:JAVA_TOOL_OPTIONS="-Djdk.net.unixdomain.tmpdir=C:\Users\Public"`) |
| `./mvnw: Permission denied` en Git Bash | El archivo perdió el permiso de ejecución | `sh mvnw test`, o `chmod +x mvnw` |
| Una prueba falla después de abrir el proyecto en VS Code u otro IDE | El IDE recompiló `target/` con su propio compilador | Repite con `clean`: `./mvnw clean verify` |

## Cómo corre el pipeline

Todo pasa en GitHub Actions; nadie despliega desde su computador. Un cambio recorre este camino:

```
rama corte3/<id> ──push──► CI + Seguridad ──► pull request ──► aprobación de otro integrante ──► merge a main
                                                                                                   │
            CD: publicar imagen ghcr.io/drearsanti/pet-finder:<SHA> ──► desplegar en Render ◄──────┘
                                                                         │
                         esperar el SHA en /actuator/info ──► prueba de humo ──► carga corta (SLO)
```

| Workflow | Cuándo corre | Qué hace | Si falla |
|---|---|---|---|
| [CI](.github/workflows/ci.yml) | Cada push a cualquier rama, cada pull request a `main`, los tags `v*`, todos los días a las 6:00 de Bogotá y a mano | `construccion` compila y empaqueta; después, en paralelo, `pruebas-unitarias` (`*Test`, con JaCoCo), `reglas-arquitectura` (ArchUnit), `pruebas-integracion` (`*IT` con H2 y la app por HTTP) y `pruebas-ui` (Selenium con Chrome sin ventana). Cada job publica sus reportes como artefacto | El pull request no se puede integrar: los cuatro primeros jobs son checks obligatorios de `main` |
| [Seguridad](.github/workflows/seguridad.yml) | Los mismos eventos que CI | `Secretos` revisa todo el historial con Gitleaks; `SAST` analiza Java y JavaScript con CodeQL y se detiene ante un hallazgo de severidad 7.0 o más sin tratar | El pull request queda bloqueado. El detalle está en `docs/seguridad.md` |
| [CD](.github/workflows/cd.yml) | Cada push a `main` (solo llega por pull request) y cada tag `v*` | `Publicar imagen` construye el `Dockerfile`, publica la imagen con el SHA del commit (y con la versión si es un tag, nunca `latest`) y comprueba que arranca y reporta ese SHA. En `main`, `Desplegar en Render y verificar` llama al deploy hook de Render dentro del entorno `produccion`, espera hasta 10 minutos a que la URL pública reporte el SHA nuevo, corre la prueba de humo y [`perf/scripts/corta.js`](perf/scripts/corta.js), y sube el resultado de k6 como artefacto | La corrida queda en rojo y se ve en el badge. Si la versión nueva quedó mal, se revierte |
| [Revertir](.github/workflows/revertir.yml) | A mano: pestaña **Actions** → **Revertir** → **Run workflow**, con un tag (`v2.1.0`) o el SHA completo de un commit de `main` | Comprueba que esa imagen existe en GHCR, la despliega con el mismo deploy hook, espera su SHA en `/actuator/info` y corre la prueba de humo y la de carga corta | Producción sigue en la versión que tenía. Nunca corre al mismo tiempo que un despliegue del CD |
| [Carga larga](.github/workflows/carga-larga.yml) | A mano | Levanta la app con Docker Compose y PostgreSQL dentro del runner y corre [`perf/scripts/carga.js`](perf/scripts/carga.js) (50 usuarios, 4 min 30 s) sin tocar producción | No bloquea nada: es la medición completa del SLO para el informe |

El SLO es el del Corte 2: p95 por debajo de 500 ms, menos de 1 % de errores y al menos 30 peticiones por segundo. Está explicado en [`perf/README.md`](perf/README.md).

**Para lanzar un workflow a mano** sin abrir el navegador, con [GitHub CLI](https://cli.github.com/): `gh workflow run ci.yml --ref main`, o `gh workflow run revertir.yml -f version=v2.1.0`. Con `gh run watch` sigues la corrida desde la terminal.

**Versiones.** Una versión es un tag `vX.Y.Z` sobre un commit de `main` con el CI en verde. Al empujarlo, el CD publica `ghcr.io/drearsanti/pet-finder:vX.Y.Z`, que es lo que después se puede pasar a **Revertir**. Lo que cambió en cada versión va en `CHANGELOG.md` y en [GitHub Releases](https://github.com/DrearSanti/pet-finder-dyas/releases).

**Secretos.** El deploy hook de Render vive solo como secreto del entorno `produccion` en GitHub. Las claves de Anthropic, de CARTO y de PostgreSQL viven en el panel de Render. Ninguna está en el repositorio ni en la imagen, y Gitleaks lo vigila en cada push.

## Cómo trabajamos

**Corte 3.** El reparto, las decisiones y el estado de cada tarea están en [`docs/plan-corte-3.md`](docs/plan-corte-3.md). Cada tarea va en su rama (`corte3/<id>`, por ejemplo `corte3/M1`) y entra a `main` por un pull request que revisa otro integrante y que se integra con *merge commit*. Quien hizo la tarea hace su commit: la autoría queda en el historial.

**Corte 2.** El trabajo se organizó con un plan de construcción (blueprint) por tareas y épicas: [cómo nos organizamos](docs/corte-2/organizacion.md).

---

## Arquitectura y trazabilidad del Corte 2

El Corte 2 atiende dos retos, **menú interactivo** y **captura de voz**, sin cambiar las reglas del Corte 1: reorganiza el núcleo en arquitectura hexagonal y le agrega entradas (API REST, página web instalable con mapa, voz, asistente) y salidas (H2, Claude) intercambiables.

| Documento | Qué contiene |
|---|---|
| [`docs/arquitectura.md`](docs/arquitectura.md) | El documento de arquitectura con las 7 secciones del enunciado |
| [`docs/adr/ADR-001-estilo-arquitectonico.md`](docs/adr/ADR-001-estilo-arquitectonico.md) | Por qué hexagonal: Capas, MVC, Hexagonal y Microservicios contra criterios de los retos |
| [`docs/adr/ADR-002-extraccion-de-intencion.md`](docs/adr/ADR-002-extraccion-de-intencion.md) | Por qué Claude con respaldo regex y no Laya |
| [`docs/diagramas/c4.md`](docs/diagramas/c4.md) | Arquitectura del Corte 1 frente a los C4 del Corte 2 |
| [`docs/pruebas.md`](docs/pruebas.md) | Estrategia por nivel, matriz de casos y hallazgos |
| [`perf/README.md`](perf/README.md) | SLO, escenario, resultados de carga y cuello de botella |

### Tabla de trazabilidad del Corte 2

Ampliada en el Corte 3 con dos columnas: la etapa del pipeline que vuelve a verificar cada reto en cada cambio y el umbral que lo detiene. Las demás columnas son las del Corte 2 y no cambian. Los nombres de las etapas son los checks obligatorios del ruleset de `main` ([`docs/devops.md`](docs/devops.md)).

Números de `./mvnw clean -Pui verify` del 2026-09-27 sobre `main` (commit `bd67f12`) (Surefire, Failsafe y JaCoCo) y de las corridas de k6 en `perf/resultados/`.

| Reto | Atributo de calidad | Decisión arquitectónica | Dónde está | Prueba que lo evidencia | Resultado | Etapa del pipeline | Umbral que detiene el pipeline |
|---|---|---|---|---|---|---|---|
| Menú interactivo | Usabilidad y modificabilidad | La web es un adaptador de entrada más: los controladores solo conocen puertos de entrada | `adaptadores/entrada/web/` (`ReporteController`, `AvistamientoController`), `static/index.html`, `static/js/app.js` | `ControladoresWebTest`, `FlujoReportePerdidaSistemaIT` | 17 de 17 y 6 de 6 en verde. Ciclo completo por HTTP: 201 → ACTIVO → 201 → 204 → RESUELTO → 409; errores 400, 404 y 409 con `{"error": …}` | Modificabilidad: `reglas-arquitectura` y `pruebas-integracion` (CI, en cada PR). Usabilidad: **sin verificación automática**, porque se evalúa con personas en la demo | Una regla de ArchUnit violada (por ejemplo `lasEntradasSoloHablanConPuertos`) o cualquier prueba de integración o sistema en rojo |
| Menú interactivo: **mapa con coordenadas**. *Funcionalidad agregada porque el reto la exige*: el menú interactivo pide ver los casos cercanos, y eso necesita ubicarlos | Usabilidad y privacidad | `Ubicacion` gana latitud y longitud opcionales y juntas; la lista pública las redondea con `aproximada()` | `domain/model/Ubicacion.java`, `static/js/mapa.js` | `UbicacionTest`, `FlujoReportePerdidaSistemaIT.listaEnmascaraYDetalleNo` | 10 de 10 en verde; la lista publica coordenadas a 3 decimales (unos 100 m) y el contacto enmascarado (`300•••••67`) | `pruebas-unitarias` y `pruebas-integracion` (CI) | Una prueba en rojo (`UbicacionTest`, `FlujoReportePerdidaSistemaIT`) o cobertura de líneas del dominio por debajo de 70 % |
| Menú interactivo: **Cerca de ti**. *Funcionalidad agregada porque el reto la exige*: el menú interactivo pide ver los casos cercanos; "Mi ubicación" filtra los que están a menos de 5 km | Usabilidad y privacidad | El filtro y la distancia se calculan en el navegador; la posición de la persona nunca se envía al servidor | `static/js/app.js`, `static/js/mapa.js` | Sin prueba automática; se verifica a mano en la demo | **Resuelto con un límite:** falta una prueba de interfaz que lo cubra (sección 7) | **Sin verificación automática**: el filtro corre en el navegador con la ubicación real y no tiene prueba de interfaz (A11 no se alcanzó) | — |
| Menú interactivo: **La tengo yo** y **Ya apareció**. *Funcionalidad agregada porque el reto la exige*: el menú lleva al navegador las operaciones del Corte 1; avisar que alguien tiene la mascota es una variante del avistamiento, y "Ya apareció" usa el `resolver()` que ya existía | Usabilidad y seguridad | `Avistamiento` gana `TipoAvistamiento` (`LA_VI`, `LA_TENGO`); un hallazgo exige contacto y el caso sigue activo hasta que la familia confirma | `domain/model/Avistamiento.java`, `domain/model/TipoAvistamiento.java`, `ReportePerdida.laTieneAlguien()` | `HallazgoTest`, `FlujoHallazgoSistemaIT` | 3 de 3 y 3 de 3 en verde; "La tengo yo" sin contacto responde 400 y no toca el caso | `pruebas-unitarias` y `pruebas-integracion` (CI) | Una prueba en rojo (`HallazgoTest`, `FlujoHallazgoSistemaIT`) |
| Menú interactivo: **posibles coincidencias**. *Funcionalidad agregada*, con relación indirecta al reto: al reportar un hallazgo, la interfaz sugiere hasta 3 casos perdidos por nombre, o por especie y cercanía | Usabilidad | Heurística en el navegador; solo sugiere y la persona decide | `static/js/app.js` (`posiblesCoincidencias`) | Sin prueba automática | **Declarado como límite:** el plan del corte la había dejado fuera de alcance y no hay prueba que la respalde (sección 7) | **Sin verificación automática**: es una heurística del navegador sin prueba de interfaz (A11 no se alcanzó) | — |
| Captura de voz | Rendimiento | El servidor solo recibe texto; `InterpreteComandoVoz` usa reglas (regex) y no un modelo, para responder en microsegundos | `adaptadores/entrada/web/VozController.java`, `application/service/InterpreteComandoVoz.java`, `ServicioComandosVoz.java` | k6 `perf/scripts/carga.js` (50 usuarios, 4 min 30 s) | **Cumple el SLO**: p95 de 89,2 ms (objetivo ≤ 500 ms), 0 % de errores en 21.442 peticiones (objetivo < 1 %), 79,2 req/s (objetivo ≥ 30) | Capacidad: `despliegue-y-verificacion` (CI, en cada PR), con `perf/scripts/corta.js` contra la misma imagen y PostgreSQL en el runner. Plataforma real: despliegue → prueba de carga en `cd.yml`, contra Render, después de la prueba de humo | En el PR: p95 ≥ 500 ms, errores ≥ 1 % o menos de 1800 peticiones en 60 s (30 req/s). Contra Render gratis, a 6 req/s durante 330 s: p95 ≥ 500 ms, errores ≥ 1 %, menos de 1800 peticiones o alguna iteración perdida ([ADR-004](docs/adr/ADR-004-estrategia-de-despliegue.md)) |
| Captura de voz | Modificabilidad | La voz publica por `GestionReportes`, el mismo puerto que el formulario y la consola | `application/port/entrada/ProcesadorComandosVoz.java`, `config/ConfiguracionPetFinder.java` | `FlujoVozSistemaIT`, `InterpreteComandoVozTest`, `ServicioComandosVozTest` | 4 de 4, 26 de 26 y 5 de 5 en verde. "Max, perro, Chía" por formulario, voz y asistente produce el mismo reporte. **Resuelto con un límite:** la transcripción depende de Web Speech, así que la voz funciona en Chrome, Edge y Safari (Mac y iPhone, con el dictado activado) y necesita internet; en Firefox la misma frase se escribe en el campo de texto. Faltaría transcribir en el servidor, que se descartó para no procesar audio (sección 7) | `reglas-arquitectura` y `pruebas-integracion` (CI) | Una regla de ArchUnit violada (`lasEntradasSoloHablanConPuertos`) o `FlujoVozSistemaIT` en rojo |
| Captura de voz: **asistente con tarjeta viva**. *Funcionalidad agregada porque el reto la exige*: la captura de voz necesita completar por turnos lo que una sola frase no dice | Disponibilidad y seguridad | Puerto `ExtractorDatosReporte` con dos adaptadores (Claude primero, regex de respaldo); el asistente no recibe `GestionReportes`, así que nunca publica | `application/service/ServicioAsistente.java`, `adaptadores/salida/ia/ExtractorClaude.java`, `application/service/ExtractorRegex.java`, `static/js/tarjeta-viva.js` | `ServicioAsistenteTest`, `ExtractorRegexTest`, `ExtractorClaudeTest` | 15 de 15, 26 de 26 y 45 de 45 en verde, sin llamar a Claude. Las coordenadas que Claude sugiere para un lugar nombrado se descartan si caen fuera de Colombia. `git diff step-14-h2-integrado step-15-extractor-claude --stat -- src/main/java/petfinder/domain` sale vacío | `reglas-arquitectura` y `pruebas-unitarias` (CI); `SAST (java-kotlin)` (Seguridad) | Se viola `elAsistenteNuncaPublica` o `soloElAdaptadorDeIaUsaElSdkDeAnthropic`, una prueba en rojo, o un hallazgo de CodeQL con severidad 7.0 o más |
| Los dos retos | Modificabilidad y testabilidad | Arquitectura hexagonal verificada con ArchUnit ([ADR-001](docs/adr/ADR-001-estilo-arquitectonico.md)) | `test/.../arquitectura/ReglasArquitecturaTest.java` | `ReglasArquitecturaTest` y JaCoCo | 5 de 5 reglas en verde. Cobertura total: 83,1 % de instrucciones y 83,3 % de ramas; paquete `domain`: 97,0 % y 87,3 % | `reglas-arquitectura` y `pruebas-unitarias` (CI) | Cualquiera de las 9 reglas de ArchUnit violada, o cobertura de líneas del dominio por debajo de 70 % (JaCoCo, `umbral-dominio`) |

**En total:** 253 pruebas automatizadas en verde (227 unitarias, de arquitectura y de capa web; 11 de integración con H2; 13 de sistema por HTTP; 2 de interfaz con Selenium). Cobertura: 83,1 % de instrucciones y 83,3 % de ramas; paquete `domain`: 97,0 %.

### Límites conocidos

- **La voz depende del navegador y de internet** (Web Speech): funciona en Chrome, Edge y Safari; en Firefox el campo de texto hace lo mismo.
- **Plan gratis de Render**: la app se duerme tras 15 minutos sin uso y tarda cerca de un minuto en despertar; al despertar vuelve a los casos de ejemplo.
- **H2 en memoria:** los datos se pierden al reiniciar y la API no puede correr en varias instancias.
- **Sin paginación y con consultas N+1** en la lista de casos: es el cuello de botella que midió la carga.
- **Sin cuentas ni permisos:** cualquiera puede resolver o cerrar un caso.
- **"Cerca de ti" y las posibles coincidencias no tienen prueba automática**; se verifican a mano.
- **Las posibles coincidencias contradicen el plan del corte**, que las dejó fuera de alcance: son solo una sugerencia y decidir es de la persona.
- **Una sola corrida de carga**, con k6 y la app en la misma máquina.

El detalle de cada límite y el trabajo pendiente para el Corte 3 están en la [sección 7 de `docs/arquitectura.md`](docs/arquitectura.md#7-límites-conocidos-del-diseño-y-trabajo-pendiente-para-el-tercer-corte).
