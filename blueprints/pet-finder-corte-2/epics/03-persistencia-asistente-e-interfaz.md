# Épica 03: Persistencia, asistente e interfaz

> Los reportes viven en H2, el asistente llena la tarjeta viva con Claude o con regex, la interfaz premium con mapa funciona en computador y como PWA en celular, corre en Docker y tiene medido su rendimiento con k6.

| | |
|---|---|
| **Epic id** | `03-persistencia-asistente-e-interfaz` |
| **Responsable** | Mateo Ramírez — solo esta persona trabaja las tareas de esta épica |
| **Tasks** | `E3-T1` … `E3-T9` (9 tareas) |
| **Depends on** | `E2-T1` (02-adaptador-web-y-pruebas), `E1-T3` (01-nucleo-hexagonal), `E1-T4` (01-nucleo-hexagonal), `E1-T5` (01-nucleo-hexagonal), `E2-T4` (02-adaptador-web-y-pruebas) |
| **Unlocks** | `E2-T4`, `E2-T5`, `E2-T6`, `E1-T6` |
| **Parallel with** | Las otras dos épicas: cada tarea lista sus dependencias exactas; mientras estén en `done`, se trabaja en paralelo |

You do not need any other file to complete this epic. Everything below is repeated here on purpose.

---

## Stack

Java 17 · Spring Boot 4.1.1 (starters modulares) · arquitectura hexagonal · JPA/Hibernate + H2 en memoria · HTML/CSS/JS sin framework + Leaflet · Claude Sonnet 5 opcional con respaldo por reglas (regex) · Docker en Render.
Build con **Maven Wrapper**: `./mvnw` (macOS, Linux y Git Bash de Windows) o `.\mvnw.cmd` (PowerShell). No instales Maven. **JDK 17 obligatorio** (`./mvnw -v` debe decir `Java version: 17`). Las versiones de dependencias están en `pom.xml`: léelas, nunca las adivines ni las cambies.

| Task | Command |
|---|---|
| Compilar | `./mvnw -q -DskipTests compile` |
| App web | `./mvnw spring-boot:run` → http://localhost:8080 |
| Demo de consola | `./mvnw -q compile exec:java` · menú: `./mvnw -q compile exec:java -Dexec.args="--menu"` |
| Unitarias (`*Test`) | `./mvnw test` · una clase: `./mvnw -q test -Dtest=Clase` |
| Integración y sistema (`*IT`) + cobertura | `./mvnw verify` · una IT: `./mvnw -q verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=Clase` |
| Prueba de humo | `./mvnw -q -DskipTests package && sh scripts/humo.sh [/ruta ...]` |
| Verificar contenido | `sh scripts/contiene.sh ARCHIVO 'texto' ['!prohibido' ...]` (comillas simples) |
| Consola H2 | http://localhost:8080/h2-console · JDBC `jdbc:h2:mem:petfinder` · usuario `sa`, sin clave |

**Gate:** `./mvnw -q clean verify` passes before any task here is marked done.

Windows: todos los comandos corren igual en **Git Bash** (viene con Git para Windows). En PowerShell cambia `./mvnw` por `.\mvnw.cmd` y corre los scripts con el `sh` de Git Bash.
El proyecto no necesita ningún servicio externo para las pruebas: H2 es embebida y las pruebas nunca llaman a Claude (toda `@SpringBootTest` fija `ANTHROPIC_API_KEY=`).

Herramientas extra que solo usa esta épica: **k6** (E3-T1 y E3-T9), **Docker** en marcha (E3-T8). Sin ellas, esas tareas no pueden cerrar.

## Directory subtree

Only the parts this epic touches:

```
perf/README.md                                         # E3-T1 NEW (SLO), E3-T9 EDIT (resultados y análisis)
perf/scripts/baseline.js, perf/scripts/carga.js        # E3-T1 NEW
perf/resultados/                                       # E3-T1 .gitkeep, E3-T9 JSON y capturas
src/main/resources/static/
  css/tokens.css                                       # E3-T2 NEW — bloque de DESIGN.md §13 tal cual
  css/app.css                                          # E3-T2 NEW, E3-T7 EDIT — clases de vistas.html, solo var(--token)
  index.html                                           # E3-T2 NEW, E3-T7 EDIT, E3-T8 EDIT
  img/marca.svg                                        # E3-T2 NEW — copia exacta de docs/diseno/logo/marca.svg
  img/icono-180.png, icono-192.png, icono-512.png, icono-512-maskable.png   # ya existen (workspace)
  js/app.js, js/tarjeta-viva.js, js/mapa.js            # E3-T7 NEW
  js/api.js, js/voz.js                                 # existen (E2-T1), read-only
  manifest.webmanifest, sw.js                          # E3-T8 NEW
src/main/java/petfinder/
  adaptadores/salida/persistencia/h2/                  # E3-T3 NEW — ReporteEntity, AvistamientoEntity, ReporteJpaRepository, RepositorioReportesH2
  application/service/ServicioAsistente.java           # E3-T4 NEW
  application/service/ExtractorRegex.java              # E3-T4 NEW
  adaptadores/salida/ia/ExtractorClaude.java           # E3-T6 NEW — con la interfaz anidada ClienteModelo
  adaptadores/salida/ia/ClienteModeloAnthropic.java    # E3-T6 NEW — único archivo que importa com.anthropic
  config/ConfiguracionAsistente.java                   # E3-T4 NEW
  config/ConfiguracionIa.java                          # E3-T6 NEW
  config/DatosDeEjemplo.java                           # E3-T5 NEW
  config/ConfiguracionPetFinder.java                   # E3-T5 EDIT — solo el bean RepositorioReportes
src/main/resources/application.properties              # E3-T5 EDIT — petfinder.datos-ejemplo
pom.xml                                                # E3-T6 EDIT — anthropic-java
Dockerfile, .dockerignore                              # E3-T8 NEW
src/test/java/petfinder/
  integracion/persistencia/RepositorioReportesH2IT.java    # E3-T3 NEW
  integracion/persistencia/ServicioReportesH2IT.java       # E3-T5 NEW
  integracion/persistencia/DatosDeEjemploIT.java           # E3-T5 NEW
  application/asistente/ServicioAsistenteTest.java         # E3-T4 NEW
  application/asistente/ExtractorRegexTest.java            # E3-T4 NEW
  adaptadores/ia/ExtractorClaudeTest.java                  # E3-T6 NEW
```

Everything outside this subtree is out of scope. `scripts/*.sh`, `mvnw`, los íconos PNG y la configuración de agentes ya están en la raíz: los copió el Bootstrap desde el `workspace/` del bundle. If a task seems to require editing a file not listed here, stop and report.

## Data model touched here

| Entity | Fields this epic adds or reads | Notes |
|---|---|---|
| `REPORTES` | `id` PK, `tipo`, `estado` (índice `IDX_REPORTES_ESTADO`), `fecha_creacion`, `zona`, `referencia`, `latitud`, `longitud`, `descripcion` (2000), `contacto_nombre`, `contacto_medio`, `mascota_*` (5), `descripcion_mascota` (2000) | Una tabla para los dos tipos; lo que no aplica queda null |
| `AVISTAMIENTOS` | `id` PK, `reporte_id` FK → `REPORTES`, `fecha_hora`, `zona`, `referencia`, `latitud`, `longitud`, `descripcion` (2000), `contacto_nombre`, `contacto_medio` | `cascade = ALL`, `orphanRemoval = true`; contacto opcional |

Hibernate crea el esquema al arrancar (`ddl-auto` por defecto con H2 embebida: `create-drop`); no hay migraciones porque la base es en memoria y se reinicia con la app. El DDL exacto está en `blueprint.md` §4.

## Contracts

**Consumed** — already exists, do not rebuild:

| From | Interface | Guarantee |
|---|---|---|
| `01-nucleo-hexagonal` (E1-T3) | `ReportePerdida.reconstruir`, `ReporteEncontrada.reconstruir` | Conservan fecha, estado y avistamientos |
| `01-nucleo-hexagonal` (E1-T4) | `Ubicacion` con coordenadas, `BorradorReporte`, `AsistenteReportes`, `ExtractorDatosReporte` | Firmas abajo |
| `01-nucleo-hexagonal` (E1-T5) | Bean `InterpreteComandoVoz` con `interpretar(String)` | Java puro, sin red |
| `02-adaptador-web-y-pruebas` (E2-T1) | `api.js`, `voz.js` | Funciones de la tabla de la API; `ErrorApi` con el mensaje del servidor |
| `02-adaptador-web-y-pruebas` (E2-T4) | La API REST de abajo | Códigos y JSON exactos |

