# ADR-002: Extracción de intención y datos en la captura de voz

| | |
|---|---|
| **Estado** | Aceptada |
| **Fecha de registro** | 2026-09-26, al cerrar el corte |
| **Decide** | Equipo Pet Finder |
| **Relacionada con** | [ADR-001](ADR-001-estilo-arquitectonico.md): la decisión vive detrás del puerto `ExtractorDatosReporte` |

## Contexto

El reto de **captura de voz** necesita convertir una frase dicha en voz alta ("perdí un perro llamado Max en Chía") en una acción y en los datos de un reporte. El navegador transcribe el audio (Web Speech, `es-CO`); el servidor solo recibe texto. Queda por decidir **quién entiende ese texto**.

Hay dos necesidades distintas:

1. **Comandos cortos** del reto (registrar, listar, consultar): frases con una forma conocida.
2. **La tarjeta viva del asistente**: frases libres ("se me escapó la Luna ayer por la tarde, es blanca con una mancha negra") de las que hay que sacar nombre, especie, color, zona o contacto, turno a turno.

Mínimos acordados por el equipo antes de probar cualquier opción: **85 %** de acierto en la intención y **90 %** en la confirmación de los datos, en español.

## Opciones consideradas

| Opción | Qué es | Resultado |
|---|---|---|
| **Laya** 0.3.20 (github.com/NandhaKishorM/laya) | Clasificador de intención local, sin costo por llamada | **Probado y descartado**: intención **60 %** (mínimo 85 %), confirmación **84 %** (mínimo 90 %), p95 de **812 ms** en CPU |
| **Jev** (TypeSafe) | Servicio de extracción estructurada | **Descartado sin probar**: los registros se cerraron el 22 de septiembre de 2026 y no extrae texto libre |
| NVIDIA | Modelos alojados | Listado como alternativa; no hay prueba registrada |
| **Solo regex** | Expresiones regulares sobre la frase normalizada | Suficiente para los comandos cortos; no ve datos libres como color o señas |
| **Claude + regex** | Claude extrae los datos del asistente; la regex responde si Claude no puede | **Elegida** |

### La prueba de Laya

Se corrió un guion con frases de reporte en español y se midió la intención reconocida, la confirmación de los datos extraídos y la latencia. **El guion no se guarda en el repositorio**: era una prueba de viabilidad, no parte del producto. Los resultados:

| Métrica | Mínimo acordado | Laya |
|---|---|---|
| Acierto de intención | 85 % | **60 %** |
| Acierto de confirmación | 90 % | **84 %** |
| Latencia p95 en CPU | — | **812 ms** |

Laya no llegó a ninguno de los dos mínimos. Además, sus 812 ms de p95 solo para clasificar ya superan por sí solos el SLO de 500 ms que se fijó para la voz (`perf/README.md`), antes de tocar la base de datos.

## Decisión

**Claude (Sonnet 5, por el SDK oficial de Java) para el asistente, con respaldo por regex; y solo regex para los comandos de voz.**

- **Comandos de voz** (`POST /api/voz`): `InterpreteComandoVoz` con cuatro expresiones regulares. Responde en microsegundos, no cuesta, funciona sin red y siempre da la misma respuesta para la misma frase, que es lo que necesita una prueba de carga.
- **Asistente** (`POST /api/asistente/turno`): `ServicioAsistente` recibe una lista ordenada de `ExtractorDatosReporte` y usa el primero que responda: primero `ExtractorClaude`, después `ExtractorRegex`.
- `ExtractorClaude` devuelve vacío ante cualquier fallo: sin `ANTHROPIC_API_KEY`, tope diario alcanzado (`PETFINDER_IA_TOPE_DIARIO`, 200 por defecto), red caída o respuesta ilegible. En todos esos casos responde la regex y la app sigue funcionando.
- El asistente **nunca publica**: no recibe `GestionReportes`. Publicar siempre es un `POST /api/reportes` que dispara la persona.

## Consecuencias positivas

- **La app funciona completa sin clave.** Las pruebas nunca llaman a Claude (toda `@SpringBootTest` fija `ANTHROPIC_API_KEY=`), y la carga de k6 se midió con el asistente en regex: p95 de 89,2 ms con 50 usuarios (`perf/README.md`).
- **Claude entró sin tocar el dominio.** `git diff step-14-h2-integrado step-15-extractor-claude --stat -- src/main/java/petfinder/domain` sale vacío: fue un adaptador más del puerto `ExtractorDatosReporte`.
- **El modelo no puede escribir en la base**, ni por error ni por una frase maliciosa, porque el asistente no tiene acceso al puerto que publica.
- **El costo tiene techo**: el tope diario limita las llamadas a Claude.

## Consecuencias negativas

- **Depende de un servicio externo y pagado** para la mejor experiencia del asistente; sin clave, el asistente entiende menos (solo lo que la regex reconoce).
- **Dos extractores que mantener**: cuando se agrega un campo al borrador, hay que enseñárselo a los dos.
- **La regex es rígida**: una frase fuera de sus cuatro formas es `NO_RECONOCIDO` (422 en la API). La página sugiere cómo decirla.

## Cuándo se revisaría

Si Jev abre acceso con un costo menor y buena precisión en español, o si Laya mejora hasta los mínimos de 85 % y 90 %. Cualquiera de los dos entraría como otro adaptador de `ExtractorDatosReporte`, sin tocar el servicio ni el dominio.
