# Documento de arquitectura: Pet Finder, Corte 2

Diseño y Arquitectura de Software · Universidad de La Sabana
Equipo: Santiago Escobar, Antonio Benítez, Mateo Ramírez

Cada número de este documento sale de un reporte del repositorio: Surefire y Failsafe (`target/surefire-reports/`, `target/failsafe-reports/`), JaCoCo (`target/site/jacoco/`) y k6 (`perf/resultados/`). Los de pruebas y cobertura son de `./mvnw clean -Pui verify` corrido el 2026-09-27 sobre `main` (commit `bd67f12`). Lo que no se midió o no se logró se dice en la sección 7.

## 1. Descripción del sistema y resumen de lo entregado en el primer corte

**Pet Finder** conecta a quien perdió una mascota con quien la vio o la encontró. Una familia publica la pérdida; un vecino ve los casos cercanos y reporta dónde vio al animal; el propietario recibe la pista.

**Lo entregado en el Corte 1** fue un núcleo Java de consola:

| Pieza | Qué hacía |
|---|---|
| Factory Method (`CreadorReporte`, `CreadorReportePerdida`, `CreadorReporteEncontrada`) | Validar y crear pérdidas y hallazgos sin que el servicio nombrara clases concretas |
| Observer (`PublicadorAvistamientos`, `AlertaPropietarioObserver`, `AuditoriaObserver`) | Avisar al propietario y auditar cada avistamiento |
| Estados (`EstadoReporte`) | Solo un caso ACTIVO puede pasar a RESUELTO o CERRADO |
| `RepositorioReportes` + `RepositorioReportesEnMemoria` | Guardar casos detrás de una interfaz |
| `MenuConsola`, `EscenarioDemostracion`, `Main` | Menú y demo de consola; `Main` armaba todo con `new` |
| Pruebas | 11 (`ServicioReportesTest` 5, `ServicioAvistamientosTest` 6), todas en verde |

**Lo que agrega el Corte 2**, sin cambiar las reglas del Corte 1: una API REST, una página web instalable (PWA) con mapa, captura de voz, un asistente con "tarjeta viva", persistencia en H2 y un extractor opcional con Claude. Todo sobre una arquitectura hexagonal en Spring Boot 4.1.1. La consola del Corte 1 sigue funcionando (`./mvnw -q compile exec:java` imprime la demo hasta `PASO 11`).

## 2. Retos asignados y tabla de trazabilidad

| Reto | Qué exige en términos del sistema | Atributos de calidad | Cómo lo atiende Pet Finder |
|---|---|---|---|
| **Menú interactivo** | Que las operaciones del Corte 1 (publicar, ver casos, reportar un avistamiento, resolver, cerrar) se puedan hacer desde una interfaz navegable en computador y celular, además de la consola, **sin duplicar las reglas de negocio** en cada interfaz | Usabilidad, modificabilidad | Página web con lista, mapa y detalle de cada caso (escritorio en tres columnas y celular como PWA) sobre una API REST. Web y consola son adaptadores del mismo puerto `GestionReportes` |
| **Captura de voz** | Que una frase dicha en voz alta termine en **el mismo caso de uso** que el formulario, con las mismas validaciones, y que responda rápido aunque muchas personas dicten a la vez. El servidor no debe procesar audio | Rendimiento, modificabilidad, disponibilidad | El navegador transcribe (Web Speech, `es-CO`) y el servidor interpreta solo el texto con reglas en `POST /api/voz`. El asistente con tarjeta viva deja completar por turnos lo que una sola frase no dice |

### Tabla de trazabilidad

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
| Los dos retos | Modificabilidad y testabilidad | Arquitectura hexagonal verificada con ArchUnit ([ADR-001](adr/ADR-001-estilo-arquitectonico.md)) | `test/.../arquitectura/ReglasArquitecturaTest.java` | `ReglasArquitecturaTest` y JaCoCo | 5 de 5 reglas en verde. Cobertura total: 83,1 % de instrucciones y 83,3 % de ramas; paquete `domain`: 97,0 % y 87,3 % |