```java
// application/port/entrada — lo único que conocen los adaptadores de entrada
public interface GestionReportes {
    ReporteMascota registrar(TipoReporte tipo, SolicitudReporte solicitud);
    List<ReporteMascota> listarActivos();
    ReporteMascota consultar(String id);          // ReporteNoEncontradoException si no existe
    void resolver(String id);                     // OperacionNoPermitidaException si no está ACTIVO
    void cerrar(String id);
}
public interface RegistroAvistamientos {
    void registrar(String idReporte, Avistamiento avistamiento);
}
public interface ProcesadorComandosVoz {
    ResultadoComandoVoz procesar(String texto, Contacto contacto);
    record ResultadoComandoVoz(AccionVoz accion, String mensaje, List<ReporteMascota> reportes) {}
    enum AccionVoz { REGISTRAR_PERDIDA, REGISTRAR_ENCONTRADA, LISTAR_ACTIVOS, CONSULTAR, NO_RECONOCIDO }
}
public interface AsistenteReportes {
    ResultadoTurno procesarTurno(BorradorReporte actual, String texto);
    record ResultadoTurno(BorradorReporte borrador, List<BorradorReporte.Campo> faltantes,
                          String pregunta, String fuente) { boolean listoParaPublicar(); }
}
// application/port/salida — lo que el núcleo necesita de afuera
public interface RepositorioReportes {
    void guardar(ReporteMascota reporte);
    Optional<ReporteMascota> buscarPorId(String id);
    List<ReporteMascota> listarActivos();
}
public interface ExtractorDatosReporte {
    String nombre();                                          // "claude" o "regex"
    Optional<BorradorReporte> extraer(String texto, BorradorReporte contexto);
}
// domain/model — firmas nuevas del Corte 2
public record Ubicacion(String zonaOBarrio, String referencia, Double latitud, Double longitud) {
    public Ubicacion(String zonaOBarrio, String referencia);  // constructor del Corte 1, se conserva
    public boolean tieneCoordenadas();
    public Ubicacion aproximada();                            // coordenadas a 3 decimales (≈100 m)
}
public record BorradorReporte(TipoReporte tipo, String nombre, String especie, String raza, String color,
        String senas, String descripcionMascota, String zona, String referencia, Double latitud,
        Double longitud, String descripcion, String contactoNombre, String contactoMedio) {
    public enum Campo { TIPO, NOMBRE, ESPECIE, DESCRIPCION_MASCOTA, ZONA, DESCRIPCION, CONTACTO; String pregunta(); }
    public static BorradorReporte vacio();
    public List<Campo> camposFaltantes();
    public boolean estaCompleto();
    public BorradorReporte fusionar(BorradorReporte nuevo);
    public SolicitudReporte aSolicitud();
}
public static ReportePerdida reconstruir(String id, Ubicacion ubicacion, String descripcion, Mascota mascota,
        Contacto contactoPropietario, LocalDateTime fechaCreacion, EstadoReporte estado, List<Avistamiento> avistamientos);
public static ReporteEncontrada reconstruir(String id, Ubicacion ubicacion, String descripcion,
        String descripcionMascota, Contacto contactoReportante, LocalDateTime fechaCreacion, EstadoReporte estado);
```

| Método y ruta | Puerto | Éxito | Errores |
|---|---|---|---|
| `POST /api/reportes` | `GestionReportes.registrar` | 201 + `ReporteDTO` (detalle) | 400 |
| `GET /api/reportes` | `GestionReportes.listarActivos` | 200 + `ReporteDTO[]` (resumen, más reciente primero) | — |
| `GET /api/reportes/{id}` | `GestionReportes.consultar` | 200 + `ReporteDTO` (detalle) | 404 |
| `POST /api/reportes/{id}/resolver` | `GestionReportes.resolver` | 204 | 404, 409 |
| `POST /api/reportes/{id}/cerrar` | `GestionReportes.cerrar` | 204 | 404, 409 |
| `POST /api/reportes/{id}/avistamientos` | `RegistroAvistamientos.registrar` | 201 + `ReporteDTO` (detalle, actualizado) | 400, 404, 409 |
| `POST /api/voz` | `ProcesadorComandosVoz.procesar` | 201 si registró · 200 si listó o consultó | 404 (consulta a un id inexistente), 422 si no entendió |
| `POST /api/asistente/turno` | `AsistenteReportes.procesarTurno` | 200 + `RespuestaTurnoDTO` | 400 |

Errores: siempre `{"error": "mensaje para la persona"}`. `DatosInvalidosException` → 400, `ReporteNoEncontradoException` → 404, `OperacionNoPermitidaException` → 409, JSON ilegible → 400 `El cuerpo de la petición no es válido`, frase de voz no reconocida → 422 `No entendí. Prueba con: perdí un perro llamado Max en Chía`.

```json
// SolicitudReporteDTO — entrada de POST /api/reportes (ENCONTRADA: mascota null y descripcionMascota con texto)
{ "tipo": "PERDIDA", "zona": "Cedritos", "referencia": "Parque de la 147", "latitud": 4.7235, "longitud": -74.0417,
  "descripcion": "Se escapó en la tarde", "nombreContacto": "Camila", "medioContacto": "3001234567",
  "mascota": { "nombre": "Luna", "especie": "perro", "raza": "criolla", "color": "blanca", "senas": "mancha negra" },
  "descripcionMascota": null }

// ReporteDTO — salida. resumen(): contacto enmascarado y avistamientos []. detalle(): contacto completo y avistamientos.
{ "id": "PF-001", "tipo": "PERDIDA", "estado": "ACTIVO", "fechaCreacion": "2026-09-24T16:30:00",
  "zona": "Cedritos", "referencia": "Parque de la 147", "latitud": 4.724, "longitud": -74.042,
  "descripcion": "Se escapó en la tarde", "nombreMascota": "Luna", "especie": "perro", "raza": "criolla",
  "color": "blanca", "senas": "mancha negra", "descripcionMascota": null,
  "nombreContacto": "Camila", "contacto": "300•••••67", "cantidadAvistamientos": 1,
  "avistamientos": [ { "id": "AV-1a2b3c4d", "fechaHora": "2026-09-24T17:10:00", "zona": "Cedritos",
                       "referencia": "Canchas", "latitud": null, "longitud": null, "descripcion": "La vi corriendo" } ] }

// AvistamientoDTO — entrada de POST /api/reportes/{id}/avistamientos (contacto opcional)
{ "zona": "Cedritos", "referencia": "Canchas", "latitud": 4.7301, "longitud": -74.0455,
  "descripcion": "La vi corriendo", "nombreContacto": null, "medioContacto": null }

// ComandoVozDTO → RespuestaVozDTO — POST /api/voz
{ "texto": "perdí un perro llamado Max en Chía", "nombreContacto": "Ana", "medioContacto": "3001112233" }
{ "accion": "REGISTRAR_PERDIDA", "mensaje": "Reporte PF-005 creado", "reportes": [ /* ReporteDTO resumen */ ] }

// TurnoDTO → RespuestaTurnoDTO — POST /api/asistente/turno (borrador null en el primer turno)
{ "borrador": null, "texto": "se me perdió mi perrita Luna en Cedritos" }
{ "borrador": { "tipo": "PERDIDA", "nombre": "Luna", "especie": "perro", "raza": null, "color": null, "senas": null,
                "descripcionMascota": null, "zona": "Cedritos", "referencia": null, "latitud": null, "longitud": null,
                "descripcion": "se me perdió mi perrita Luna en Cedritos", "contactoNombre": null, "contactoMedio": null },
  "faltantes": ["CONTACTO"], "pregunta": "¿A qué número o correo te pueden escribir?", "fuente": "regex", "listo": false }
```

**Produced** — later epics depend on exactly these signatures. Changing one breaks them:

| Export | Signature | Used by |
|---|---|---|
| `RepositorioReportesH2` | `implements RepositorioReportes`; sin `@Repository`; constructor `(ReporteJpaRepository)` | `E3-T5`, pruebas de sistema de `E2-T5` |
| Bean `AsistenteReportes` | `ServicioAsistente(List<ExtractorDatosReporte>)`; extractores por `@Order` (Claude 10, regex 100) | `E2-T4` (`AsistenteController`) |
| `ExtractorClaude` | `nombre()` = `claude`; vacío si no hay clave, si se llegó al tope o si el JSON es inválido | `E3-T4` por la lista ordenada |
| `index.html` | los ganchos `data-prueba` de la tabla | `E2-T6` |
| Imagen Docker | `Dockerfile` en la raíz; escucha en `PORT` (8080 por defecto); salud en `/actuator/health` | despliegue en Render |

Ganchos `data-prueba` que `index.html` expone (contrato entre E3-T7 y las pruebas de UI de E2-T6; no se renombran):

| `data-prueba` | Elemento |
|---|---|
| `lista-casos` | Contenedor de las tarjetas de casos activos (cada tarjeta generada lleva `data-prueba="tarjeta-caso"` y `data-id="PF-…"`) |
| `mapa` | Contenedor del mapa Leaflet |
| `boton-nuevo-reporte` | Abre la vista Nuevo reporte |
| `entrada-texto` | Campo de texto del asistente (siempre visible, respaldo de la voz) |
| `boton-enviar-texto` | Envía la frase escrita al asistente |
| `boton-microfono` | Inicia o detiene la captura de voz (oculto si `soportaVoz()` es falso) |
| `tarjeta-viva` | La tarjeta con los campos del borrador (cada fila generada lleva `data-prueba="campo-<nombre>"`, p. ej. `campo-contactoMedio`, editable) |
| `boton-publicar` | Publica el borrador con `POST /api/reportes` |
| `mensaje` | Zona de avisos (éxito y errores de `ErrorApi`) |
| `boton-la-vi` | En el detalle de un caso, abre Reportar avistamiento |
| `boton-enviar-pista` | Envía el avistamiento |

## Conventions that bite in this area

