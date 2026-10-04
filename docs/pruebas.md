# Pruebas de Pet Finder

Esta guía explica qué se prueba en cada nivel, con qué herramienta y por qué, cómo se corre cada nivel y qué casos cubre. La idea que la ordena: **cada nivel responde una pregunta que el anterior no puede responder**. Una prueba unitaria dice si una regla está bien escrita; solo una prueba de sistema dice si formulario, voz y asistente llegan de verdad al mismo caso de uso.

## Estrategia por nivel

| Nivel | Qué prueba | Herramienta | Por qué así | Clases |
|---|---|---|---|---|
| **Unitarias** | Reglas del dominio (estados, creadores, ubicación, borrador), servicios de aplicación, intérprete de voz, extractores del asistente y mapeo de DTO | JUnit 5 + Mockito | Son milisegundos y no necesitan Spring: el dominio y la aplicación son Java puro, así que un doble del puerto basta para aislar cada caso de uso | `domain/*Test`, `application/**/*Test`, `adaptadores/ia/ExtractorClaudeTest`, `adaptadores/entrada/web/MapeoDtoTest` |
| **Arquitectura** | Que las capas no se salten la dirección de dependencias (el dominio no importa Spring, la web solo habla con puertos…) | ArchUnit | La arquitectura hexagonal es una promesa que se rompe con un solo `import`; ArchUnit la vuelve una prueba que falla en cada `mvnw test` | `arquitectura/ReglasArquitecturaTest` |
| **Slice web** | La traducción HTTP ↔ puerto de cada controlador: códigos, JSON y errores | `@WebMvcTest` + `@MockitoBean` | Levanta solo la capa web con los puertos simulados: prueba cada código de estado sin base de datos ni servicios reales | `adaptadores/entrada/web/ControladoresWebTest` |
| **Integración** | El adaptador de persistencia contra H2 real y un caso de uso completo sobre la base | `@DataJpaTest`, `@SpringBootTest` + `@Transactional` | Un doble no detecta un mapeo JPA mal hecho ni una consulta que ordena al revés; H2 embebida sí, y no necesita instalar nada | `integracion/persistencia/*IT` |
| **Sistema** | La aplicación completa por HTTP, como la usa el navegador: controlador → puerto → servicio → dominio → H2 | `@SpringBootTest(RANDOM_PORT)` + `RestClient` | Caja negra: sin mocks ni beans internos. Es la única prueba que demuestra que los tres canales de entrada producen el mismo reporte | `integracion/sistema/*IT` |
| **UI** | Dos flujos en Chrome: publicar un reporte con el asistente y reportar un avistamiento | Selenium + Page Objects, Chrome sin ventana | Es lo único que prueba que los ganchos `data-prueba`, el JavaScript y la API funcionan juntos. Los Page Objects concentran los selectores y cada espera es explícita (`WebDriverWait`), sin pausas fijas | `ui/PaginaInicio`, `ui/PaginaNuevoReporte`, `ui/FlujosPrincipalesUIT` |
| **Carga** | Latencia, errores y throughput con 50 usuarios en hora pico de reportes por voz | k6 | Ver [`perf/README.md`](../perf/README.md): SLO, escenario, resultados y cuello de botella | `perf/scripts/*.js` |

Cómo se separan los niveles en Maven: el sufijo del nombre decide quién corre cada clase. `*Test` lo corre Surefire en `mvnw test`; `*IT` lo corre Failsafe en `mvnw verify`; `*UIT` solo corre con el perfil `ui`. Así `mvnw test` sigue siendo rápido y nunca levanta un servidor.

## Comandos

Desde la raíz del proyecto. En macOS, Linux y **Git Bash** de Windows se usa `./mvnw`; en **PowerShell**, `.\mvnw.cmd` con los mismos argumentos.

| Para qué | macOS / Git Bash | PowerShell |
|---|---|---|
| Unitarias, ArchUnit y slice web (`*Test`) | `./mvnw test` | `.\mvnw.cmd test` |
| Una sola clase | `./mvnw test -Dtest=MapeoDtoTest` | `.\mvnw.cmd test "-Dtest=MapeoDtoTest"` |
| Todo: unitarias + integración + sistema (`*IT`) + cobertura | `./mvnw verify` | `.\mvnw.cmd verify` |
| Solo las pruebas de sistema | `./mvnw verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test='FlujoReportePerdidaSistemaIT,FlujoVozSistemaIT,FlujoHallazgoSistemaIT'` | `.\mvnw.cmd verify "-Dtest=NINGUNA" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dit.test=FlujoReportePerdidaSistemaIT,FlujoVozSistemaIT,FlujoHallazgoSistemaIT"` |
| Interfaz (`*UIT`, necesita Chrome) | `./mvnw -q -Pui verify` | `.\mvnw.cmd -q -Pui verify` |
| Carga (k6 instalado, app corriendo) | `k6 run perf/scripts/carga.js` | `k6 run perf/scripts/carga.js` |