## 3. Comparación de estilos y ADR de la decisión

Se compararon **Capas, MVC, Hexagonal y Microservicios** contra seis criterios que salen de los retos: agregar una entrada sin tocar el negocio, cambiar una tecnología de salida, probar sin infraestructura, poder verificar la regla, reutilizar el Corte 1 y construirlo entre tres personas en tres días.

| Estilo | Resumen del resultado |
|---|---|
| Capas | Reutiliza bien el Corte 1, pero el negocio depende de la capa de datos: cambiar memoria por H2 lo tocaría |
| MVC | Pensado para una sola interfaz web; la voz y la consola quedarían como controladores con lógica propia |
| **Hexagonal** | **Elegida.** Cada entrada es un adaptador del mismo puerto; cada salida, un adaptador intercambiable; ArchUnit verifica la regla |
| Microservicios | Descartada: un solo contexto de negocio y tres personas; agrega red y despliegues sin atender mejor ningún reto |

- La matriz completa, la decisión y sus consecuencias positivas y negativas están en **[ADR-001: estilo arquitectónico](adr/ADR-001-estilo-arquitectonico.md)**.
- La decisión sobre quién entiende las frases (Laya descartado con 60 % de intención; Claude con respaldo regex) está en **[ADR-002: extracción de intención](adr/ADR-002-extraccion-de-intencion.md)**.

## 4. Arquitectura inicial (Corte 1) y arquitectura evolucionada (Corte 2), con diagramas

Los diagramas están en **[`docs/diagramas/c4.md`](diagramas/c4.md)**: el del Corte 1 y los C4 del Corte 2 en tres niveles (contexto, contenedores y componentes). El diagrama de clases del Corte 1 sigue en [`docs/uml/diagrama-clases.mmd`](uml/diagrama-clases.mmd).

| | Corte 1 | Corte 2 |
|---|---|---|
| Estilo | Capas: `ui` → `application` → `domain` → `infrastructure` | Hexagonal: adaptadores → puertos → servicios → dominio |
| Entradas | Consola | Consola, API REST, página web / PWA, voz, asistente |
| Salidas | Repositorio en memoria | H2 (app) o memoria (pruebas unitarias) detrás de `RepositorioReportes`; Claude o regex detrás de `ExtractorDatosReporte` |
| Ensamblaje | `Main` con `new` | `config/` con `@Bean` (composition root); `Main` usa el mismo contexto de Spring |
| Framework | Ninguno | Spring Boot 4.1.1; dominio y servicios siguen siendo Java puro |
| Regla verificada | Por convención | 5 reglas de ArchUnit en cada `./mvnw test` |

**Cómo se hizo la evolución.** La tarea E1-T2 movió los paquetes con `git mv` (cada archivo conserva su historia del Corte 1) y convirtió los servicios en implementaciones de `GestionReportes` y `RegistroAvistamientos`. A partir de ahí, cada tecnología entró como un adaptador: H2 (E3-T3), la API REST (E2-T4), el asistente (E3-T4) y Claude (E3-T6). El dominio solo ganó lo que los retos exigían: reconstrucción desde la base, coordenadas y el borrador de la tarjeta viva.

## 5. Estrategia de pruebas: qué se prueba en cada nivel y por qué

La estrategia completa, con la matriz de clases de equivalencia y valores límite, está en **[`docs/pruebas.md`](pruebas.md)**. Resumen:

| Nivel | Qué prueba | Por qué así | Herramienta |
|---|---|---|---|
| Unitarias | Dominio (estados, creadores, ubicación, borrador), servicios, intérprete de voz, extractores y mapeo de DTO | El núcleo es Java puro: se prueba en milisegundos con un doble del puerto | JUnit 5, Mockito |
| Arquitectura | La dirección de las dependencias entre capas | Una arquitectura hexagonal se rompe con un solo `import`; ArchUnit lo vuelve una prueba | ArchUnit |
| Capa web | Cada controlador: códigos HTTP, JSON y errores | Levanta solo la web con los puertos simulados | `@WebMvcTest` |
| Integración | El adaptador H2 y un caso de uso completo sobre la base real | Un doble no detecta un mapeo JPA mal hecho | `@DataJpaTest`, `@SpringBootTest` |
| Sistema | La app completa por HTTP, sin dobles | Es la única prueba que demuestra que los tres canales producen el mismo reporte | `@SpringBootTest(RANDOM_PORT)` |
| Interfaz (opcional, con bonificación) | Dos flujos en Chrome | Probar lo que ve la persona | Selenium, con Page Objects y esperas explícitas |
| Carga | Latencia, errores y throughput en la hora pico de reportes por voz | El reto de voz se liga al rendimiento; el SLO se fijó antes de medir | k6 |

Técnicas usadas: clases de equivalencia (por ejemplo, nulo, vacío y solo espacios en cada dato obligatorio), valores límite (200 y 201 caracteres de una frase de voz, −90 y 90,0001 de latitud, `PF-999` → `PF-1000`) y tabla de decisión (las 9 transiciones de estado).

## 6. Resultados de las pruebas: reportes, cobertura y análisis de carga

### Pruebas automatizadas

`./mvnw clean -Pui verify` del 2026-09-27 sobre `main` (commit `bd67f12`):

| Nivel | Pruebas | Pasan | Reporte |
|---|---|---|---|
| Unitarias + arquitectura + capa web (`*Test`) | 227 | 227 | `target/surefire-reports/` |
| Integración con H2 (`DatosDeEjemploIT` 1, `RepositorioReportesH2IT` 7, `ServicioReportesH2IT` 3) | 11 | 11 | `target/failsafe-reports/` |
| Sistema por HTTP (`FlujoReportePerdidaSistemaIT` 6, `FlujoVozSistemaIT` 4, `FlujoHallazgoSistemaIT` 3) | 13 | 13 | `target/failsafe-reports/` |
| Interfaz con Selenium (`FlujosPrincipalesUIT`, perfil `-Pui`) | 2 | 2 | `target/failsafe-reports/` |
| **Total** | **253** | **253** | |