- **Todo en español**: clases, métodos, variables, Javadoc y textos visibles. Solo los sufijos que imponen las herramientas van en inglés (`Controller`, `DTO`, `Entity`, `Test`, `IT`). No renombres código existente.
- **Javadoc que explica el porqué**, como el del Corte 1: una decisión y su razón, no una paráfrasis del método.
- **Objetos de valor como `record`**; entidades con estado protegido (sin setters; las transiciones solo en `EstadoReporte`).
- **Errores de negocio con subclases de `DominioException`** (`DatosInvalidosException`, `ReporteNoEncontradoException`, `OperacionNoPermitidaException`) y mensajes en español para la persona.
- **Pruebas**: `// Arrange`, `// Act`, `// Assert`, `@DisplayName` en español, nada compartido entre pruebas, nunca un ID fijo en pruebas de integración o sistema.
- **Dirección de dependencias** (ArchUnit la verifica): `domain` no importa nada de Spring, JPA, Anthropic ni de otros paquetes del proyecto; `application` no importa Spring ni adaptadores; la web solo conoce puertos; solo `adaptadores.salida.ia` importa `com.anthropic`; solo `adaptadores.salida.persistencia` importa `jakarta.persistence`.
- **Commits y push los hace la persona responsable**, nunca el agente: el agente prepara el cambio, corre el `Verify` y entrega el bloque `Checkpoint`. Un commit por tarea con el prefijo del id (`E1-T3: …`) y su tag.
- **La interfaz sigue el skill `diseno-pet-finder`** (en `.claude/skills/`, ya está en el repo): solo `var(--token)`, un botón primario por pantalla, textos en español con la voz de `docs/diseno/DESIGN.md` §2.3, nada de `alert()`, y el campo de texto siempre visible como respaldo de la voz.
- **Nunca pongas `@Repository` a `RepositorioReportesH2`**: habría dos beans del puerto y la app no arrancaría.
- **La clave de Anthropic solo vive en `.env` o en las variables del servidor.** Nunca en el código, en el JavaScript, en los logs ni en el repo.
- **La carga se mide con el asistente en regex** (sin clave): mide nuestro código y no gasta créditos.

Full project rules: `CLAUDE.md`. Area rules: `.claude/rules/persistencia.md`, `.claude/rules/ia.md`, `.claude/rules/interfaz.md`, `.claude/rules/pruebas.md`. Both sit in the project root — the builder copied them there from the bundle's `workspace/` before task one.

---

## Tasks

Listed in the same order as `tasks.json`. That order is the build order — work top to bottom and do not re-rank by priority or by what looks quick. Una tarea está lista cuando todas sus dependencias (de cualquier épica) están en `done`: antes de empezar, haz `git pull` de `main` para tener el trabajo de los demás.

### `E3-T1` — Fijar el SLO y escribir los guiones de k6

**Depends on:** nothing · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-03-slo-y-k6`

**Esta tarea se commitea hoy, antes de correr cualquier carga**: la fecha del commit es la evidencia de que el SLO se definió antes de ejecutar (lo exige la rúbrica). `perf/README.md` lleva: reto ligado (captura de voz, atributo rendimiento), SLO, escenario "hora pico de reportes por voz" (cada usuario virtual registra por voz y luego pide ver reportes), tipos de prueba (baseline y carga obligatorios, estrés opcional), entorno (PC, VM o el mismo Mac) y una tabla de resultados con `__` en las celdas que E3-T9 llena. Los guiones leen `const BASE = __ENV.BASE_URL || 'http://localhost:8080';`, hacen `POST /api/voz` con `perdí un perro llamado Max${__VU}x${__ITER} en Chía` (tag `voz_registrar`, espera 201) y luego `POST /api/voz` con `ver reportes` (tag `voz_listar`, espera 200), con `sleep(1)` entre iteraciones. `baseline.js`: 5 VUs durante 1 minuto. `carga.js`: `stages` 1 min hasta 50 VUs, 3 min sostenidos, 30 s de bajada, y los `thresholds` del SLO. Las pruebas de carga corren con el asistente en regex (sin `ANTHROPIC_API_KEY`), para medir nuestro código y no gastar créditos.

**Files**
- `perf/README.md`
- `perf/scripts/baseline.js`
- `perf/scripts/carga.js`
- `perf/resultados/.gitkeep`

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** `perf/README.md` is read **THE SYSTEM SHALL** state the SLO before any run: p95 of `http_req_duration` ≤ 500 ms (`p(95)<500`), `http_req_failed` < 1 % (`rate<0.01`) and at least 30 req/s sustained.
2. **WHEN** `k6 inspect` parses `perf/scripts/baseline.js` and `perf/scripts/carga.js` **THE SYSTEM SHALL** exit 0 for both.
3. **WHEN** `perf/scripts/carga.js` is read **THE SYSTEM SHALL** encode the SLO as thresholds `http_req_duration: ['p(95)<500']` and `http_req_failed: ['rate<0.01']`.
4. **WHEN** a script sends requests **THE SYSTEM SHALL** tag them `voz_registrar` and `voz_listar` so both latencies can be compared.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
sh scripts/contiene.sh perf/README.md 'p(95)<500' 'rate<0.01' '30 req/s' 'Escenario' 'Captura de voz'
sh scripts/contiene.sh perf/scripts/carga.js "http_req_duration: ['p(95)<500']" "http_req_failed: ['rate<0.01']" 'voz_registrar' 'voz_listar'
sh scripts/contiene.sh perf/scripts/baseline.js 'voz_registrar' 'voz_listar'
k6 inspect perf/scripts/baseline.js
k6 inspect perf/scripts/carga.js
```

**Checkpoint**

```bash
git add -A && git commit -m "E3-T1: Fijar el SLO y escribir los guiones de k6"
git tag step-03-slo-y-k6
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-03-slo-y-k6`. Never invent the tag: copy the `checkpoint` field.

---

### `E3-T2` — Montar tokens, estilos base y el esqueleto de la página

**Depends on:** nothing · **Priority:** p1 — metadata for scope cuts, not a running order · **Checkpoint:** `step-05-sistema-visual`

Sigue el skill `diseno-pet-finder` (se activa solo al tocar la interfaz) y copia, no reinterpretes: `tokens.css` es el bloque CSS de `docs/diseno/DESIGN.md` §13 tal cual (claro, oscuro con `prefers-color-scheme` y además `:root[data-tema="oscuro"]`). `app.css` porta las clases de `docs/diseno/vistas.html` (`.btn-primario`, `.estado`, `.tarjeta-mascota`, `.tarjeta-viva`, `.campo`, `.pregunta`, `.hoja`, `.pestanas`, `.vidrio`, `.pin`, `.segmentado`) usando **solo** `var(--token)`; sin selectores de id (el verificador de hexadecimales los confundiría) y sin colores sueltos. `index.html` es el esqueleto: fuentes Geist, Geist Mono e Instrument Serif desde Google Fonts, cabecera con la marca y contenedores vacíos para las vistas que llena E3-T7. `img/marca.svg` es una copia exacta de `docs/diseno/logo/marca.svg` (el logo nunca se redibuja). No necesita el servidor.

**Files**
- `src/main/resources/static/css/tokens.css`
- `src/main/resources/static/css/app.css`
- `src/main/resources/static/index.html`
- `src/main/resources/static/img/marca.svg`

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** `sh scripts/verificar-tokens.sh` runs **THE SYSTEM SHALL** find every token defined in `docs/diseno/DESIGN.md` §13 inside `static/css/tokens.css` and zero hexadecimal colors in `static/css/app.css`.
2. **WHEN** `static/index.html` is read **THE SYSTEM SHALL** link `css/tokens.css`, `css/app.css` and `img/marca.svg` and declare `lang="es"` and a viewport meta tag.
3. **WHEN** `static/img/marca.svg` is compared with `docs/diseno/logo/marca.svg` **THE SYSTEM SHALL** be byte-identical.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
sh scripts/verificar-tokens.sh
sh scripts/contiene.sh src/main/resources/static/index.html 'css/tokens.css' 'css/app.css' 'img/marca.svg' 'lang="es"' 'name="viewport"'
cmp -s docs/diseno/logo/marca.svg src/main/resources/static/img/marca.svg
```

**Checkpoint**

```bash
git add -A && git commit -m "E3-T2: Montar tokens, estilos base y el esqueleto de la página"
git tag step-05-sistema-visual
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-05-sistema-visual`. Never invent the tag: copy the `checkpoint` field.

---

### `E3-T3` — Implementar el adaptador de persistencia H2

**Depends on:** `E1-T3`, `E1-T4` · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-10-adaptador-h2`

Adaptador de salida del puerto `RepositorioReportes` sobre JPA + H2. Las entidades son **clases distintas** a las del dominio a propósito (con `@Entity` en el dominio, ArchUnit falla). Una sola tabla `REPORTES` para los dos tipos con columnas nulas, y `AVISTAMIENTOS` con `@ManyToOne`. `RepositorioReportesH2` **no** lleva `@Repository`: lo construye `ConfiguracionPetFinder` en E3-T5, porque si Spring lo encontrara solo habría dos beans del mismo puerto y la app no arrancaría. Al guardar reutiliza la fila existente y limpia su lista (`clear()` + `add`), nunca la reemplaza: eso evita `A collection with cascade=all-delete-orphan was no longer referenced`. La prueba usa `@DataJpaTest` (`org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest` en Boot 4), que revierte cada prueba: son aisladas y no dependen del orden. Nunca afirmes un ID generado fijo.

**Files**
- `src/main/java/petfinder/adaptadores/salida/persistencia/h2/ReporteEntity.java`
- `src/main/java/petfinder/adaptadores/salida/persistencia/h2/AvistamientoEntity.java`
- `src/main/java/petfinder/adaptadores/salida/persistencia/h2/ReporteJpaRepository.java`
- `src/main/java/petfinder/adaptadores/salida/persistencia/h2/RepositorioReportesH2.java`
- `src/test/java/petfinder/integracion/persistencia/RepositorioReportesH2IT.java`

