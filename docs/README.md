# Documentación

Índice de todo lo que hay en `docs/`. Para correr el proyecto, empieza por el [README](../README.md).

## Vigente

| Documento | Qué contiene |
|---|---|
| [`arquitectura.md`](arquitectura.md) | La arquitectura hexagonal: retos, estilo, componentes, decisiones y límites conocidos |
| [`adr/`](adr/) | Decisiones de arquitectura: [ADR-001](adr/ADR-001-estilo-arquitectonico.md) (por qué hexagonal) y [ADR-002](adr/ADR-002-extraccion-de-intencion.md) (Claude con respaldo de reglas) |
| [`diagramas/c4.md`](diagramas/c4.md) | Diagramas C4 de contexto, contenedores y componentes |
| [`pruebas.md`](pruebas.md) | Estrategia de pruebas por nivel, matriz de casos y hallazgos |
| [`diseno/`](diseno/DESIGN.md) | Sistema de diseño de la interfaz: [`DESIGN.md`](diseno/DESIGN.md), [`vistas.html`](diseno/vistas.html) y el [logo](diseno/logo/) |
| [`evidencias/`](evidencias/) | Capturas y reportes de las corridas de pruebas, y el [registro del despliegue en Render](evidencias/despliegue-render.md) |
| [`plan-corte-3.md`](plan-corte-3.md) | Reparto, decisiones y estado de las tareas del Corte 3 |
| `tareas-corte-3/` | La guía de tareas de cada integrante, con los parches ya probados (entra con el PR del plan) |

La carga (SLO, escenario y resultados de k6) está fuera de esta carpeta, en [`perf/README.md`](../perf/README.md).

## En construcción (Corte 3)

Se agregan a medida que entran las tareas del [plan](plan-corte-3.md):

| Documento | Qué va a contener | Tarea |
|---|---|---|
| `devops.md` | El pipeline, qué hace cada etapa, qué umbral la detiene y cómo se revierte una versión | A12 |
| `seguridad.md` | Modelo de amenazas, controles, umbrales y registro de hallazgos | S11 |
| `evolucion.md` | El recorrido de los tres cortes y cómo se resolvió cada pendiente | S14 |
| `implementacion.md` | La guía para que una organización instale y opere Pet Finder por su cuenta | A13 |
| `adr/ADR-003` y `adr/ADR-004` | Estrategia de seguridad del pipeline y estrategia de despliegue | S13, M12 |

## Historia

| Carpeta | Qué contiene |
|---|---|
| [`corte-1/`](corte-1/README.md) | El problema, SOLID, patrones, UML y la versión de consola del Corte 1, con el [cómic](corte-1/comic/pet-finder-comic.pdf) y la [guía técnica](corte-1/guia-tecnica.md) de entonces |
| [`corte-2/`](corte-2/organizacion.md) | Cómo nos organizamos para construir el Corte 2 con un plan por tareas |