Las pruebas encontraron dos defectos que se corrigieron sin relajar ninguna aserción (detalle en `docs/pruebas.md`): la voz perdía las tildes de la zona ("Chía" llegaba como "Chia"; corregido en el PR #19) y las rutas con `{id}` respondían 500 si el IDE recompilaba sin `-parameters`.

### Cobertura (JaCoCo)

| Alcance | Instrucciones | Ramas |
|---|---|---|
| Todo el proyecto | 83,1 % | 83,3 % |
| Paquete `domain` | 97,0 % | 87,3 % |

Reporte: `target/site/jacoco/index.html`.

### Carga (k6)

SLO fijado antes de correr (commit de E3-T1): p95 ≤ 500 ms, errores < 1 %, al menos 30 req/s. Escenario: hora pico de reportes por voz; cada usuario registra una pérdida por voz y luego pide `ver reportes`. Detalle completo en **[`perf/README.md`](../perf/README.md)**.

| Prueba | Usuarios | p95 global | p95 `voz_registrar` | p95 `voz_listar` | req/s | Errores | ¿Cumple? |
|---|---|---|---|---|---|---|---|
| Baseline | 5 | 14,0 ms | 10,9 ms | 14,4 ms | 9,8 | 0 % (0 de 590) | Sí en latencia y errores (5 usuarios con 1 s de pausa no llegan a 30 req/s) |
| Carga | 50 | 89,2 ms | 15,7 ms | 106,1 ms | 79,2 | 0 % (0 de 21.442) | **Sí, los tres criterios** |

**Concurrencia:** al terminar, `SELECT COUNT(*) FROM REPORTES` dio 10.725 = 10.721 respuestas 201 + 4 casos de ejemplo. No se perdió ni se duplicó ningún reporte con 50 usuarios escribiendo a la vez, lo que es coherente con el generador de ID atómico de E1-T3.

**Cuello de botella:** `voz_listar`. Su p95 es unas 7 veces el de `voz_registrar` porque la lista **no está paginada** (con 10.725 casos una respuesta pesa 5,2 MB) y hace **consultas N+1** (una consulta por cada pérdida para cargar sus avistamientos: 4, 10 y 30 consultas con 4, 10 y 30 casos). El SLO se cumple porque H2 está en el mismo proceso y cada consulta cuesta microsegundos; con una base remota no se sostendría.

**Qué ofrece la arquitectura:** el arreglo (`JOIN FETCH` o un tope en la lista) vive solo en `RepositorioReportesH2`, detrás del puerto, sin tocar el dominio ni los servicios.

## 7. Límites conocidos del diseño y trabajo pendiente para el tercer corte

### Límites conocidos

| Límite | Por qué existe | Qué implica |
|---|---|---|
| **La voz depende del navegador y de internet** | Web Speech no existe en Firefox; en Safari necesita el dictado activado, y Chrome y Safari transcriben en servidores externos | Sin soporte, el micrófono se oculta y el campo de texto hace lo mismo |
| **H2 en memoria** | La rúbrica no pide durabilidad y H2 no necesita instalar nada | Los datos se pierden al reiniciar y la API no puede correr en varias instancias |
| **Sin paginación y con consultas N+1 en la lista** | Declarado fuera de alcance; es el cuello de botella que analiza la carga | Con muchos casos y una base remota, `ver reportes` rompería el SLO |
| **Sin cuentas ni permisos** | Fuera de alcance del corte | Cualquiera puede resolver o cerrar un caso |
| **La voz por reglas es rígida** | Se eligió regex por velocidad y costo (ADR-002) | Una frase fuera de las cuatro formas responde 422 con una sugerencia |
| **El asistente con Claude depende de una clave y tiene costo** | Servicio externo | Sin clave o sobre el tope diario responde la regex, que entiende menos |
| **"Cerca de ti" y las posibles coincidencias no tienen prueba automática** | Se agregaron al final del corte | Se verifican a mano; Selenium cubre solo publicar un reporte y reportar un avistamiento |
| **Las posibles coincidencias contradicen el plan del corte** | El plan dejó fuera las coincidencias pérdida ↔ hallazgo porque necesitan datos e imágenes que no existen | Es solo una sugerencia por nombre, especie y cercanía: puede proponer un caso equivocado, y decidir es de la persona |
| **Una sola corrida de carga**, con k6 y la app en la misma máquina | Tiempo del corte | Los números son indicativos, no un promedio de repeticiones |
| **Despliegue en el plan gratis de Render** | Sin costo para el curso. Está en https://pet-finder-akac.onrender.com (evidencia en `docs/evidencias/despliegue-render.md`) | Se duerme tras 15 minutos sin uso y la primera visita tarda cerca de un minuto; al despertar, H2 en memoria vuelve a los casos de ejemplo |

### Trabajo pendiente para el tercer corte

1. **Pipeline de CI/CD con DevSecOps**: `./mvnw clean verify` en cada Pull Request, escaneo de dependencias y de secretos.
2. **PostgreSQL** como segundo adaptador de `RepositorioReportes`, con migraciones (Flyway), para que los datos sobrevivan a un reinicio. El dominio no cambia.
3. **Paginación en `GET /api/reportes` y `JOIN FETCH`** de avistamientos, para quitar el cuello de botella medido.
4. **Cuentas y permisos** para que solo quien publicó pueda resolver o cerrar su caso.
5. **Fotos** de referencia y de avistamiento con almacenamiento externo, después de las cuentas.
6. Cubrir con Selenium **"Cerca de ti" y las posibles coincidencias**, o retirarlas si no se pueden probar.