**Contenido literal** — escríbelo tal cual; este código ya se compiló y probó.

**`src/main/java/petfinder/adaptadores/salida/persistencia/h2/ReporteEntity.java`** — nuevo

```java
package petfinder.adaptadores.salida.persistencia.h2;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.TipoReporte;

/**
 * Fila de la tabla REPORTES. Es una clase distinta a ReporteMascota a
 * propósito: si el dominio llevara @Entity dependería de JPA y ArchUnit lo
 * rechazaría. Una sola tabla para los dos tipos; las columnas que no aplican
 * a un tipo quedan en null.
 */
@Entity
@Table(name = "REPORTES", indexes = @Index(name = "IDX_REPORTES_ESTADO", columnList = "estado"))
public class ReporteEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoReporte tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReporte estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private String zona;
    private String referencia;
    private Double latitud;
    private Double longitud;

    @Column(length = 2000)
    private String descripcion;

    private String contactoNombre;
    private String contactoMedio;

    private String mascotaNombre;
    private String mascotaEspecie;
    private String mascotaRaza;
    private String mascotaColor;
    private String mascotaSenas;

    @Column(length = 2000)
    private String descripcionMascota;

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaHora ASC")
    private List<AvistamientoEntity> avistamientos = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public TipoReporte getTipo() { return tipo; }
    public void setTipo(TipoReporte tipo) { this.tipo = tipo; }
    public EstadoReporte getEstado() { return estado; }
    public void setEstado(EstadoReporte estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getContactoNombre() { return contactoNombre; }
    public void setContactoNombre(String contactoNombre) { this.contactoNombre = contactoNombre; }
    public String getContactoMedio() { return contactoMedio; }
    public void setContactoMedio(String contactoMedio) { this.contactoMedio = contactoMedio; }
    public String getMascotaNombre() { return mascotaNombre; }
    public void setMascotaNombre(String mascotaNombre) { this.mascotaNombre = mascotaNombre; }
    public String getMascotaEspecie() { return mascotaEspecie; }
    public void setMascotaEspecie(String mascotaEspecie) { this.mascotaEspecie = mascotaEspecie; }
    public String getMascotaRaza() { return mascotaRaza; }
    public void setMascotaRaza(String mascotaRaza) { this.mascotaRaza = mascotaRaza; }
    public String getMascotaColor() { return mascotaColor; }
    public void setMascotaColor(String mascotaColor) { this.mascotaColor = mascotaColor; }
    public String getMascotaSenas() { return mascotaSenas; }
    public void setMascotaSenas(String mascotaSenas) { this.mascotaSenas = mascotaSenas; }
    public String getDescripcionMascota() { return descripcionMascota; }
    public void setDescripcionMascota(String descripcionMascota) { this.descripcionMascota = descripcionMascota; }
    public List<AvistamientoEntity> getAvistamientos() { return avistamientos; }
}
```

**`src/main/java/petfinder/adaptadores/salida/persistencia/h2/AvistamientoEntity.java`** — nuevo

```java
package petfinder.adaptadores.salida.persistencia.h2;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Fila de la tabla AVISTAMIENTOS. El contacto es opcional, como en el dominio. */
@Entity
@Table(name = "AVISTAMIENTOS")
public class AvistamientoEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporte_id")
    private ReporteEntity reporte;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    private String zona;
    private String referencia;
    private Double latitud;
    private Double longitud;

    @Column(length = 2000)
    private String descripcion;

    private String contactoNombre;
    private String contactoMedio;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public ReporteEntity getReporte() { return reporte; }
    public void setReporte(ReporteEntity reporte) { this.reporte = reporte; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getContactoNombre() { return contactoNombre; }
    public void setContactoNombre(String contactoNombre) { this.contactoNombre = contactoNombre; }
    public String getContactoMedio() { return contactoMedio; }
    public void setContactoMedio(String contactoMedio) { this.contactoMedio = contactoMedio; }
}
```

**`src/main/java/petfinder/adaptadores/salida/persistencia/h2/ReporteJpaRepository.java`** — nuevo

```java
package petfinder.adaptadores.salida.persistencia.h2;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import petfinder.domain.model.EstadoReporte;

/** Consultas de Spring Data. Solo la usa RepositorioReportesH2. */
public interface ReporteJpaRepository extends JpaRepository<ReporteEntity, String> {

    List<ReporteEntity> findByEstadoOrderByFechaCreacionDesc(EstadoReporte estado);
}
```

**`src/main/java/petfinder/adaptadores/salida/persistencia/h2/RepositorioReportesH2.java`** — nuevo

```java
package petfinder.adaptadores.salida.persistencia.h2;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import petfinder.application.port.salida.RepositorioReportes;
import petfinder.domain.model.Avistamiento;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteEncontrada;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

/**
 * Adaptador de salida: implementa el puerto RepositorioReportes sobre H2.
 *
 * No lleva @Repository a propósito: lo construye ConfiguracionPetFinder, que
 * es quien decide qué implementación del puerto usa la aplicación. Si Spring
 * lo encontrara solo, habría dos beans del mismo puerto.
 *
 * El mapeo dominio ↔ entidad vive aquí, en un solo lugar; es el único sitio
 * del proyecto donde se pregunta por el tipo concreto de un reporte para
 * guardarlo.
 */
public class RepositorioReportesH2 implements RepositorioReportes {

    private final ReporteJpaRepository jpa;

    public RepositorioReportesH2(ReporteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public void guardar(ReporteMascota reporte) {
        // Se reutiliza la fila existente y se limpia su lista: reemplazar la
        // colección provoca "A collection with cascade=all-delete-orphan was
        // no longer referenced".
        ReporteEntity fila = jpa.findById(reporte.getId()).orElseGet(ReporteEntity::new);
        copiar(reporte, fila);
        jpa.save(fila);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReporteMascota> buscarPorId(String id) {
        return jpa.findById(id).map(RepositorioReportesH2::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteMascota> listarActivos() {
        return jpa.findByEstadoOrderByFechaCreacionDesc(EstadoReporte.ACTIVO).stream()
                .map(RepositorioReportesH2::aDominio)
                .toList();
    }

    private static void copiar(ReporteMascota reporte, ReporteEntity fila) {
        fila.setId(reporte.getId());
        fila.setEstado(reporte.getEstado());
        fila.setFechaCreacion(reporte.getFechaCreacion());
        fila.setDescripcion(reporte.getDescripcion());
        Ubicacion u = reporte.getUbicacion();
        fila.setZona(u.zonaOBarrio());
        fila.setReferencia(u.referencia());
        fila.setLatitud(u.latitud());
        fila.setLongitud(u.longitud());
        fila.getAvistamientos().clear();
        if (reporte instanceof ReportePerdida perdida) {
            fila.setTipo(TipoReporte.PERDIDA);
            Mascota m = perdida.getMascota();
            fila.setMascotaNombre(m.nombre());
            fila.setMascotaEspecie(m.especie());
            fila.setMascotaRaza(m.raza());
            fila.setMascotaColor(m.color());
            fila.setMascotaSenas(m.senasParticulares());
            fila.setContactoNombre(perdida.getContactoPropietario().nombre());
            fila.setContactoMedio(perdida.getContactoPropietario().medioContacto());
            for (Avistamiento a : perdida.getAvistamientos()) {
                fila.getAvistamientos().add(aFila(a, fila));
            }
        } else if (reporte instanceof ReporteEncontrada encontrada) {
            fila.setTipo(TipoReporte.ENCONTRADA);
            fila.setDescripcionMascota(encontrada.getDescripcionMascota());
            fila.setContactoNombre(encontrada.getContactoReportante().nombre());
            fila.setContactoMedio(encontrada.getContactoReportante().medioContacto());
        }
    }

    private static AvistamientoEntity aFila(Avistamiento a, ReporteEntity reporte) {
        AvistamientoEntity fila = new AvistamientoEntity();
        fila.setId(a.id());
        fila.setReporte(reporte);
        fila.setFechaHora(a.fechaHora());
        fila.setZona(a.ubicacion().zonaOBarrio());
        fila.setReferencia(a.ubicacion().referencia());
        fila.setLatitud(a.ubicacion().latitud());
        fila.setLongitud(a.ubicacion().longitud());
        fila.setDescripcion(a.descripcion());
        if (a.contactoReportante() != null) {
            fila.setContactoNombre(a.contactoReportante().nombre());
            fila.setContactoMedio(a.contactoReportante().medioContacto());
        }
        return fila;
    }

    private static ReporteMascota aDominio(ReporteEntity fila) {
        Ubicacion ubicacion = new Ubicacion(fila.getZona(), fila.getReferencia(), fila.getLatitud(), fila.getLongitud());
        Contacto contacto = new Contacto(fila.getContactoNombre(), fila.getContactoMedio());
        if (fila.getTipo() == TipoReporte.PERDIDA) {
            List<Avistamiento> avistamientos = fila.getAvistamientos().stream()
                    .map(RepositorioReportesH2::aAvistamiento)
                    .toList();
            return ReportePerdida.reconstruir(fila.getId(), ubicacion, fila.getDescripcion(),
                    new Mascota(fila.getMascotaNombre(), fila.getMascotaEspecie(), fila.getMascotaRaza(),
                            fila.getMascotaColor(), fila.getMascotaSenas()),
                    contacto, fila.getFechaCreacion(), fila.getEstado(), avistamientos);
        }
        return ReporteEncontrada.reconstruir(fila.getId(), ubicacion, fila.getDescripcion(),
                fila.getDescripcionMascota(), contacto, fila.getFechaCreacion(), fila.getEstado());
    }

    private static Avistamiento aAvistamiento(AvistamientoEntity fila) {
        Contacto contacto = fila.getContactoMedio() == null && fila.getContactoNombre() == null
                ? null
                : new Contacto(fila.getContactoNombre(), fila.getContactoMedio());
        return new Avistamiento(fila.getId(), fila.getFechaHora(),
                new Ubicacion(fila.getZona(), fila.getReferencia(), fila.getLatitud(), fila.getLongitud()),
                fila.getDescripcion(), contacto);
    }
}
```

