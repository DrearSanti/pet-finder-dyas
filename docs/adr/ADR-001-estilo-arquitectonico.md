# ADR-001: Estilo arquitectónico del Corte 2

| | |
|---|---|
| **Estado** | Aceptada |
| **Fecha de registro** | 2026-09-26, al cerrar el corte (la decisión se tomó al planear el Corte 2 y se aplicó en la tarea E1-T2) |
| **Decide** | Equipo Pet Finder (Santiago Escobar, Antonio Benítez, Mateo Ramírez) |
| **Reemplaza** | El diseño en capas implícito del Corte 1 (`ui` → `application` → `domain` → `infrastructure`) |

## Contexto

En el Corte 1, Pet Finder era un núcleo Java de consola: `Main` armaba todo a mano, el Factory Method creaba reportes, el Observer avisaba de avistamientos y un repositorio en memoria guardaba los casos. Tenía 11 pruebas en verde.

El Corte 2 asigna dos retos:

1. **Menú interactivo**: que la persona use Pet Finder desde una interfaz navegable (web, celular y la consola que ya existía).
2. **Captura de voz**: que la persona pueda registrar y consultar casos hablando.

Los dos retos piden lo mismo en el fondo: **sumar formas nuevas de entrar al sistema sin cambiar las reglas del negocio**. Además, para atenderlos bien aparecieron dos salidas nuevas: una base de datos (H2) para que la web tenga estado compartido, y un modelo de lenguaje (Claude) con respaldo por reglas para entender frases libres.

De ahí salen los criterios con los que se comparan los estilos:

| # | Criterio | Sale de |
|---|---|---|
| C1 | Agregar una entrada (web, voz, asistente) sin tocar el dominio ni los casos de uso | Los dos retos |
| C2 | Cambiar una tecnología de salida (memoria → H2, regex → Claude) sin tocar el dominio | Captura de voz (el extractor) y el menú web (estado compartido) |
| C3 | Probar las reglas sin levantar servidor, base ni red | Rúbrica de pruebas (unitarias, integración, sistema) |
| C4 | Que la regla del estilo se pueda **verificar** automáticamente, no solo declarar | Evaluación de la arquitectura |
| C5 | Reutilizar el Corte 1 (Factory Method, Observer, 11 pruebas) sin reescribirlo | Continuidad entre cortes |
| C6 | Que tres personas lo construyan en paralelo en tres días | Restricción del equipo |

## Opciones consideradas

Cada celda dice qué tan bien cumple el estilo el criterio: **Alta**, **Media** o **Baja**.

| Criterio | Capas | MVC | Hexagonal | Microservicios |
|---|---|---|---|---|
| C1 Entrada nueva sin tocar el negocio | Media: la entrada nueva va en la capa de presentación, pero nada impide que llame a la persistencia | Media: pensado para una sola interfaz web; la voz y la consola quedan como controladores con lógica propia | **Alta**: cada entrada es un adaptador que llama al mismo puerto de entrada | Alta, pero cada entrada sería otro servicio desplegado |
| C2 Salida intercambiable | Baja: la capa de negocio depende de la de datos, así que cambiar la base toca el negocio | Baja: el modelo suele ser la entidad de persistencia | **Alta**: el dominio define `RepositorioReportes` y `ExtractorDatosReporte`; los adaptadores los implementan | Alta dentro de cada servicio |
| C3 Probar sin infraestructura | Media | Media: el controlador y el modelo se prueban con Spring | **Alta**: dominio y servicios son Java puro; un doble del puerto basta | Baja: las pruebas de un flujo cruzan la red |
| C4 Regla verificable | Media: ArchUnit puede revisar capas | Baja: la separación es por convención | **Alta**: 5 reglas de ArchUnit sobre la dirección de dependencias | Baja: la frontera es de despliegue, no de código |
| C5 Reutilizar el Corte 1 | Alta: ya era casi así | Media: hay que repartir el núcleo entre modelo y controlador | **Alta**: el repositorio del Corte 1 ya era un puerto; solo se mueven paquetes | Baja: habría que partir el núcleo en servicios |
| C6 Tres personas, tres días | Alta | Alta | **Alta**: los puertos son el contrato; cada persona construye un adaptador | **Baja**: red, despliegue y datos distribuidos para un solo caso de uso |

