# Épica 02: Adaptador web y pruebas

> Existe una API REST que traduce HTTP a los puertos, un cliente JavaScript con captura de voz, y las pruebas con dobles, de sistema y de UI que demuestran que formulario, voz y asistente llegan al mismo caso de uso.

| | |
|---|---|
| **Epic id** | `02-adaptador-web-y-pruebas` |
| **Responsable** | Antonio Benítez — solo esta persona trabaja las tareas de esta épica |
| **Tasks** | `E2-T1` … `E2-T6` (6 tareas) |
| **Depends on** | `E1-T2` (01-nucleo-hexagonal), `E1-T4` (01-nucleo-hexagonal), `E1-T5` (01-nucleo-hexagonal), `E3-T4` (03-persistencia-asistente-e-interfaz), `E3-T5` (03-persistencia-asistente-e-interfaz), `E3-T6` (03-persistencia-asistente-e-interfaz), `E3-T7` (03-persistencia-asistente-e-interfaz) |
| **Unlocks** | `E3-T7`, `E3-T9`, `E1-T6` |
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

## Directory subtree

Only the parts this epic touches:

```
src/main/resources/static/js/api.js                    # E2-T1 NEW — cliente fetch de la API
src/main/resources/static/js/voz.js                    # E2-T1 NEW — captura con Web Speech (es-CO)
src/main/java/petfinder/adaptadores/entrada/web/
  dto/SolicitudReporteDTO.java                         # E2-T3 NEW
  dto/ReporteDTO.java                                  # E2-T3 NEW — resumen() y detalle(), enmascarado
  dto/AvistamientoDTO.java                             # E2-T3 NEW
  ManejadorErrores.java                                # E2-T3 NEW — @RestControllerAdvice
  ReporteController.java                               # E2-T4 NEW
  AvistamientoController.java                          # E2-T4 NEW
  VozController.java                                   # E2-T4 NEW — DTO anidados
  AsistenteController.java                             # E2-T4 NEW — DTO anidados
src/test/java/petfinder/
  application/service/ServicioAvistamientosMockTest.java   # E2-T2 NEW
  application/service/ServicioReportesMockTest.java        # E2-T2 NEW
  adaptadores/entrada/web/MapeoDtoTest.java                # E2-T3 NEW
  adaptadores/entrada/web/ControladoresWebTest.java        # E2-T4 NEW — @WebMvcTest
  integracion/sistema/FlujoReportePerdidaSistemaIT.java    # E2-T5 NEW — caja negra por HTTP
  integracion/sistema/FlujoVozSistemaIT.java               # E2-T5 NEW
  ui/PaginaInicio.java, ui/PaginaNuevoReporte.java         # E2-T6 NEW — Page Objects
  ui/FlujosPrincipalesUIT.java                             # E2-T6 NEW — Selenium, perfil ui
pom.xml                                                # E2-T6 EDIT — selenium-java (test) y perfil ui
docs/pruebas.md                                        # E2-T5 NEW
README.md                                              # E2-T5 EDIT — sección "Cómo ejecutar"
```

Everything outside this subtree is out of scope. `scripts/*.sh`, `mvnw` y la configuración de agentes ya están en la raíz: los copió el Bootstrap desde el `workspace/` del bundle. If a task seems to require editing a file not listed here, stop and report.

## Data model touched here

| Entity | Fields this epic adds or reads | Notes |
|---|---|---|
| `ReporteMascota`, `ReportePerdida`, `ReporteEncontrada` | lee todos los getters | El `instanceof` para saber el tipo vive solo en `ReporteDTO` |
| `Ubicacion` | `latitud`, `longitud`, `aproximada()` | En listas y mapa se publican redondeadas |
| `BorradorReporte` | los 14 campos | Se traduce desde y hacia `BorradorDTO` en `AsistenteController` |
| `Avistamiento` | todos | El ID `AV-xxxxxxxx` se genera en `AvistamientoController` |

## Contracts

**Consumed** — already exists, do not rebuild:

| From | Interface | Guarantee |
|---|---|---|
| `01-nucleo-hexagonal` (E1-T2, E1-T4, E1-T5) | Puertos de entrada y tipos del dominio (firmas abajo) | Lanzan solo `DominioException` y subclases |
| `03-persistencia-asistente-e-interfaz` (E3-T4) | Bean `AsistenteReportes` | Sin estado; nunca publica |
| `03-persistencia-asistente-e-interfaz` (E3-T5) | H2 como `RepositorioReportes` y datos de ejemplo | Las pruebas de sistema corren contra H2 real |
| `03-persistencia-asistente-e-interfaz` (E3-T7) | Ganchos `data-prueba` de `index.html` | Tabla abajo; no se renombran |

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

**Produced** — later epics depend on exactly these signatures. Changing one breaks them:

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
- **Los controladores dependen solo de interfaces de puertos**, nunca de `ServicioReportes` ni de `ServicioAvistamientos`: ArchUnit (`laWebSoloHablaConPuertos`) lo rompe si pasa.
- **Sin reglas de negocio ni `try/catch` de excepciones del dominio en los controladores**: solo traducen HTTP ↔ puerto; los errores los traduce `ManejadorErrores`.
- **Sufijos**: `*Test` es unitaria (corre con `mvnw test`), `*IT` es integración o sistema (corre con `mvnw verify`), `*UIT` es UI (solo con `-Pui`). Si confundes el sufijo, se mezclan los niveles.
- **En pruebas de sistema la base no se revierte**: usa siempre el ID que devolvió el POST y compara tamaños antes y después.

Full project rules: `CLAUDE.md`. Area rules: `.claude/rules/web.md`, `.claude/rules/interfaz.md`, `.claude/rules/pruebas.md`. Both sit in the project root — the builder copied them there from the bundle's `workspace/` before task one.

---

## Tasks

Listed in the same order as `tasks.json`. That order is the build order — work top to bottom and do not re-rank by priority or by what looks quick. Una tarea está lista cuando todas sus dependencias (de cualquier épica) están en `done`: antes de empezar, haz `git pull` de `main` para tener el trabajo de los demás.

### `E2-T1` — Escribir el cliente de la API y la captura de voz