**`src/test/java/petfinder/integracion/persistencia/RepositorioReportesH2IT.java`** — nuevo

```java
package petfinder.integracion.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;

import petfinder.adaptadores.salida.persistencia.h2.ReporteJpaRepository;
import petfinder.adaptadores.salida.persistencia.h2.RepositorioReportesH2;
import petfinder.domain.model.Avistamiento;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteEncontrada;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.Ubicacion;

/**
 * Frontera adaptador de salida + base de datos real (H2 embebida).
 * @DataJpaTest revierte cada prueba al terminar: son aisladas y no dependen
 * del orden. Nunca se afirma un ID fijo: cada prueba crea el suyo.
 */
@DataJpaTest
@Import(RepositorioReportesH2IT.Configuracion.class)
class RepositorioReportesH2IT {

    @TestConfiguration
    static class Configuracion {
        @Bean
        RepositorioReportesH2 repositorioReportesH2(ReporteJpaRepository jpa) {
            return new RepositorioReportesH2(jpa);
        }
    }

    private static final LocalDateTime HACE_UNA_HORA = LocalDateTime.of(2026, 9, 24, 15, 0);

    @Autowired
    private RepositorioReportesH2 repositorio;

    private static ReportePerdida perdida(String id, EstadoReporte estado, List<Avistamiento> avistamientos) {
        return ReportePerdida.reconstruir(id, new Ubicacion("Cedritos", "Parque 147", 4.7235, -74.0417),
                "Se escapó en la tarde", new Mascota("Luna", "perro", "criolla", "blanca", "mancha negra"),
                new Contacto("Camila", "3001234567"), HACE_UNA_HORA, estado, avistamientos);
    }

    @Test
    @DisplayName("Guardar una pérdida y buscarla conserva mascota, contacto y coordenadas")
    void idaYVueltaDePerdida() {
        // Arrange
        repositorio.guardar(perdida("PF-T01", EstadoReporte.ACTIVO, List.of()));
        // Act
        ReporteMascota leido = repositorio.buscarPorId("PF-T01").orElseThrow();
        // Assert
        ReportePerdida p = assertInstanceOf(ReportePerdida.class, leido);
        assertEquals("Luna", p.getMascota().nombre());
        assertEquals("3001234567", p.getContactoPropietario().medioContacto());
        assertEquals(4.7235, p.getUbicacion().latitud());
    }

    @Test
    @DisplayName("Guardar un hallazgo conserva la descripción del animal")
    void idaYVueltaDeEncontrada() {
        // Arrange
        repositorio.guardar(ReporteEncontrada.reconstruir("PF-T02", new Ubicacion("Chía", "Parque"),
                "Estaba sola", "Gata gris", new Contacto("Lorenzi", "3007654321"), HACE_UNA_HORA, EstadoReporte.ACTIVO));
        // Act
        ReporteEncontrada e = assertInstanceOf(ReporteEncontrada.class, repositorio.buscarPorId("PF-T02").orElseThrow());
        // Assert
        assertEquals("Gata gris", e.getDescripcionMascota());
    }

    @Test
    @DisplayName("Buscar un ID inexistente devuelve Optional vacío")
    void idInexistente() {
        assertTrue(repositorio.buscarPorId("PF-NO-EXISTE").isEmpty());
    }

    @Test
    @DisplayName("Listar activos excluye resueltos y cerrados")
    void listarActivosFiltraPorEstado() {
        // Arrange
        repositorio.guardar(perdida("PF-T03", EstadoReporte.ACTIVO, List.of()));
        repositorio.guardar(perdida("PF-T04", EstadoReporte.RESUELTO, List.of()));
        repositorio.guardar(perdida("PF-T05", EstadoReporte.CERRADO, List.of()));
        // Act
        List<String> ids = repositorio.listarActivos().stream().map(ReporteMascota::getId).toList();
        // Assert
        assertTrue(ids.contains("PF-T03"));
        assertFalse(ids.contains("PF-T04"));
        assertFalse(ids.contains("PF-T05"));
    }

    @Test
    @DisplayName("Guardar de nuevo un reporte resuelto actualiza su estado")
    void actualizarEstado() {
        // Arrange
        ReportePerdida reporte = perdida("PF-T06", EstadoReporte.ACTIVO, List.of());
        repositorio.guardar(reporte);
        // Act
        reporte.resolver();
        repositorio.guardar(reporte);
        // Assert
        assertEquals(EstadoReporte.RESUELTO, repositorio.buscarPorId("PF-T06").orElseThrow().getEstado());
    }

    @Test
    @DisplayName("Los avistamientos se guardan y se recuperan con el reporte")
    void avistamientosIdaYVuelta() {
        // Arrange
        ReportePerdida reporte = perdida("PF-T07", EstadoReporte.ACTIVO, List.of());
        repositorio.guardar(reporte);
        reporte.agregarAvistamiento(new Avistamiento("AV-T1", HACE_UNA_HORA.plusMinutes(30),
                new Ubicacion("Cedritos", "Canchas"), "La vi corriendo", null));
        // Act
        repositorio.guardar(reporte);
        // Assert
        ReportePerdida leido = (ReportePerdida) repositorio.buscarPorId("PF-T07").orElseThrow();
        assertEquals(1, leido.getAvistamientos().size());
        assertEquals("La vi corriendo", leido.getAvistamientos().get(0).descripcion());
    }

    @Test
    @DisplayName("La fecha de creación se conserva al reconstruir")
    void fechaSeConserva() {
        // Arrange
        repositorio.guardar(perdida("PF-T08", EstadoReporte.ACTIVO, List.of()));
        // Act + Assert
        assertEquals(HACE_UNA_HORA, repositorio.buscarPorId("PF-T08").orElseThrow().getFechaCreacion());
    }
}
```

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** a lost-pet report with coordinates is saved and read back **THE SYSTEM SHALL** return the same pet name, contact medium and latitude.
2. **WHEN** a found-pet report is saved and read back **THE SYSTEM SHALL** keep its `descripcionMascota`.
3. **WHEN** an unknown id is looked up **THE SYSTEM SHALL** return an empty `Optional`.
4. **WHEN** ACTIVO, RESUELTO and CERRADO reports are saved **THE SYSTEM SHALL** list only the ACTIVO one as active.
5. **WHEN** a saved report is resolved or receives a sighting and is saved again **THE SYSTEM SHALL** persist the new state and the sighting, and WHEN it is read back THE SYSTEM SHALL keep its original `fechaCreacion`.
6. **WHEN** `./mvnw -q verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=RepositorioReportesH2IT` runs **THE SYSTEM SHALL** exit 0 against a real embedded H2.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
./mvnw -q verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=RepositorioReportesH2IT
./mvnw -q clean test
```

**Checkpoint**

```bash
git add -A && git commit -m "E3-T3: Implementar el adaptador de persistencia H2"
git tag step-10-adaptador-h2
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-10-adaptador-h2`. Never invent the tag: copy the `checkpoint` field.

---

### `E3-T4` — Implementar el servicio del asistente con respaldo regex

**Depends on:** `E1-T4`, `E1-T5` · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-12-asistente`

`ServicioAsistente implements AsistenteReportes` recibe una `List<ExtractorDatosReporte>` ya ordenada y **no** recibe `GestionReportes`: por construcción no puede publicar; publicar es siempre `POST /api/reportes` desde la tarjeta. En cada turno prueba los extractores en orden (captura `RuntimeException` y sigue), fusiona con `BorradorReporte.fusionar`, y calcula faltantes y pregunta con `camposFaltantes()` y `Campo.pregunta()`. Texto nulo o vacío devuelve el borrador tal cual con la pregunta pendiente. `ExtractorRegex implements ExtractorDatosReporte` (`nombre()` = `regex`) vive en `application/service` porque es lógica pura: usa `InterpreteComandoVoz` para las frases completas y además reconoce en cualquier frase un celular colombiano (`3` + 9 dígitos, con espacios o `+57` opcionales) o un correo como `contactoMedio`, `perd`→PERDIDA, `encontr`→ENCONTRADA y las especies perro/gato. `ConfiguracionAsistente` declara `@Bean @Order(100) ExtractorRegex` y `@Bean AsistenteReportes asistenteReportes(List<ExtractorDatosReporte> extractores)`; E3-T6 agrega Claude con `@Order(10)` sin tocar este archivo.