**Cobertura:** `mvnw verify` deja el reporte de jacoco en `target/site/jacoco/index.html` (ábrelo en el navegador).

Antes de entregar cualquier cambio, el gate del equipo es `./mvnw -q clean verify` en verde. El `clean` importa: garantiza que Maven compile todo desde cero (ver "Hallazgos").

Ninguna prueba necesita internet ni una clave de Anthropic: toda `@SpringBootTest` fija `ANTHROPIC_API_KEY=` y el asistente cae al extractor por reglas.

## Resultados

Corrida completa del 3 de octubre de 2026 sobre `main` (`./mvnw clean verify` y `./mvnw -Pui verify`, Windows 11, JDK 17, Chrome sin ventana):

| Nivel | Pruebas | Pasan |
|---|---|---|
| Unitarias + ArchUnit + slice web | 227 | 227 |
| Integración (H2) | 11 | 11 |
| Sistema (HTTP) | 13 | 13 |
| UI (Selenium) | 2 | 2 |
| **Total** | **253** | **253** |

**Cobertura (JaCoCo)**, sobre unitarias, integración y sistema:

| Alcance | Instrucciones | Ramas |
|---|---|---|
| Todo el proyecto | 83,1 % | 83,3 % |
| Paquete `domain` | 97,0 % | 87,3 % |

Lo que baja el total es a propósito: el menú de consola del Corte 1 (`adaptadores.entrada.consola`, 0 %) es interactivo y no tiene pruebas automáticas, y `Main` solo arranca la demo. Las ramas sin cubrir de `config` son el caso "hay clave de Anthropic", porque ninguna prueba usa una clave real.

Entre la primera corrida (26 de septiembre: 205 unitarias, 10 de sistema, 79 % / 80 %) y esta entraron las pruebas de las funciones agregadas al final: cerrar un caso con "Ya apareció", "La tengo yo" y las posibles coincidencias (`FlujoHallazgoSistemaIT`).

### Registro de evidencias

| Fecha | Persona | Nivel | Comando | Resultado | Archivo |
|---|---|---|---|---|---|
| 2026-09-24 | Santi | Consola del Corte 1 y pruebas | `./mvnw -q compile exec:java`, `./mvnw test` | Pasaron | `docs/evidencias/2026-09-24_santi_e1t1-*.png` |
| 2026-09-25 | Santi | Unitarias (dominio y puertos) | `./mvnw test` | Pasaron | `docs/evidencias/2026-09-25_santi_e1t4-pruebas.png` |
| 2026-09-25 | Mateo | Carga (k6) | `k6 run perf/scripts/baseline.js` y `carga.js` | Cumple el SLO | `perf/resultados/` |
| 2026-09-28 | Equipo | Despliegue | Render | En línea | `docs/evidencias/despliegue-render.md` |
| 2026-10-03 | Santi | Unitarias + ArchUnit + slice web | `./mvnw clean verify` | 227 de 227 | `docs/evidencias/2026-10-03_santi_pruebas-unitarias.txt` |
| 2026-10-03 | Santi | Integración y sistema | `./mvnw clean verify` | 24 de 24 | `docs/evidencias/2026-10-03_santi_pruebas-integracion-y-sistema.txt` |
| 2026-10-03 | Santi | Cobertura | `./mvnw clean verify` | 83,1 % / 83,3 %; `domain` 97,0 % | `docs/evidencias/2026-10-03_santi_cobertura.png` |
| 2026-10-03 | Santi | UI (Selenium) | `./mvnw -Pui verify -Dit.test=FlujosPrincipalesUIT` | 2 de 2 | `docs/evidencias/2026-10-03_santi_pruebas-ui.txt` |

## Matriz de casos

Cada fila es un caso con la clase de equivalencia o el valor límite que representa. Los valores límite se prueban a ambos lados del borde.