**Depends on:** nothing · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-02-cliente-js`

Dos módulos ES sin dependencias, servidos por Spring desde `static/js/`. **`api.js`** envuelve `fetch` para cada ruta de §5 (tabla más abajo): envía y recibe JSON, y ante un estado no 2xx lanza `ErrorApi(status, mensaje)` con el campo `error` del cuerpo; nunca usa `alert()` (la página muestra el mensaje en `data-prueba="mensaje"`). **`voz.js`** encapsula Web Speech: `soportaVoz()` devuelve `false` si no existen `SpeechRecognition` ni `webkitSpeechRecognition` (Firefox, algunos iPhone), `iniciarCaptura({ alParcial, alFinal, alError })` usa `lang = 'es-CO'`, `interimResults = true`, y `detenerCaptura()` corta. El servidor nunca recibe audio: solo texto. Esta tarea no necesita el servidor; el comportamiento en navegador lo prueba E2-T6 con Selenium.

**Files**
- `src/main/resources/static/js/api.js`
- `src/main/resources/static/js/voz.js`

**Contenido literal** — escríbelo tal cual; este código ya se compiló y probó.

**Contrato de `api.js`** (una función por ruta de §5; todas devuelven la respuesta ya parseada):

| Función | Método y ruta | Devuelve |
|---|---|---|
| `listarReportes()` | `GET /api/reportes` | `ReporteDTO[]` (resumen) |
| `consultarReporte(id)` | `GET /api/reportes/{id}` | `ReporteDTO` (detalle) |
| `crearReporte(solicitud)` | `POST /api/reportes` | `ReporteDTO` (201) |
| `registrarAvistamiento(id, avistamiento)` | `POST /api/reportes/{id}/avistamientos` | `ReporteDTO` (201) |
| `resolverReporte(id)` / `cerrarReporte(id)` | `POST /api/reportes/{id}/resolver` · `/cerrar` | nada (204) |
| `enviarVoz(texto, nombreContacto, medioContacto)` | `POST /api/voz` | `RespuestaVozDTO` |
| `turnoAsistente(borrador, texto)` | `POST /api/asistente/turno` | `RespuestaTurnoDTO` |

```javascript
export class ErrorApi extends Error {
  constructor(status, mensaje) { super(mensaje); this.status = status; }
}
// Toda llamada no 2xx termina aquí: el mensaje es el campo `error` del servidor.
async function pedir(metodo, ruta, cuerpo) {
  const r = await fetch(ruta, {
    method: metodo,
    headers: cuerpo ? { 'Content-Type': 'application/json' } : {},
    body: cuerpo ? JSON.stringify(cuerpo) : undefined,
  });
  if (r.status === 204) return null;
  const datos = await r.json().catch(() => ({}));
  if (!r.ok) throw new ErrorApi(r.status, datos.error || 'No pudimos completar la operación.');
  return datos;
}
```

**Contrato de `voz.js`:**

```javascript
const Reconocedor = window.SpeechRecognition || window.webkitSpeechRecognition;
export function soportaVoz() { return Boolean(Reconocedor); }
// iniciarCaptura({ alParcial(texto), alFinal(texto), alError(mensaje) }) — lang 'es-CO', interimResults true.
// Solo funciona en contexto seguro: https o http://localhost. Firefox no lo soporta: la página deja el campo de texto.
```

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** `sh scripts/contiene.sh` reads `static/js/api.js` **THE SYSTEM SHALL** find the exported async functions `listarReportes`, `consultarReporte`, `crearReporte`, `registrarAvistamiento`, `resolverReporte`, `cerrarReporte`, `enviarVoz` and `turnoAsistente` and the class `ErrorApi`.
2. **WHEN** a response status is 400, 404, 409 or 422 **THE SYSTEM SHALL** reject with an `ErrorApi` whose `message` is the server's `error` field (static check: `api.js` contains `class ErrorApi` and `.error`).
3. **WHEN** `sh scripts/contiene.sh` reads `static/js/voz.js` **THE SYSTEM SHALL** find `export function soportaVoz`, `export function iniciarCaptura`, `export function detenerCaptura`, `webkitSpeechRecognition` and `es-CO`.
4. **WHEN** either module is scanned **THE SYSTEM SHALL** contain no `alert(` call.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
sh scripts/contiene.sh src/main/resources/static/js/api.js 'export async function listarReportes' 'export async function consultarReporte' 'export async function crearReporte' 'export async function registrarAvistamiento' 'export async function resolverReporte' 'export async function cerrarReporte' 'export async function enviarVoz' 'export async function turnoAsistente' 'class ErrorApi' '.error' '!alert('
sh scripts/contiene.sh src/main/resources/static/js/voz.js 'export function soportaVoz' 'export function iniciarCaptura' 'export function detenerCaptura' 'webkitSpeechRecognition' 'es-CO' '!alert('
```

**Checkpoint**

```bash
git add -A && git commit -m "E2-T1: Escribir el cliente de la API y la captura de voz"
git tag step-02-cliente-js
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-02-cliente-js`. Never invent the tag: copy the `checkpoint` field.

---

### `E2-T2` — Probar los servicios con dobles de Mockito

**Depends on:** `E1-T2` · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-06-pruebas-mockito`

Pruebas unitarias con `@ExtendWith(MockitoExtension.class)`, `@Mock RepositorioReportes` y `@Mock PublicadorAvistamientos` (Mockito viene en `spring-boot-starter-test`). Las pruebas del Corte 1 usan el repositorio en memoria; estas verifican **interacciones** que con el repositorio real no se ven. Casos: `registrarEnReporteInexistenteNoGuardaNiNotifica`, `registrarEnReporteDeEncontradaLanzaExcepcionYNoNotifica`, `registrarEnReporteResueltoLanzaExcepcionYNoNotifica`, `registrarValidoGuardaAntesDeNotificar` (`InOrder`); y en reportes `registrarConTipoSinCreadorLanzaOperacionNoPermitida`, `registrarConDatosInvalidosNoGuardaNada`, `resolverGuardaElReporteActualizado`. Cada una con `// Arrange`, `// Act`, `// Assert` y `@DisplayName` en español. Usa `ReportePerdida` reales como valores de retorno del mock; no simules el dominio.

**Files**
- `src/test/java/petfinder/application/service/ServicioAvistamientosMockTest.java`
- `src/test/java/petfinder/application/service/ServicioReportesMockTest.java`

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** an avistamiento targets a missing report, a found-pet report or a RESUELTO report **THE SYSTEM SHALL** throw the domain exception and Mockito SHALL verify `guardar` and `notificar` were never called.
2. **WHEN** a valid avistamiento is registered **THE SYSTEM SHALL** call `repositorio.guardar` before `publicador.notificar`, verified with `InOrder`.
3. **WHEN** `ServicioReportes` is built with an empty creators map **THE SYSTEM SHALL** throw `OperacionNoPermitidaException` on `registrar` and never call `guardar`.
4. **WHEN** a lost-pet request without zone is registered **THE SYSTEM SHALL** throw `DatosInvalidosException` and never call `guardar`; WHEN a report is resolved THE SYSTEM SHALL call `guardar` with that report.
5. **WHEN** `./mvnw -q test -Dtest='ServicioAvistamientosMockTest,ServicioReportesMockTest'` runs **THE SYSTEM SHALL** exit 0 with every test of both classes passing.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
./mvnw -q test -Dtest='ServicioAvistamientosMockTest,ServicioReportesMockTest'
./mvnw -q clean test
```

**Checkpoint**

```bash
git add -A && git commit -m "E2-T2: Probar los servicios con dobles de Mockito"
git tag step-06-pruebas-mockito
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-06-pruebas-mockito`. Never invent the tag: copy the `checkpoint` field.

---

### `E2-T3` — Crear los DTO web y el manejador de errores

**Depends on:** `E1-T4` · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-11-dtos-y-errores`

Los DTO son `record` y son el formato del JSON: el dominio nunca sale directo por HTTP. Campos exactos en §5 (tabla "Contratos JSON" de esta épica). `ReporteDTO` tiene dos fábricas: `resumen(ReporteMascota)` para listas y mapa (coordenadas con `Ubicacion.aproximada()` y contacto enmascarado) y `detalle(ReporteMascota)` para la página de un caso (contacto completo, avistamientos sin el contacto de quien los reportó). El `instanceof` para saber el tipo vive solo ahí. Enmascarado: teléfono → primeros 3 dígitos + un `•` por cada dígito intermedio + últimos 2; correo → 2 primeros caracteres + `•••` + `@dominio`; otro texto de más de 5 caracteres → 2 primeros + `•••`; 5 o menos → `•••`. `ManejadorErrores` es un `@RestControllerAdvice` que traduce excepciones del dominio en un solo lugar (sin `try/catch` en los controladores) y además convierte `HttpMessageNotReadableException` en 400 con `El cuerpo de la petición no es válido`.

**Files**
- `src/main/java/petfinder/adaptadores/entrada/web/dto/SolicitudReporteDTO.java`
- `src/main/java/petfinder/adaptadores/entrada/web/dto/ReporteDTO.java`
- `src/main/java/petfinder/adaptadores/entrada/web/dto/AvistamientoDTO.java`
- `src/main/java/petfinder/adaptadores/entrada/web/ManejadorErrores.java`
- `src/test/java/petfinder/adaptadores/entrada/web/MapeoDtoTest.java`

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** a `SolicitudReporteDTO` with `tipo` PERDIDA is converted **THE SYSTEM SHALL** build `SolicitudReporte.paraPerdida` with a `Mascota`, and with ENCONTRADA THE SYSTEM SHALL build `paraEncontrada` with `descripcionMascota`.
2. **WHEN** `ReporteDTO.resumen(reporte)` maps a report **THE SYSTEM SHALL** round its coordinates to 3 decimals and mask the contact: `3001234567` → `300•••••67`, `sofia@correo.co` → `so•••@correo.co`.
3. **WHEN** `ReporteDTO.detalle(reporte)` maps the same report **THE SYSTEM SHALL** keep the full contact and list its sightings without the spotter's contact.
4. **WHEN** `ManejadorErrores` handles `DatosInvalidosException`, `ReporteNoEncontradoException` or `OperacionNoPermitidaException` **THE SYSTEM SHALL** answer 400, 404 or 409 with the body `{"error": "<mensaje de la excepción>"}`.
5. **WHEN** `./mvnw -q test -Dtest=MapeoDtoTest` runs **THE SYSTEM SHALL** exit 0 with every mapping and handler test passing.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
./mvnw -q test -Dtest=MapeoDtoTest
./mvnw -q clean test
```

**Checkpoint**

```bash
git add -A && git commit -m "E2-T3: Crear los DTO web y el manejador de errores"
git tag step-11-dtos-y-errores
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-11-dtos-y-errores`. Never invent the tag: copy the `checkpoint` field.

---

### `E2-T4` — Exponer la API REST de reportes, voz y asistente

**Depends on:** `E2-T3`, `E1-T5`, `E3-T4` · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-13-controladores-rest`

Cada controlador recibe **por constructor** la interfaz del puerto (`GestionReportes`, `RegistroAvistamientos`, `ProcesadorComandosVoz`, `AsistenteReportes`), nunca un servicio; ArchUnit lo verifica. No hay reglas de negocio ni `try/catch` de excepciones del dominio: solo traducen HTTP ↔ puerto, igual que `MenuConsola` con el teclado. El ID de un avistamiento se genera aquí como `"AV-" + UUID.randomUUID().toString().substring(0, 8)` y su fecha es `LocalDateTime.now()`. `VozController` y `AsistenteController` llevan sus DTO como `record` anidados (`ComandoVozDTO`, `RespuestaVozDTO`, `TurnoDTO`, `BorradorDTO`, `RespuestaTurnoDTO`). La prueba usa `@WebMvcTest` (`org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`) con `@MockitoBean` (`org.springframework.test.context.bean.override.mockito.MockitoBean`) para cada puerto.

**Files**
- `src/main/java/petfinder/adaptadores/entrada/web/ReporteController.java`
- `src/main/java/petfinder/adaptadores/entrada/web/AvistamientoController.java`
- `src/main/java/petfinder/adaptadores/entrada/web/VozController.java`
- `src/main/java/petfinder/adaptadores/entrada/web/AsistenteController.java`
- `src/test/java/petfinder/adaptadores/entrada/web/ControladoresWebTest.java`

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** `POST /api/reportes` receives a valid lost-pet body **THE SYSTEM SHALL** answer 201 with the `ReporteDTO`, and WHEN the zone is missing THE SYSTEM SHALL answer 400 with `{"error": …}`.
2. **WHEN** `GET /api/reportes/{id}` asks for an unknown id **THE SYSTEM SHALL** answer 404, and WHEN `POST /api/reportes/{id}/resolver` hits an already resolved report THE SYSTEM SHALL answer 409.
3. **WHEN** `POST /api/reportes/{id}/avistamientos` succeeds **THE SYSTEM SHALL** answer 201 with the updated report.
4. **WHEN** `POST /api/voz` gets a recognized registration **THE SYSTEM SHALL** answer 201, a list or query 200, and an unrecognized phrase 422 with the help message.
5. **WHEN** `POST /api/asistente/turno` receives `{borrador, texto}` **THE SYSTEM SHALL** answer 200 with `borrador`, `faltantes`, `pregunta`, `fuente` and `listo`.
6. **WHEN** ArchUnit runs **THE SYSTEM SHALL** confirm that no class in `adaptadores.entrada.web` depends on `application.service`, and `sh scripts/humo.sh /api/reportes` SHALL exit 0.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
./mvnw -q test -Dtest=ControladoresWebTest
./mvnw -q clean test
./mvnw -q -DskipTests package
sh scripts/humo.sh /api/reportes
```

**Checkpoint**

```bash
git add -A && git commit -m "E2-T4: Exponer la API REST de reportes, voz y asistente"
git tag step-13-controladores-rest
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-13-controladores-rest`. Never invent the tag: copy the `checkpoint` field.

---

### `E2-T5` — Escribir las pruebas de sistema y la guía de pruebas

**Depends on:** `E2-T4`, `E3-T5` · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-17-pruebas-sistema`

Pruebas de caja negra: se levanta la aplicación completa (controlador → puerto → servicio → dominio → H2) con `@SpringBootTest(webEnvironment = RANDOM_PORT, properties = "ANTHROPIC_API_KEY=")` y `@AutoConfigureTestRestTemplate` (`org.springframework.boot.resttestclient.autoconfigure`), y se le habla **solo por HTTP** con `TestRestTemplate` (`org.springframework.boot.resttestclient.TestRestTemplate`). Sin mocks. La base no se revierte entre pruebas (el servidor atiende en otro hilo), así que **nunca afirmes un ID fijo ni un tamaño absoluto**: usa el ID que devuelve el POST o compara tamaños antes y después. La prueba `vozFormularioYAsistenteProducenElMismoReporte` es la evidencia del reto de modificabilidad. `docs/pruebas.md`: estrategia por nivel (qué, herramienta, por qué), comandos, resultados y matriz de casos con su clase de equivalencia o valor límite; Mateo agrega después su sección de persistencia y carga. En el README, la sección `Cómo ejecutar` desde cero: requisitos (JDK 17), `./mvnw spring-boot:run` / `mvnw.cmd spring-boot:run`, abrir `http://localhost:8080` en Chrome, `./mvnw test`, `./mvnw verify`, `k6 run perf/scripts/carga.js`, consola H2 y demo de consola. La rúbrica limita las pruebas y la demo a nivel Básico si el sistema no se puede ejecutar con el README: escríbelo para alguien que clona el repo por primera vez en Windows.

**Files**
- `src/test/java/petfinder/integracion/sistema/FlujoReportePerdidaSistemaIT.java`
- `src/test/java/petfinder/integracion/sistema/FlujoVozSistemaIT.java`
- `docs/pruebas.md`
- `README.md`

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** a lost pet is created over HTTP **THE SYSTEM SHALL** answer 201, show ACTIVO, accept a sighting with 201, resolve with 204, show RESUELTO and reject a new sighting with 409, using only the id returned by the POST.
2. **WHEN** a report has no zone **THE SYSTEM SHALL** answer 400, WHEN `PF-999999` is requested THE SYSTEM SHALL answer 404, and WHEN a report is resolved twice THE SYSTEM SHALL answer 409.
3. **WHEN** `perdí un perro llamado Max en Chía` is sent to `/api/voz` **THE SYSTEM SHALL** answer 201 and the new id SHALL appear in `GET /api/reportes`.
4. **WHEN** an unintelligible phrase is sent to `/api/voz` **THE SYSTEM SHALL** answer 422 and the size of `GET /api/reportes` SHALL not change.
5. **WHEN** "Max, perro, Chía" is created by form, by `/api/voz` and by an assistant draft published through `POST /api/reportes` **THE SYSTEM SHALL** return the same tipo, nombre, especie and zona in the three reports.
6. **WHEN** `docs/pruebas.md` and the README section `Cómo ejecutar` are read **THE SYSTEM SHALL** document the command of every test level (`./mvnw test`, `./mvnw verify`, `k6 run`) for macOS/Git Bash (`./mvnw`) and PowerShell (`mvnw.cmd`), because the assignment requires every test type to run with a command documented in the README.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
./mvnw -q verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test='FlujoReportePerdidaSistemaIT,FlujoVozSistemaIT'
./mvnw -q clean verify
sh scripts/contiene.sh docs/pruebas.md 'mvnw test' 'mvnw verify' 'Unitarias' 'Integración' 'Sistema' 'Carga' 'jacoco'
sh scripts/contiene.sh README.md 'Cómo ejecutar' './mvnw spring-boot:run' 'mvnw.cmd' 'JDK 17' './mvnw test' './mvnw verify' 'k6 run'
```

**Checkpoint**

```bash
git add -A && git commit -m "E2-T5: Escribir las pruebas de sistema y la guía de pruebas"
git tag step-17-pruebas-sistema
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-17-pruebas-sistema`. Never invent the tag: copy the `checkpoint` field.

---

### `E2-T6` — Automatizar dos flujos de UI con Selenium

**Depends on:** `E3-T7`, `E3-T6` · **Priority:** p2 — metadata for scope cuts, not a running order · **Checkpoint:** `step-20-pruebas-ui`

Bonificación de pruebas de UI (+5). Agrega `org.seleniumhq.selenium:selenium-java` con `scope` test **sin versión** (la gestiona Spring Boot) y un perfil `ui` en el `pom.xml`: por defecto Failsafe excluye `**/*UIT.java`; con `-Pui` los incluye. `FlujosPrincipalesUIT` levanta la app con `@SpringBootTest(webEnvironment = RANDOM_PORT, properties = "ANTHROPIC_API_KEY=")` y Chrome `--headless=new` (Selenium Manager descarga el driver). Page Objects `PaginaInicio` y `PaginaNuevoReporte`, selectores `[data-prueba="…"]` del contrato de E3-T7 y esperas `WebDriverWait` de 10 s. Requiere Chrome instalado solo en la máquina que corre `-Pui`. Agrega el comando `./mvnw -q -Pui verify` a la sección `Cómo ejecutar` del README.

**Files**
- `pom.xml`
- `src/test/java/petfinder/ui/PaginaInicio.java`
- `src/test/java/petfinder/ui/PaginaNuevoReporte.java`
- `src/test/java/petfinder/ui/FlujosPrincipalesUIT.java`
- `README.md`

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** `./mvnw -q -Pui verify` runs with headless Chrome **THE SYSTEM SHALL** pass the flow that writes a report in `entrada-texto`, completes the contact, publishes with `boton-publicar` and finds the new case in `lista-casos`.
2. **WHEN** it runs the flow that opens a case, presses `boton-la-vi` and sends a sighting with `boton-enviar-pista` **THE SYSTEM SHALL** show the confirmation in `mensaje`.
3. **WHEN** the tests locate elements **THE SYSTEM SHALL** use only `data-prueba` selectors inside Page Objects and explicit `WebDriverWait`, and SHALL contain no `Thread.sleep`.
4. **WHEN** `./mvnw -q clean verify` runs without `-Pui` **THE SYSTEM SHALL** not execute any `*UIT` class, so machines without Chrome still pass.
5. **WHEN** the README section `Cómo ejecutar` is read **THE SYSTEM SHALL** document `./mvnw -q -Pui verify` as the command of the UI tests and say they need Chrome.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
./mvnw -q -Pui verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=FlujosPrincipalesUIT
sh scripts/contiene.sh src/test/java/petfinder/ui/PaginaInicio.java 'WebDriverWait' 'data-prueba' '!Thread.sleep'
sh scripts/contiene.sh src/test/java/petfinder/ui/FlujosPrincipalesUIT.java '!Thread.sleep' '!By.xpath'
sh scripts/contiene.sh README.md './mvnw -q -Pui verify' 'Chrome'
./mvnw -q clean verify
```

**Checkpoint**

```bash
git add -A && git commit -m "E2-T6: Automatizar dos flujos de UI con Selenium"
git tag step-20-pruebas-ui
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-20-pruebas-ui`. Never invent the tag: copy the `checkpoint` field.

---

## Epic acceptance

The epic is done when every task is `done` **and**:

1. **WHEN** `./mvnw -q clean verify` runs **THE SYSTEM SHALL** exit 0 with the Mockito, web-slice and black-box system tests passing together with the other epics' tests.
2. **WHEN** the same "Max, perro, Chía" report is created by form, by voice and by the assistant **THE SYSTEM SHALL** store three reports with identical tipo, nombre, especie and zona — the evidence that three adapters reach one use case.

```bash
./mvnw -q clean verify
./mvnw -q verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=FlujoVozSistemaIT
```

Run from the project root. Both criteria must be decidable by these commands.

## Pitfalls

- **Inyectar `ServicioReportes` en un controlador** — compila y funciona, pero rompe el argumento central de la exposición y ArchUnit lo rechaza. Inyecta siempre la interfaz del puerto.
- **`TestRestTemplate` sin `@AutoConfigureTestRestTemplate`** — en Spring Boot 4 ya no se configura solo con `@SpringBootTest`; el campo queda en null.
- **Afirmar `PF-001` en una prueba de sistema** — los datos de ejemplo y las otras pruebas ya usaron IDs. Usa el ID del POST.
- **Olvidar `ANTHROPIC_API_KEY=` en una `@SpringBootTest`** — si la persona tiene su clave en `.env`, la prueba llamaría a Claude: gasta créditos y deja de ser determinista.
- **`Thread.sleep` en Selenium** — la rúbrica lo califica como prueba frágil. Esperas explícitas con `WebDriverWait`.

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