**Files**
- `src/main/java/petfinder/application/service/ServicioAsistente.java`
- `src/main/java/petfinder/application/service/ExtractorRegex.java`
- `src/main/java/petfinder/config/ConfiguracionAsistente.java`
- `src/test/java/petfinder/application/asistente/ServicioAsistenteTest.java`
- `src/test/java/petfinder/application/asistente/ExtractorRegexTest.java`

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** the first extractor returns data **THE SYSTEM SHALL** merge it into the draft and report that extractor's `nombre()` as `fuente`.
2. **WHEN** the first extractor throws or returns empty **THE SYSTEM SHALL** use the next one, and WHEN none answers THE SYSTEM SHALL keep the draft unchanged and report `fuente` `ninguna`.
3. **WHEN** fields are missing **THE SYSTEM SHALL** return as `pregunta` the question of the first missing field, and WHEN none is missing THE SYSTEM SHALL return `Listo. Revisa la tarjeta y publica.`
4. **WHEN** a turn produces a type but the draft has no description **THE SYSTEM SHALL** use the turn's text as `descripcion`.
5. **WHEN** `ExtractorRegex` reads `perdí un perro llamado Max en Chía, mi número es 300 123 4567` **THE SYSTEM SHALL** extract PERDIDA, `Max`, `perro`, `Chía` and `3001234567`.
6. **WHEN** the app boots **THE SYSTEM SHALL** expose an `AsistenteReportes` bean, and `sh scripts/humo.sh` SHALL exit 0.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
./mvnw -q test -Dtest='ServicioAsistenteTest,ExtractorRegexTest'
./mvnw -q clean test
./mvnw -q -DskipTests package
sh scripts/humo.sh
```

**Checkpoint**

```bash
git add -A && git commit -m "E3-T4: Implementar el servicio del asistente con respaldo regex"
git tag step-12-asistente
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-12-asistente`. Never invent the tag: copy the `checkpoint` field.

---

### `E3-T5` — Conectar H2 a la aplicación y cargar datos de ejemplo

**Depends on:** `E3-T3`, `E1-T5` · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-14-h2-integrado`

Cambia **una sola cosa** en `ConfiguracionPetFinder`: el bean del puerto `RepositorioReportes` pasa de memoria a H2 (el en memoria se conserva para las pruebas unitarias). Agrega `DatosDeEjemplo`, un `ApplicationRunner` que entra por el puerto `GestionReportes` (los datos pasan por las mismas validaciones) y que solo corre en la aplicación web (`@ConditionalOnWebApplication`), así la demo de consola conserva su `PF-001`. Toda prueba `@SpringBootTest` fija `ANTHROPIC_API_KEY=` para no llamar a Claude aunque la persona tenga su clave en `.env`.

**Files**
- `src/main/java/petfinder/config/ConfiguracionPetFinder.java`
- `src/main/java/petfinder/config/DatosDeEjemplo.java`
- `src/main/resources/application.properties`
- `src/test/java/petfinder/integracion/persistencia/ServicioReportesH2IT.java`
- `src/test/java/petfinder/integracion/persistencia/DatosDeEjemploIT.java`

**Contenido literal** — escríbelo tal cual; este código ya se compiló y probó.

**`src/main/java/petfinder/config/ConfiguracionPetFinder.java`** — edición. Reemplaza este método:

```java
    @Bean
    public RepositorioReportes repositorioReportes() {
        return new RepositorioReportesEnMemoria();
    }
```

por este:

```java
    /**
     * La aplicación guarda en H2. RepositorioReportesEnMemoria sigue existiendo
     * para las pruebas unitarias: cambiar de adaptador es cambiar esta línea.
     */
    @Bean
    public RepositorioReportes repositorioReportes(ReporteJpaRepository jpa) {
        return new RepositorioReportesH2(jpa);
    }
```

y en los imports cambia `import petfinder.adaptadores.salida.persistencia.memoria.RepositorioReportesEnMemoria;` por

```java
import petfinder.adaptadores.salida.persistencia.h2.ReporteJpaRepository;
import petfinder.adaptadores.salida.persistencia.h2.RepositorioReportesH2;
```

**`src/main/java/petfinder/config/DatosDeEjemplo.java`** — nuevo

```java
package petfinder.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import petfinder.application.port.entrada.GestionReportes;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

/**
 * Carga casos de ejemplo al arrancar, porque H2 en memoria empieza vacío en
 * cada reinicio (también en Render). Entra por el puerto GestionReportes, como
 * cualquier otro adaptador: los datos pasan por las mismas validaciones.
 * Solo corre en la aplicación web (la demo de consola conserva sus PF-001 y
 * PF-002 del Corte 1) y se apaga con petfinder.datos-ejemplo=false.
 */
@Component
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "petfinder.datos-ejemplo", havingValue = "true", matchIfMissing = true)
public class DatosDeEjemplo implements ApplicationRunner {

    private final GestionReportes gestion;

    public DatosDeEjemplo(GestionReportes gestion) {
        this.gestion = gestion;
    }

    @Override
    public void run(ApplicationArguments args) {
        gestion.registrar(TipoReporte.PERDIDA, SolicitudReporte.paraPerdida(
                new Ubicacion("Cedritos", "Parque de la 147", 4.7235, -74.0417),
                "Se escapó del conjunto esta tarde. Es tímida: si la ves, no la persigas.",
                new Contacto("Camila", "3001234567"),
                new Mascota("Luna", "perro", "criolla", "blanca", "mancha negra en el lomo, collar rojo")));
        gestion.registrar(TipoReporte.PERDIDA, SolicitudReporte.paraPerdida(
                new Ubicacion("Usaquén", "Parque de Usaquén", 4.6946, -74.0302),
                "Salió corriendo cuando abrieron la puerta.",
                new Contacto("Andrés", "3109876543"),
                new Mascota("Max", "perro", "labrador", "café", "collar azul")));
        gestion.registrar(TipoReporte.ENCONTRADA, SolicitudReporte.paraEncontrada(
                new Ubicacion("Calle 147", "Portal de un edificio", 4.7303, -74.0460),
                "Apareció en el portal y la tengo en casa.",
                new Contacto("Lorenzi", "3007654321"),
                "Gata gris adulta, muy mansa, sin collar"));
        gestion.registrar(TipoReporte.PERDIDA, SolicitudReporte.paraPerdida(
                new Ubicacion("Suba", "Centro comercial Santafé", 4.7417, -74.0833),
                "Se perdió en el parqueadero.",
                new Contacto("Sofía", "sofia@correo.co"),
                new Mascota("Copito", "conejo", "", "blanco", "orejas caídas")));
    }
}
```

**`src/main/resources/application.properties`** — agrega al final:

```properties

# Casos de ejemplo al arrancar (H2 empieza vacío en cada reinicio)
petfinder.datos-ejemplo=true
```

**`src/test/java/petfinder/integracion/persistencia/ServicioReportesH2IT.java`** — nuevo

```java
package petfinder.integracion.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import petfinder.application.port.entrada.GestionReportes;
import petfinder.application.port.salida.RepositorioReportes;
import petfinder.adaptadores.salida.persistencia.h2.RepositorioReportesH2;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.model.Ubicacion;

/**
 * Frontera caso de uso + adaptador + base de datos, sin ningún mock. Se
 * inyecta el puerto, no la clase del servicio.
 */
@SpringBootTest(properties = {"petfinder.datos-ejemplo=false", "ANTHROPIC_API_KEY="})
@Transactional
class ServicioReportesH2IT {

    @Autowired
    private GestionReportes gestion;

    @Autowired
    private RepositorioReportes repositorio;

    private SolicitudReporte solicitud() {
        return SolicitudReporte.paraPerdida(new Ubicacion("Chía", "Parque principal"), "Se escapó",
                new Contacto("Ana", "3001112233"), new Mascota("Max", "perro", "criollo", "café", "collar rojo"));
    }

    @Test
    @DisplayName("La aplicación usa el adaptador H2 como repositorio")
    void usaElAdaptadorH2() {
        assertTrue(repositorio instanceof RepositorioReportesH2);
    }

    @Test
    @DisplayName("Registrar y consultar un reporte pasa por la base de datos")
    void registrarYConsultar() {
        // Act
        ReporteMascota creado = gestion.registrar(TipoReporte.PERDIDA, solicitud());
        // Assert
        assertEquals(creado.getId(), gestion.consultar(creado.getId()).getId());
        assertTrue(gestion.listarActivos().stream().anyMatch(r -> r.getId().equals(creado.getId())));
    }

    @Test
    @DisplayName("Resolver un reporte queda persistido")
    void resolverQuedaPersistido() {
        // Arrange
        ReporteMascota creado = gestion.registrar(TipoReporte.PERDIDA, solicitud());
        // Act
        gestion.resolver(creado.getId());
        // Assert
        assertEquals(EstadoReporte.RESUELTO, gestion.consultar(creado.getId()).getEstado());
    }
}
```

**`src/test/java/petfinder/integracion/persistencia/DatosDeEjemploIT.java`** — nuevo