| # | Nivel | Caso | Clase de equivalencia / valor límite | Esperado | Prueba |
|---|---|---|---|---|---|
| 1 | Unitaria | Latitud y longitud en los extremos | Límite válido: −90, 90, −180, 180 | Se acepta | `UbicacionTest.limitesValidos` |
| 2 | Unitaria | Un paso fuera del rango | Límite inválido: −90,0001, 90,0001, −180,0001, 180,0001 | `DatosInvalidosException` | `UbicacionTest.fueraDeRango` |
| 3 | Unitaria | Latitud sin longitud | Inválida: coordenada incompleta | `DatosInvalidosException` | `UbicacionTest.coordenadaIncompleta` |
| 4 | Unitaria | Transiciones de estado | Válidas: ACTIVO → RESUELTO / CERRADO; inválidas: desde RESUELTO o CERRADO | Solo ACTIVO cambia | `EstadoReporteTest` |
| 5 | Unitaria | Frase de voz de 200 y de 201 caracteres | Límite de `LONGITUD_MAXIMA` | 200 se reconoce, 201 no | `InterpreteComandoVozTest` |
| 6 | Unitaria | Celular con 9, 10 y 11 dígitos | Límite: solo 10 dígitos que empiezan por 3 | Solo el de 10 es contacto | `ExtractorRegexTest` |
| 7 | Unitaria | Enmascarado del contacto | Teléfono de 6 dígitos (mínimo), 10 dígitos, con espacios; correo; texto de 5 y de 6 caracteres | `601•34`, `300•••••67`, `so•••@correo.co`, `•••`, `Ca•••` | `MapeoDtoTest.enmascarado` |
| 8 | Unitaria | Solicitud sin tipo | Inválida: `tipo` nulo | 400 con mensaje para la persona | `MapeoDtoTest.solicitudSinTipoSeRechaza` |
| 9 | Unitaria | Avistamiento sin contacto | Válida: contacto opcional (nulo o en blanco) | Contacto `null`, id y hora del servidor | `MapeoDtoTest.avistamientoSinContacto` |
| 10 | Slice web | Cada excepción del dominio | `DatosInvalidos`, `ReporteNoEncontrado`, `OperacionNoPermitida`, JSON ilegible, voz no reconocida | 400, 404, 409, 400, 422 con `{"error": …}` | `ControladoresWebTest` |
| 11 | Sistema | Ciclo completo de una pérdida | Camino feliz + avistamiento sobre caso resuelto | 201 → ACTIVO → 201 → 204 → RESUELTO → 409 | `FlujoReportePerdidaSistemaIT.cicloCompletoDeUnaPerdida` |
| 12 | Sistema | Reporte sin zona | Inválida: campo obligatorio ausente | 400 y la lista no crece | `FlujoReportePerdidaSistemaIT.sinZonaResponde400` |
| 13 | Sistema | Cuerpo que no es JSON | Inválida: formato | 400 `El cuerpo de la petición no es válido` | `FlujoReportePerdidaSistemaIT.jsonIlegibleResponde400` |
| 14 | Sistema | Id que no existe | Inválida: `PF-999999` | 404 | `FlujoReportePerdidaSistemaIT.idInexistenteResponde404` |
| 15 | Sistema | Resolver dos veces | Inválida: transición repetida | 204 y luego 409 | `FlujoReportePerdidaSistemaIT.resolverDosVecesResponde409` |
| 16 | Sistema | Privacidad en lista y detalle | Lista pública vs. detalle del caso | Lista: `300•••••67` y coordenadas a 3 decimales; detalle: contacto completo | `FlujoReportePerdidaSistemaIT.listaEnmascaraYDetalleNo` |
| 17 | Sistema | Pérdida dictada por voz | Válida: frase reconocida | 201 y el id aparece en `GET /api/reportes` | `FlujoVozSistemaIT.vozCreaUnReporteVisibleEnLaLista` |
| 18 | Sistema | Frase ininteligible | Inválida: no es un comando | 422 con ayuda y la lista no cambia de tamaño | `FlujoVozSistemaIT.fraseIninteligibleResponde422` |
| 19 | Sistema | Listar por voz | Válida: comando de lectura | 200 y la lista no cambia | `FlujoVozSistemaIT.listarPorVozResponde200` |
| 20 | Sistema | "Max, perro, Chía" por formulario, voz y asistente | Tres canales de entrada, un caso de uso | Mismo tipo, nombre, especie y zona | `FlujoVozSistemaIT.vozFormularioYAsistenteProducenElMismoReporte` |
| 21 | Sistema | "La tengo yo" sobre un caso perdido | Válida: hallazgo con contacto asociado al caso | El caso muestra quién la tiene y su contacto; la familia lo cierra al recogerla | `FlujoHallazgoSistemaIT.hallazgoAsociadoAlCaso` |
| 22 | Sistema | "La tengo yo" sin contacto | Inválida: contacto obligatorio en un hallazgo | 400 con el mensaje del dominio y el caso no cambia | `FlujoHallazgoSistemaIT.hallazgoSinContactoResponde400` |
| 23 | Sistema | Pista "La vi" | Válida: pista sin contacto público | No muestra el contacto de quien la dejó ni cuenta como hallazgo | `FlujoHallazgoSistemaIT.pistaNoMuestraContacto` |
| 24 | Integración | Buscar en H2 un id que no existe | Inválida: id inexistente | `Optional` vacío, sin excepción | `RepositorioReportesH2IT.idInexistente` |
| 25 | Integración | Listar activos | Partición por estado: ACTIVO frente a RESUELTO y CERRADO | Solo aparecen los activos | `RepositorioReportesH2IT.listarActivosFiltraPorEstado` |