**Microservicios se descarta** desde el principio: Pet Finder tiene un solo contexto de negocio (reportes y avistamientos) y un equipo de tres personas. Partirlo agregaría red, despliegues y consistencia entre bases sin atender mejor ningún reto.

## Decisión

**Arquitectura hexagonal (puertos y adaptadores) dentro de un monolito Spring Boot 4.1.1.**

- `domain/` y `application/` son Java puro, sin anotaciones de Spring.
- Los puertos de entrada (`GestionReportes`, `RegistroAvistamientos`, `ProcesadorComandosVoz`, `AsistenteReportes`) son lo único que conocen los adaptadores de entrada: consola, controladores REST, voz y asistente.
- Los puertos de salida (`RepositorioReportes`, `ExtractorDatosReporte`) son lo único que el núcleo pide de afuera. Tienen dos adaptadores cada uno: memoria / H2, y regex / Claude.
- `config/` es el único lugar que conoce las clases concretas y las conecta con `@Bean` (composition root).
- `ReglasArquitecturaTest` (ArchUnit) convierte la regla en una prueba que falla en cada `./mvnw test`.

## Consecuencias positivas

- **Modificabilidad (evidencia medida).** El extractor con Claude entró en la tarea E3-T6 sin tocar el dominio: `git diff step-14-h2-integrado step-15-extractor-claude --stat -- src/main/java/petfinder/domain` sale vacío. Lo mismo pasó con H2 (E3-T3) y con los cuatro controladores REST (E2-T4): se agregaron adaptadores, no se cambiaron reglas.
- **El mismo caso de uso por tres entradas (evidencia medida).** `FlujoVozSistemaIT.vozFormularioYAsistenteProducenElMismoReporte` publica "Max, perro, Chía" por formulario, voz y asistente, y los tres producen el mismo tipo, nombre, especie y zona.
- **Testabilidad.** 196 pruebas unitarias, de arquitectura y de capa web corren sin base ni red. La cobertura del paquete `domain` es de 96,8 % de instrucciones (JaCoCo, `./mvnw clean verify` del 2026-09-26).
- **La regla se cumple sola.** Las 5 reglas de ArchUnit están en verde; si alguien le pone `@Entity` a un reporte o un controlador importa un servicio, `./mvnw test` se pone rojo.
- **El Corte 1 sobrevive.** La demo de consola (`./mvnw -q compile exec:java`) sigue imprimiendo hasta `PASO 11`, y las 11 pruebas originales siguen pasando con sus aserciones.

## Consecuencias negativas

- **Más interfaces y más archivos.** Cada capacidad nueva necesita un puerto, un servicio y un adaptador. Para un cambio pequeño es más código que en capas.
- **`ConfiguracionPetFinder` crece.** El composition root tiene un `@Bean` por pieza y se partió en `ConfiguracionPetFinder`, `ConfiguracionAsistente` y `ConfiguracionIa`. Es el único lugar que hay que leer para saber qué implementación se usa, pero también el que más cambia.
- **Las entidades JPA duplican la forma del dominio.** `ReporteEntity` y `AvistamientoEntity` repiten los campos de `ReportePerdida`, `ReporteEncontrada` y `Avistamiento`, y `RepositorioReportesH2` los traduce en los dos sentidos. Por eso el dominio necesitó `reconstruir(...)` (E1-T3).
- **Aislar no es escalar.** El monolito con H2 en memoria no se puede replicar en varias instancias (cada una tendría su propia base). La prueba de carga lo muestra: con 50 usuarios la memoria pasó de 471 MB a 2.479 MB en un solo proceso (`perf/README.md`).

## Cuándo se revisaría

Si el sistema necesitara escalar partes por separado con equipos distintos, se evaluaría separar servicios. Hoy no hay ni la carga ni el equipo que lo justifiquen: el SLO se cumple con 79 req/s en un solo proceso.
