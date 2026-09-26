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
| Solo las pruebas de sistema | `./mvnw verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test='FlujoReportePerdidaSistemaIT,FlujoVozSistemaIT'` | `.\mvnw.cmd verify "-Dtest=NINGUNA" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dit.test=FlujoReportePerdidaSistemaIT,FlujoVozSistemaIT"` |
| Interfaz (`*UIT`, necesita Chrome) | `./mvnw -q -Pui verify` | `.\mvnw.cmd -q -Pui verify` |
| Carga (k6 instalado, app corriendo) | `k6 run perf/scripts/carga.js` | `k6 run perf/scripts/carga.js` |

**Cobertura:** `mvnw verify` deja el reporte de jacoco en `target/site/jacoco/index.html` (ábrelo en el navegador).

Antes de entregar cualquier cambio, el gate del equipo es `./mvnw -q clean verify` en verde. El `clean` importa: garantiza que Maven compile todo desde cero (ver "Hallazgos").

Ninguna prueba necesita internet ni una clave de Anthropic: toda `@SpringBootTest` fija `ANTHROPIC_API_KEY=` y el asistente cae al extractor por reglas.

## Resultados

Corrida completa del 26 de septiembre de 2026 (`./mvnw clean verify`, Windows 11, JDK 21):

| Nivel | Pruebas | Pasan |
|---|---|---|
| Unitarias + ArchUnit + slice web | 196 | 196 |
| Integración (H2) | 11 | 11 |
| Sistema (HTTP) | 10 | 10 |
| **Cobertura (jacoco)** | 79 % de instrucciones, 80 % de ramas | |

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

Las pruebas de integración y sistema nunca afirman un id fijo ni un tamaño absoluto de lista: la base no se revierte entre pruebas (el servidor atiende en otro hilo), así que cada prueba usa el id que devolvió su propio POST y compara tamaños antes y después.

## Hallazgos

Las pruebas de sistema encontraron dos defectos que las unitarias y el slice web no podían ver:

1. **La voz perdía las tildes de la zona.** Por formulario y por asistente, "Chía" se guarda como `Chía`; por voz se guardaba como `Chia`, porque el intérprete normaliza la frase para reconocerla y usaba esa versión normalizada como zona. Cada nivel inferior pasaba, porque cada uno probaba su canal por separado. Lo detecta la fila 20 de la matriz. *Estado: corregido en la épica 01 (PR #19): la zona se toma del texto original. La prueba pasa sin haberse relajado.*
2. **Rutas con `{id}` respondían 500 si las clases se compilaban sin `-parameters`.** Pasa cuando el IDE (por ejemplo VS Code) recompila `target/classes` por su cuenta. Se corrigió nombrando la variable (`@PathVariable("id")`) para no depender del compilador. Por eso el gate usa `clean`.

## Persistencia y carga

La sección detallada de persistencia (H2, datos de ejemplo y consultas) y la de carga las completa la épica 03. Mientras tanto, los resultados de carga están en [`perf/README.md`](../perf/README.md): con 50 usuarios el p95 fue 89 ms frente a un SLO de 500 ms, 0 % de errores y 79 req/s frente a un mínimo de 30.