Las pruebas de integración y sistema nunca afirman un id fijo ni un tamaño absoluto de lista: la base no se revierte entre pruebas (el servidor atiende en otro hilo), así que cada prueba usa el id que devolvió su propio POST y compara tamaños antes y después.

## Hallazgos

Las pruebas de sistema encontraron dos defectos que las unitarias y el slice web no podían ver:

1. **La voz perdía las tildes de la zona.** Por formulario y por asistente, "Chía" se guarda como `Chía`; por voz se guardaba como `Chia`, porque el intérprete normaliza la frase para reconocerla y usaba esa versión normalizada como zona. Cada nivel inferior pasaba, porque cada uno probaba su canal por separado. Lo detecta la fila 20 de la matriz. *Estado: corregido en la épica 01 (PR #19): la zona se toma del texto original. La prueba pasa sin haberse relajado.*
2. **Rutas con `{id}` respondían 500 si las clases se compilaban sin `-parameters`.** Pasa cuando el IDE (por ejemplo VS Code) recompila `target/classes` por su cuenta. Se corrigió nombrando la variable (`@PathVariable("id")`) para no depender del compilador. Por eso el gate usa `clean`.

## Persistencia y carga

### Persistencia (H2)

- **Qué es:** `RepositorioReportesH2` es el adaptador de salida que implementa el puerto `RepositorioReportes` con Spring Data JPA (`ReporteJpaRepository`, `ReporteEntity`, `AvistamientoEntity`). Las entidades JPA viven solo en `adaptadores/salida/persistencia/h2`; el dominio no sabe que existen, y lo vigila ArchUnit con la regla `soloLaPersistenciaUsaJpa`.
- **La base:** H2 en memoria (`jdbc:h2:mem:petfinder`). Con la app corriendo se consulta en `http://localhost:8080/h2-console`, usuario `sa`, sin clave.
- **Datos de ejemplo:** al arrancar, `DatosDeEjemplo` carga 4 casos para que la demo no empiece vacía. Se apaga con `petfinder.datos-ejemplo=false`. Lo comprueba `DatosDeEjemploIT`.
- **Qué prueban las 11 de integración:**
  - `RepositorioReportesH2IT` (7): ida y vuelta de pérdidas y hallazgos (mascota, contacto, coordenadas, descripción), id inexistente, filtro de activos por estado, actualización de estado, avistamientos guardados con su reporte y fecha de creación conservada al reconstruir.
  - `ServicioReportesH2IT` (3): la aplicación usa de verdad el adaptador H2, y registrar, consultar y resolver pasan por la base.
  - `DatosDeEjemploIT` (1): los casos de ejemplo se cargan al arrancar.
- **Límite declarado:** los datos se pierden al reiniciar, y la API no se puede escalar a varias instancias porque cada una tendría su propia base en memoria. Cambiar a PostgreSQL es otro adaptador del mismo puerto, sin tocar dominio ni servicios.

### Carga (k6)

El detalle completo (SLO definido antes de correr, escenario, entorno y análisis) está en [`perf/README.md`](../perf/README.md). Resumen:

| Prueba | Usuarios | p95 | Errores | req/s | ¿Cumple el SLO? |
|---|---|---|---|---|---|
| Baseline | 5 durante 1 min | 14,0 ms | 0 % (0 de 590) | 9,8 | Sí en latencia y errores |
| Carga | hasta 50 durante 4 min 30 s | **89,2 ms** (SLO 500 ms) | **0 %** (0 de 21.442) | **79,2** (mínimo 30) | **Sí, los tres criterios** |

- **Concurrencia:** después de la carga, `SELECT COUNT(*) FROM REPORTES` dio 10.725 = 10.721 respuestas 201 + 4 casos de ejemplo. No se perdió ni se duplicó ningún reporte.
- **Cuello de botella:** `ver reportes` (p95 de 106 ms frente a 15,7 ms de registrar). No está paginado y hace N+1 consultas: una por cada pérdida para traer sus avistamientos. La corrección (`JOIN FETCH` o un tope) vive solo en el adaptador de persistencia, gracias al puerto.
- **Evidencia:** `perf/resultados/baseline.json`, `carga.json`, `concurrencia.json`, `consultas-n-mas-1.json` y las imágenes `resumen-k6-*.png`.