```java
package petfinder.integracion.persistencia;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import petfinder.application.port.entrada.GestionReportes;
import petfinder.domain.model.ReporteMascota;

/** La aplicación web arranca con los casos de ejemplo cargados por el puerto. */
@SpringBootTest(properties = "ANTHROPIC_API_KEY=")
class DatosDeEjemploIT {

    @Autowired
    private GestionReportes gestion;

    @Test
    @DisplayName("Al arrancar la app web se cargan los casos de ejemplo")
    void cargaLosCasosDeEjemplo() {
        // Act
        List<String> resumenes = gestion.listarActivos().stream().map(ReporteMascota::resumen).toList();
        // Assert
        assertTrue(resumenes.stream().anyMatch(r -> r.contains("Luna")));
        assertTrue(resumenes.stream().anyMatch(r -> r.contains("Max")));
        assertTrue(resumenes.stream().anyMatch(r -> r.contains("Gata gris")));
        assertTrue(resumenes.stream().anyMatch(r -> r.contains("Copito")));
    }
}
```

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** the application context starts **THE SYSTEM SHALL** inject `RepositorioReportesH2` as the `RepositorioReportes` port.
2. **WHEN** a report is registered and then resolved through `GestionReportes` **THE SYSTEM SHALL** read it back from H2 with state RESUELTO.
3. **WHEN** the web application starts with default properties **THE SYSTEM SHALL** list the sample cases Luna, Max, Gata gris and Copito among the active reports.
4. **WHEN** the console demo runs **THE SYSTEM SHALL** still print `Creado: PF-001` for its first report, because sample data loads only in the web application.
5. **WHEN** `./mvnw -q clean verify` runs **THE SYSTEM SHALL** exit 0 with unit and integration tests passing and write `target/site/jacoco/index.html`.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
./mvnw -q verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test='ServicioReportesH2IT,DatosDeEjemploIT'
./mvnw -q clean verify
test -f target/site/jacoco/index.html
sh scripts/humo.sh
./mvnw -q compile exec:java | grep -q "Creado: PF-001"
```

**Checkpoint**

```bash
git add -A && git commit -m "E3-T5: Conectar H2 a la aplicación y cargar datos de ejemplo"
git tag step-14-h2-integrado
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-14-h2-integrado`. Never invent the tag: copy the `checkpoint` field.

---

### `E3-T6` — Agregar el extractor con Claude y su tope diario

**Depends on:** `E3-T4`, `E3-T5` · **Priority:** p1 — metadata for scope cuts, not a running order · **Checkpoint:** `step-15-extractor-claude`

Segundo adaptador del puerto `ExtractorDatosReporte`. **Antes de escribir código del SDK, deja que se active el skill `claude-api`** (se activa solo al mencionar Claude o Anthropic) y toma de ahí la sintaxis exacta del SDK de Java; no la escribas de memoria. Estructura: `ExtractorClaude` (`nombre()` = `claude`) depende de una interfaz anidada `ExtractorClaude.ClienteModelo { String completarJson(String sistema, String mensaje); }` para poder probarlo con un doble; `ClienteModeloAnthropic` es el **único archivo** que importa `com.anthropic` (dependencia `com.anthropic:anthropic-java` de §11). Petición: modelo recibido por constructor, `max_tokens` 1024, `output_config.effort` = `low`, salida estructurada `output_config.format` tipo `json_schema` con el esquema de §17, sin prefill, timeout 10 s y 0 reintentos (el respaldo es regex). El sistema del prompt pide extraer solo lo que la frase menciona y usar el borrador de contexto para interpretar respuestas cortas (un número suelto es el contacto si el contacto falta). `ExtractorClaude` cuenta llamadas por día (`AtomicInteger` + `LocalDate`) y devuelve vacío al llegar al tope o si la clave está vacía, sin llamar al cliente. `ConfiguracionIa` crea `@Bean @Order(10)` el extractor leyendo `@Value("${ANTHROPIC_API_KEY:}")`, `@Value("${PETFINDER_IA_MODELO:claude-sonnet-5}")` y `@Value("${PETFINDER_IA_TOPE_DIARIO:200}")`. Nunca registres la clave en logs.

**Files**
- `src/main/java/petfinder/adaptadores/salida/ia/ExtractorClaude.java`
- `src/main/java/petfinder/adaptadores/salida/ia/ClienteModeloAnthropic.java`
- `src/main/java/petfinder/config/ConfiguracionIa.java`
- `pom.xml`
- `src/test/java/petfinder/adaptadores/ia/ExtractorClaudeTest.java`

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** the fake `ClienteModelo` returns `{"tipo":"PERDIDA","nombre":"Luna","especie":"perro","zona":"Cedritos"}` **THE SYSTEM SHALL** return a draft with those four values, and `nombre()` SHALL be `claude`.
2. **WHEN** the model returns text that is not valid JSON **THE SYSTEM SHALL** return `Optional.empty()` so the service falls back to regex.
3. **WHEN** the daily cap is reached **THE SYSTEM SHALL** return `Optional.empty()` without calling `ClienteModelo`.
4. **WHEN** `ANTHROPIC_API_KEY` is blank **THE SYSTEM SHALL** return `Optional.empty()` without calling `ClienteModelo`, and the app SHALL still boot with health 200.
5. **WHEN** ArchUnit runs **THE SYSTEM SHALL** find `com.anthropic` imported only inside `petfinder.adaptadores.salida.ia`.
6. **WHEN** the model id is resolved **THE SYSTEM SHALL** read `PETFINDER_IA_MODELO` with default `claude-sonnet-5` in `ConfiguracionIa`, and `ClienteModeloAnthropic` SHALL contain no model literal.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
./mvnw -q test -Dtest=ExtractorClaudeTest
./mvnw -q clean test
sh scripts/contiene.sh src/main/java/petfinder/config/ConfiguracionIa.java 'PETFINDER_IA_MODELO:claude-sonnet-5' 'PETFINDER_IA_TOPE_DIARIO:200' 'ANTHROPIC_API_KEY:'
sh scripts/contiene.sh src/main/java/petfinder/adaptadores/salida/ia/ClienteModeloAnthropic.java '!claude-sonnet-5'
./mvnw -q -DskipTests package
sh scripts/humo.sh
```

**Checkpoint**

```bash
git add -A && git commit -m "E3-T6: Agregar el extractor con Claude y su tope diario"
git tag step-15-extractor-claude
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-15-extractor-claude`. Never invent the tag: copy the `checkpoint` field.

---

### `E3-T7` — Construir la interfaz: casos, mapa y tarjeta viva

**Depends on:** `E3-T2`, `E2-T1`, `E2-T4` · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-16-interfaz-app`

Implementa las vistas de `docs/diseno/vistas.html` (secciones 05, 06 y 07) sobre el esqueleto de E3-T2, con el skill `diseno-pet-finder`. **`app.js`** enruta por hash (`#/`, `#/nuevo`, `#/caso/{id}`, `#/caso/{id}/avistamiento`, `#/publicado/{id}`) y usa solo `api.js` para hablar con el servidor. **`tarjeta-viva.js`** mantiene el borrador del turno: cada frase (de `voz.js` o de `entrada-texto`) va a `turnoAsistente`, pinta los campos que cambiaron con la animación de §9 de DESIGN.md, muestra la `pregunta`, deja cada campo editable a mano y solo publica con `boton-publicar` (→ `crearReporte` con el borrador). Si `soportaVoz()` es falso, el micrófono se oculta y el campo de texto sigue ahí. **`mapa.js`** usa Leaflet 1.9.4 de cdnjs y teselas CARTO (`light_all` / `dark_all` según el tema), pinta un pin por caso con coordenadas y su color de estado, y en `#/caso/{id}/avistamiento` permite arrastrar el pin. En escritorio (≥ 1200 px) la disposición es de tres columnas; en celular, mapa con hoja inferior y barra de pestañas. Los errores de `ErrorApi` se muestran en `data-prueba="mensaje"`, nunca con `alert()`.

**Files**
- `src/main/resources/static/js/app.js`
- `src/main/resources/static/js/tarjeta-viva.js`
- `src/main/resources/static/js/mapa.js`
- `src/main/resources/static/index.html`
- `src/main/resources/static/css/app.css`

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** `sh scripts/humo.sh` requests `/`, `/css/tokens.css`, `/css/app.css`, the modules `/js/app.js`, `/js/api.js`, `/js/voz.js`, `/js/tarjeta-viva.js`, `/js/mapa.js` and `/img/marca.svg` **THE SYSTEM SHALL** answer 200 for each.
2. **WHEN** `index.html` is read **THE SYSTEM SHALL** carry every `data-prueba` hook of the UI contract: lista-casos, mapa, boton-nuevo-reporte, entrada-texto, boton-enviar-texto, boton-microfono, tarjeta-viva, boton-publicar, mensaje, boton-la-vi and boton-enviar-pista.
3. **WHEN** `sh scripts/verificar-tokens.sh` runs **THE SYSTEM SHALL** still find every token and zero hexadecimal colors in `app.css`.
4. **WHEN** the map loads **THE SYSTEM SHALL** use Leaflet 1.9.4 from cdnjs with CARTO light and dark tiles and the attribution `© OpenStreetMap © CARTO`.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
sh scripts/verificar-tokens.sh
sh scripts/contiene.sh src/main/resources/static/index.html 'data-prueba="lista-casos"' 'data-prueba="mapa"' 'data-prueba="boton-nuevo-reporte"' 'data-prueba="entrada-texto"' 'data-prueba="boton-enviar-texto"' 'data-prueba="boton-microfono"' 'data-prueba="tarjeta-viva"' 'data-prueba="boton-publicar"' 'data-prueba="mensaje"' 'data-prueba="boton-la-vi"' 'data-prueba="boton-enviar-pista"' 'leaflet/1.9.4' 'js/app.js'
sh scripts/contiene.sh src/main/resources/static/js/mapa.js 'basemaps.cartocdn.com' '© OpenStreetMap' '© CARTO'
./mvnw -q -DskipTests package
sh scripts/humo.sh / /css/tokens.css /css/app.css /js/app.js /js/api.js /js/voz.js /js/tarjeta-viva.js /js/mapa.js /img/marca.svg
```

**Checkpoint**

```bash
git add -A && git commit -m "E3-T7: Construir la interfaz: casos, mapa y tarjeta viva"
git tag step-16-interfaz-app
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-16-interfaz-app`. Never invent the tag: copy the `checkpoint` field.

---

### `E3-T8` — Convertir en PWA y empaquetar en Docker

**Depends on:** `E3-T7` · **Priority:** p1 — metadata for scope cuts, not a running order · **Checkpoint:** `step-18-pwa-y-docker`

PWA para celular y tablet, con los datos de §11 de DESIGN.md: `manifest.webmanifest` con `name`/`short_name` `Pet Finder`, `display` `standalone`, `start_url` `/`, `background_color` y `theme_color` `#F5F5F7`, e íconos `img/icono-192.png`, `img/icono-512.png` y `img/icono-512-maskable.png` (`purpose: maskable`), que ya llegaron con el workspace. `index.html` agrega `<link rel="manifest">`, `<link rel="apple-touch-icon" href="img/icono-180.png">`, `apple-mobile-web-app-capable`, `theme-color` para claro y oscuro, y registra `sw.js`. `sw.js` precachea la interfaz (HTML, CSS, JS, imágenes) y para `/api/` siempre va a la red; sin conexión responde la interfaz con el aviso `Sin conexión. Te mostramos lo último que vimos.`. `Dockerfile` en dos etapas: `eclipse-temurin:17-jdk` compila con `./mvnw -q -DskipTests package` y `eclipse-temurin:17-jre` corre `java -XX:MaxRAMPercentage=70 -jar /app/pet-finder.jar` con `EXPOSE 8080` (Render define `PORT`). `.dockerignore` excluye `blueprints/`, `target/`, `.git/`, `.env` y `.env.*`. Requiere Docker en marcha.

