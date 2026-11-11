# Pet Finder

Proyecto de Diseño y Arquitectura de Software (cortes 1, 2 y 3). Universidad de La Sabana.
Santiago Escobar · Antonio Benítez · Mateo Ramírez

Pet Finder conecta a quien perdió una mascota con quien la vio o la encontró: se cuenta lo que pasó hablando o escribiendo, el asistente arma el reporte y el caso aparece en un mapa.

**App en vivo: [pet-finder-akac.onrender.com](https://pet-finder-akac.onrender.com)** — se abre en el navegador del computador o del celular y se instala como app (PWA) desde Safari o Chrome.

> La app está en el plan gratis de Render: si nadie la usa en 15 minutos se duerme, y la primera visita tarda cerca de un minuto en despertarla. Los datos viven en H2 en memoria, así que cada vez que despierta vuelve a los casos de ejemplo.

Lo que se puede hacer: reportar una mascota perdida o encontrada con la voz o escribiendo (el asistente con Claude llena la tarjeta y pone el pin en el lugar que se nombra), ver los casos en el mapa y los que están **cerca de ti**, dejar una pista (**La vi**), avisar que la tienes (**La tengo yo**), recibir **posibles coincidencias** al reportar un hallazgo y cerrar el caso (**Ya apareció**).

## Empieza aquí

| Si quieres… | Ve a |
|---|---|
| Probar la app | [pet-finder-akac.onrender.com](https://pet-finder-akac.onrender.com) |
| Correrla en tu computador | [Cómo ejecutar](#cómo-ejecutar) |
| Ver dónde está cada criterio del Corte 3 | [Mapa de la rúbrica](#mapa-de-la-rúbrica-del-corte-3) |
| Entender la arquitectura | [`docs/arquitectura.md`](docs/arquitectura.md) y los [ADR](docs/adr/) |
| Ver las pruebas y sus resultados | [`docs/pruebas.md`](docs/pruebas.md), [`perf/README.md`](perf/README.md) y la [tabla de trazabilidad](#tabla-de-trazabilidad-del-corte-2) |
| Leer el problema y el diseño del Corte 1 (SOLID, patrones, UML, cómic) | [`docs/corte-1/`](docs/corte-1/README.md) |
| Ver cómo nos repartimos el trabajo | [Cómo trabajamos](#cómo-trabajamos) |
| Encontrar cualquier documento | [Índice de `docs/`](docs/README.md) |

## Mapa de la rúbrica del Corte 3

Dónde está o va a estar cada criterio. Lo que todavía no existe aparece sin enlace; el avance de cada tarea está en el [plan del Corte 3](docs/plan-corte-3.md).

| Criterio | Puntos | Dónde se ve | Tareas |
|---|---|---|---|
| 1. Contenerización | 5 | [`Dockerfile`](Dockerfile), [`docker-compose.yml`](docker-compose.yml), `.env.example` | A3 |
| 2. Integración continua | 15 | `.github/workflows/ci.yml`, protección de `main`, `CHANGELOG.md` y versiones | A1, A2, A8, S1, S2, M11 |
| 3. Arquitectura verificada en el pipeline | 15 | [`ReglasArquitecturaTest`](src/test/java/petfinder/arquitectura/ReglasArquitecturaTest.java), `perf/scripts/corta.js` después de cada despliegue, [tabla de trazabilidad](#tabla-de-trazabilidad-del-corte-2) | S4, M4, M7, S15 |
| 4. DevSecOps | 20 | `docs/seguridad.md` (amenazas y hallazgos), `.github/workflows/seguridad.yml`, el pull request bloqueado | S5, S6, A5, A6, S11, S12, S13 |
| 5. Despliegue continuo | 15 | `.github/workflows/cd.yml`, `.github/workflows/revertir.yml`, la imagen en GHCR y la app en Render | M5, M6, M7, M8, S3 |
| Documentación y repositorio | 10 | Este README, `docs/devops.md`, `docs/seguridad.md`, la vista de despliegue, `docs/adr/` y `docs/evolucion.md` | A12, S11, S13, S14, M12, M13 |
| Guía de implementación | 10 | `docs/implementacion.md` | A13 |
| Presentación y demostración | 10 | En vivo: un cambio que se despliega y otro que el pipeline bloquea | M15 y los tres |
| Bono: DAST con OWASP ZAP | +5 | Etapa del pipeline contra la app desplegada | M14 |
| Bono: observabilidad | +5 | Opcional, si queda tiempo | B1 |

---

## Cómo ejecutar

Esta sección lleva de un repositorio recién clonado a la app corriendo y a cada tipo de prueba ejecutado. No hace falta instalar Maven ni una base de datos: el proyecto trae el **Maven Wrapper** (`mvnw`) y usa H2 en memoria.

### Requisitos

| Qué | Para qué | Cómo comprobarlo |
|---|---|---|
| **JDK 17** | Compilar y correr la app | `java -version` debe decir 17 (con el wrapper: `./mvnw -v` → `Java version: 17`) |
| Git | Clonar el repositorio. En Windows trae **Git Bash**, la terminal recomendada | `git --version` |
| Safari, Chrome o Edge | Usar la app. La voz funciona en los tres (en Safari, con el dictado activado); en Firefox se escribe | — |
| Google Chrome (opcional) | Solo para las pruebas de interfaz con Selenium (`-Pui`) | — |
| k6 (opcional) | Solo para las pruebas de carga | `k6 version` |

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
| Docker | `docker build -t pet-finder .` y luego `docker run -p 8080:8080 pet-finder` | igual |

**Consola de la base de datos:** con la app corriendo, http://localhost:8080/h2-console, JDBC URL `jdbc:h2:mem:petfinder`, usuario `sa`, sin contraseña.

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

Números de `./mvnw clean -Pui verify` del 2026-09-27 sobre `main` (commit `bd67f12`) (Surefire, Failsafe y JaCoCo) y de las corridas de k6 en `perf/resultados/`.

| Reto | Atributo de calidad | Decisión arquitectónica | Dónde está | Prueba que lo evidencia | Resultado |
|---|---|---|---|---|---|
| Menú interactivo | Usabilidad y modificabilidad | La web es un adaptador de entrada más: los controladores solo conocen puertos de entrada | `adaptadores/entrada/web/` (`ReporteController`, `AvistamientoController`), `static/index.html`, `static/js/app.js` | `ControladoresWebTest`, `FlujoReportePerdidaSistemaIT` | 17 de 17 y 6 de 6 en verde. Ciclo completo por HTTP: 201 → ACTIVO → 201 → 204 → RESUELTO → 409; errores 400, 404 y 409 con `{"error": …}` |
| Menú interactivo: **mapa con coordenadas**. *Funcionalidad agregada porque el reto la exige*: el menú interactivo pide ver los casos cercanos, y eso necesita ubicarlos | Usabilidad y privacidad | `Ubicacion` gana latitud y longitud opcionales y juntas; la lista pública las redondea con `aproximada()` | `domain/model/Ubicacion.java`, `static/js/mapa.js` | `UbicacionTest`, `FlujoReportePerdidaSistemaIT.listaEnmascaraYDetalleNo` | 10 de 10 en verde; la lista publica coordenadas a 3 decimales (unos 100 m) y el contacto enmascarado (`300•••••67`) |
| Menú interactivo: **Cerca de ti**. *Funcionalidad agregada porque el reto la exige*: el menú interactivo pide ver los casos cercanos; "Mi ubicación" filtra los que están a menos de 5 km | Usabilidad y privacidad | El filtro y la distancia se calculan en el navegador; la posición de la persona nunca se envía al servidor | `static/js/app.js`, `static/js/mapa.js` | Sin prueba automática; se verifica a mano en la demo | **Resuelto con un límite:** falta una prueba de interfaz que lo cubra (sección 7) |
| Menú interactivo: **La tengo yo** y **Ya apareció**. *Funcionalidad agregada porque el reto la exige*: el menú lleva al navegador las operaciones del Corte 1; avisar que alguien tiene la mascota es una variante del avistamiento, y "Ya apareció" usa el `resolver()` que ya existía | Usabilidad y seguridad | `Avistamiento` gana `TipoAvistamiento` (`LA_VI`, `LA_TENGO`); un hallazgo exige contacto y el caso sigue activo hasta que la familia confirma | `domain/model/Avistamiento.java`, `domain/model/TipoAvistamiento.java`, `ReportePerdida.laTieneAlguien()` | `HallazgoTest`, `FlujoHallazgoSistemaIT` | 3 de 3 y 3 de 3 en verde; "La tengo yo" sin contacto responde 400 y no toca el caso |
| Menú interactivo: **posibles coincidencias**. *Funcionalidad agregada*, con relación indirecta al reto: al reportar un hallazgo, la interfaz sugiere hasta 3 casos perdidos por nombre, o por especie y cercanía | Usabilidad | Heurística en el navegador; solo sugiere y la persona decide | `static/js/app.js` (`posiblesCoincidencias`) | Sin prueba automática | **Declarado como límite:** el plan del corte la había dejado fuera de alcance y no hay prueba que la respalde (sección 7) |
| Captura de voz | Rendimiento | El servidor solo recibe texto; `InterpreteComandoVoz` usa reglas (regex) y no un modelo, para responder en microsegundos | `adaptadores/entrada/web/VozController.java`, `application/service/InterpreteComandoVoz.java`, `ServicioComandosVoz.java` | k6 `perf/scripts/carga.js` (50 usuarios, 4 min 30 s) | **Cumple el SLO**: p95 de 89,2 ms (objetivo ≤ 500 ms), 0 % de errores en 21.442 peticiones (objetivo < 1 %), 79,2 req/s (objetivo ≥ 30) |
| Captura de voz | Modificabilidad | La voz publica por `GestionReportes`, el mismo puerto que el formulario y la consola | `application/port/entrada/ProcesadorComandosVoz.java`, `config/ConfiguracionPetFinder.java` | `FlujoVozSistemaIT`, `InterpreteComandoVozTest`, `ServicioComandosVozTest` | 4 de 4, 26 de 26 y 5 de 5 en verde. "Max, perro, Chía" por formulario, voz y asistente produce el mismo reporte. **Resuelto con un límite:** la transcripción depende de Web Speech, así que la voz funciona en Chrome, Edge y Safari (Mac y iPhone, con el dictado activado) y necesita internet; en Firefox la misma frase se escribe en el campo de texto. Faltaría transcribir en el servidor, que se descartó para no procesar audio (sección 7) |
| Captura de voz: **asistente con tarjeta viva**. *Funcionalidad agregada porque el reto la exige*: la captura de voz necesita completar por turnos lo que una sola frase no dice | Disponibilidad y seguridad | Puerto `ExtractorDatosReporte` con dos adaptadores (Claude primero, regex de respaldo); el asistente no recibe `GestionReportes`, así que nunca publica | `application/service/ServicioAsistente.java`, `adaptadores/salida/ia/ExtractorClaude.java`, `application/service/ExtractorRegex.java`, `static/js/tarjeta-viva.js` | `ServicioAsistenteTest`, `ExtractorRegexTest`, `ExtractorClaudeTest` | 15 de 15, 26 de 26 y 45 de 45 en verde, sin llamar a Claude. Las coordenadas que Claude sugiere para un lugar nombrado se descartan si caen fuera de Colombia. `git diff step-14-h2-integrado step-15-extractor-claude --stat -- src/main/java/petfinder/domain` sale vacío |
| Los dos retos | Modificabilidad y testabilidad | Arquitectura hexagonal verificada con ArchUnit ([ADR-001](docs/adr/ADR-001-estilo-arquitectonico.md)) | `test/.../arquitectura/ReglasArquitecturaTest.java` | `ReglasArquitecturaTest` y JaCoCo | 5 de 5 reglas en verde. Cobertura total: 83,1 % de instrucciones y 83,3 % de ramas; paquete `domain`: 97,0 % y 87,3 % |

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
