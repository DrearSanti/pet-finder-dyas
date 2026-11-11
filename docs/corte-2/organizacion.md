# Corte 2: cómo nos organizamos para construirlo

> Esta sección explica cómo trabajó el equipo. La documentación técnica del Corte 2 (ADR, diagramas C4, pruebas y trazabilidad) está en la [sección de arquitectura y trazabilidad del README](../../README.md#arquitectura-y-trazabilidad-del-corte-2).

El Corte 2 convierte el módulo de consola en una aplicación web con arquitectura hexagonal, captura de voz, asistente con tarjeta viva, mapa y PWA. Para que los tres trabajemos en paralelo sin pisarnos, todo el trabajo está descrito en un **plan de construcción** (blueprint) dentro de [`blueprints/pet-finder-corte-2/`](../../blueprints/pet-finder-corte-2/). El plan no es código: dice qué se construye, en qué orden, quién lo hace y cómo se comprueba que quedó bien.

### Qué hay en el plan

```
blueprints/pet-finder-corte-2/
├── blueprint.md   Documento maestro: alcance, arquitectura, datos, API, pruebas, despliegue y decisiones
├── tasks.json     Lista de las 21 tareas con sus dependencias y su estado (pendiente, en curso, terminada)
├── epics/         Un manual por integrante con sus tareas en detalle
└── workspace/     Reglas y herramientas del repositorio (se copiaron una vez a la raíz)
```

| Pieza | Para qué sirve |
|---|---|
| `blueprint.md` | Responde qué construimos, con qué tecnología y por qué. Tiene 20 secciones fijas; la 9 es el orden de construcción y la 20 guarda las decisiones con su justificación |
| `tasks.json` | Es el tablero del proyecto en un archivo: cada tarea dice de qué otras depende, qué criterios debe cumplir, con qué comandos se verifica y en qué estado está |
| `epics/` | Cada integrante tiene su archivo con el contexto compartido (contratos de los puertos, formato JSON de la API) y sus tareas: archivos que toca, criterios de aceptación y comandos de verificación |
| `workspace/` | Instrucciones para el entorno de desarrollo (`CLAUDE.md`), reglas por capa, permisos, Maven Wrapper y los scripts de verificación (`scripts/*.sh`) |

### Reparto

| Épica | Responsable | Tareas | Qué entrega |
|---|---|---|---|
| 01 Núcleo hexagonal, dominio y voz | Santiago Escobar | 6 | Migración a Spring Boot, puertos, reglas de ArchUnit, dominio con coordenadas y borrador, intérprete de voz, ADR y diagramas C4 |
| 02 Adaptador web y pruebas | Antonio Benítez | 6 | Cliente JavaScript con captura de voz, API REST, pruebas con Mockito, pruebas de sistema y pruebas de interfaz |
| 03 Persistencia, asistente e interfaz | Mateo Ramírez | 9 | Base de datos H2, asistente con tarjeta viva, interfaz con mapa, PWA, Docker y pruebas de carga con k6 |

Las tareas dependen unas de otras entre épicas, así que el orden importa: la base en Spring Boot (tarea E1-T1) y la reorganización hexagonal (E1-T2) desbloquean casi todo lo demás, y la documentación final (E1-T6) espera a las pruebas de sistema, la PWA y la carga.

### Flujo de trabajo

1. `git pull` de `main` y una rama por tarea (`tarea/E3-T1`).
2. Se construye la tarea siguiendo su archivo en `epics/`.
3. Se corren sus comandos de verificación; la tarea solo se marca terminada en `tasks.json` si todos pasan.
4. El integrante responsable hace el commit (con el id de la tarea, por ejemplo `E3-T1: …`), crea la etiqueta de la tarea (`step-03-slo-y-k6`) y abre el Pull Request, que entra a `main` con *merge commit*.

Cada tarea queda así con un commit y una etiqueta propios, de modo que el historial de Git muestra quién hizo qué y permite volver a cualquier paso.

La explicación completa (puesta a punto en macOS y Windows, cómo leer `tasks.json` y el día a día) está en la [wiki del proyecto](https://github.com/DrearSanti/pet-finder-dyas/wiki).