**Files**
- `src/main/resources/static/manifest.webmanifest`
- `src/main/resources/static/sw.js`
- `src/main/resources/static/index.html`
- `Dockerfile`
- `.dockerignore`

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** `/manifest.webmanifest` is served **THE SYSTEM SHALL** declare the name `Pet Finder`, `standalone` display and the 192, 512 and maskable 512 icons, and each icon SHALL answer 200.
2. **WHEN** `index.html` loads **THE SYSTEM SHALL** link the manifest and the 180 px `apple-touch-icon` and register `sw.js`.
3. **WHEN** `sw.js` handles a request whose path starts with `/api/` **THE SYSTEM SHALL** go to the network and never answer it from cache.
4. **WHEN** `sh scripts/humo-docker.sh` builds the image from `Dockerfile` and runs it **THE SYSTEM SHALL** answer health 200 inside the container.
5. **WHEN** Docker builds **THE SYSTEM SHALL** exclude `blueprints/`, `target/` and `.env` from the build context.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
sh scripts/contiene.sh src/main/resources/static/manifest.webmanifest '"name": "Pet Finder"' '"display": "standalone"' 'img/icono-192.png' 'img/icono-512.png' 'img/icono-512-maskable.png' 'maskable'
sh scripts/contiene.sh src/main/resources/static/index.html 'manifest.webmanifest' 'apple-touch-icon' 'img/icono-180.png' 'serviceWorker'
sh scripts/contiene.sh src/main/resources/static/sw.js '/api/'
sh scripts/contiene.sh .dockerignore 'blueprints/' 'target/' '.env'
./mvnw -q -DskipTests package
sh scripts/humo.sh /manifest.webmanifest /sw.js /img/icono-180.png /img/icono-192.png /img/icono-512.png /img/icono-512-maskable.png
sh scripts/humo-docker.sh
```

**Checkpoint**

```bash
git add -A && git commit -m "E3-T8: Convertir en PWA y empaquetar en Docker"
git tag step-18-pwa-y-docker
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-18-pwa-y-docker`. Never invent the tag: copy the `checkpoint` field.

---

### `E3-T9` — Correr baseline y carga, y analizar el cuello de botella

**Depends on:** `E3-T1`, `E3-T5`, `E2-T4` · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-19-carga-k6`

Con la app corriendo **sin clave de Anthropic** (`./mvnw spring-boot:run`, o el jar), en otra terminal: `k6 run --summary-export perf/resultados/baseline.json perf/scripts/baseline.js` y luego `k6 run --summary-export perf/resultados/carga.json perf/scripts/carga.js` (con `BASE_URL` si k6 corre en la VM). Si el SLO no se cumple, k6 sale distinto de 0 y **eso es un resultado válido**: se reporta tal cual; reconocer un límite vale más que maquillarlo. Guarda capturas del resumen y del uso de CPU/RAM en `perf/resultados/`. En el README: tabla (prueba, VUs, duración, p95, req/s, errores, ¿cumple?), el cuello de botella con datos (`voz_listar` devuelve todos los activos sin paginar y la lista crece durante la prueba; con `spring.jpa.show-sql=true` cuenta las consultas de un `GET` para ver si hay N+1), qué ofrece y qué no la arquitectura, y la verificación de IDs con la consola H2 (`http://localhost:8080/h2-console`).

**Files**
- `perf/README.md`
- `perf/resultados/*.json`
- `perf/resultados/*.png`

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** the baseline (5 VUs, 1 min) and load (50 VUs for 3 min) scripts run against the application **THE SYSTEM SHALL** write `perf/resultados/baseline.json` and `perf/resultados/carga.json` containing `http_req_duration` percentiles.
2. **WHEN** `perf/README.md` is read **THE SYSTEM SHALL** report p95, throughput and error rate per test and state whether the SLO is met, with no `__` placeholder left.
3. **WHEN** the bottleneck is analysed **THE SYSTEM SHALL** compare the `voz_registrar` and `voz_listar` latencies and state what the hexagonal architecture offers (pagination or `JOIN FETCH` only in the H2 adapter) and what it does not (H2 in the same process as the API).
4. **WHEN** concurrency is checked **THE SYSTEM SHALL** record the result of `SELECT COUNT(*) FROM REPORTES` next to the number of 201 responses k6 counted.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
test -s perf/resultados/baseline.json
test -s perf/resultados/carga.json
sh scripts/contiene.sh perf/resultados/carga.json 'http_req_duration' 'p(95)'
sh scripts/contiene.sh perf/README.md 'Resultados' 'Baseline' 'Carga' 'voz_registrar' 'voz_listar' 'JOIN FETCH' 'SELECT COUNT(*) FROM REPORTES' '!__'
```

**Checkpoint**

```bash
git add -A && git commit -m "E3-T9: Correr baseline y carga, y analizar el cuello de botella"
git tag step-19-carga-k6
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-19-carga-k6`. Never invent the tag: copy the `checkpoint` field.

---

## Epic acceptance

The epic is done when every task is `done` **and**:

1. **WHEN** the web app starts from the Docker image **THE SYSTEM SHALL** answer health 200, serve the PWA manifest and list the sample cases through the API.
2. **WHEN** `ANTHROPIC_API_KEY` is blank **THE SYSTEM SHALL** keep the assistant working through `ExtractorRegex`, and `./mvnw -q clean verify` **SHALL** exit 0 without any network call to Anthropic.

```bash
./mvnw -q clean verify
./mvnw -q -DskipTests package && sh scripts/humo.sh /api/reportes /manifest.webmanifest /js/tarjeta-viva.js
sh scripts/humo-docker.sh
```

Run from the project root. Both criteria must be decidable by these commands.

## Pitfalls

- **Reemplazar la lista de avistamientos de la entidad** — provoca `A collection with cascade=all-delete-orphan was no longer referenced`. Limpia con `clear()` y vuelve a agregar.
- **Leer los avistamientos fuera de la transacción** — `LazyInitializationException`. El mapeo a dominio ocurre dentro de los métodos `@Transactional` del adaptador.
- **Escribir el SDK de Anthropic de memoria** — deja que el skill `claude-api` se active y toma la sintaxis de ahí; el modelo sale de `PETFINDER_IA_MODELO`, nunca de un literal en la llamada.
- **Correr k6 contra la app con la clave puesta** — mides a Anthropic, no a Pet Finder, y gastas el presupuesto. Carga siempre sin clave.
- **Micrófono en `http://192.168.x.x`** — el navegador solo da el micrófono en `https` o en `http://localhost`. Para probar en el celular, usa la URL `https` de Render o un túnel de Cloudflare.
- **Cachear la API en `sw.js`** — la lista de casos quedaría vieja. Solo la interfaz se precachea; `/api/` va siempre a la red.

## Before moving on

- [ ] Every task in this epic is `done` in `tasks.json` — no task left `in_progress`.
- [ ] Every `verify` command of every task in this epic passed, not just the first one.
- [ ] No `verify` command was edited, and none was skipped because a file it names did not exist.
- [ ] **Every task in this epic has its `checkpoint` tag in version control** — one tag per task, matching the `checkpoint` value in `tasks.json`. `git tag -l 'step-*'` lists them, and they were pushed with `git push origin <tag>`.
- [ ] Gate command passes clean, run from the project root: `./mvnw -q clean verify`.
- [ ] Every "Produced" contract above exists with the stated signature.
- [ ] No file outside the subtree was modified.
- [ ] `.env.example` updated if this epic added a variable — this project's variables are `ANTHROPIC_API_KEY`, `PETFINDER_IA_MODELO`, `PETFINDER_IA_TOPE_DIARIO` and `PORT`.
- [ ] One commit per task, each prefixed with its task id, each followed by its checkpoint tag — **hechos por la persona responsable**.
- [ ] Las ramas entraron a `main` por Pull Request con *merge commit* (no *squash*): con *squash* los tags quedan apuntando a commits que no están en `main`.
