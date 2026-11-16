# Documentación

Índice de todo lo que hay en `docs/`. Para correr el proyecto, empieza por el [README](../README.md).

## Vigente

| Documento | Qué contiene |
|---|---|
| [`arquitectura.md`](arquitectura.md) | La arquitectura hexagonal: retos, estilo, componentes, decisiones y límites conocidos |
| [`adr/`](adr/) | Decisiones de arquitectura: [ADR-001](adr/ADR-001-estilo-arquitectonico.md) (por qué hexagonal), [ADR-002](adr/ADR-002-extraccion-de-intencion.md) (Claude con respaldo de reglas), [ADR-003](adr/ADR-003-seguridad-del-pipeline.md) (seguridad del pipeline: herramientas y umbrales) y [ADR-004](adr/ADR-004-estrategia-de-despliegue.md) (estrategia de despliegue) |
| [`diagramas/c4.md`](diagramas/c4.md) | Diagramas C4 de contexto, contenedores y componentes |
| [`pruebas.md`](pruebas.md) | Estrategia de pruebas por nivel, matriz de casos y hallazgos |
| [`seguridad.md`](seguridad.md) | Modelo de amenazas con STRIDE, controles del pipeline con su umbral, registro de hallazgos con su decisión, riesgos aceptados y el PR bloqueado de evidencia |
| [`evolucion.md`](evolucion.md) | El recorrido de los tres cortes, qué decisiones se mantuvieron y cuáles cambiaron, cómo se resolvió cada pendiente y los límites del sistema final |
| [`devops.md`](devops.md) | El pipeline de CI/CD: diagrama, qué hace cada etapa, qué umbral la detiene y por qué, las pruebas en rojo de cada control y cómo se revierte una versión |
| [`diseno/`](diseno/DESIGN.md) | Sistema de diseño de la interfaz: [`DESIGN.md`](diseno/DESIGN.md), [`vistas.html`](diseno/vistas.html) y el [logo](diseno/logo/) |
| [`evidencias/`](evidencias/) | Capturas y reportes de las corridas de pruebas, y el [registro del despliegue en Render](evidencias/despliegue-render.md) |
| [`implementacion.md`](implementacion.md) | Guía de implementación: requisitos, costos, configuración, instalación paso a paso en Render, verificación, operación (versiones, reversión, copias de seguridad) y solución de problemas |
| [`plan-corte-3.md`](plan-corte-3.md) | Reparto, decisiones y estado de las tareas del Corte 3 |
| `tareas-corte-3/` | La guía de tareas de cada integrante, con los parches ya probados (entra con el PR del plan) |

La carga (SLO, escenario y resultados de k6) está fuera de esta carpeta, en [`perf/README.md`](../perf/README.md).

## Historia

| Carpeta | Qué contiene |
|---|---|
| [`corte-1/`](corte-1/README.md) | El problema, SOLID, patrones, UML y la versión de consola del Corte 1, con el [cómic](corte-1/comic/pet-finder-comic.pdf) y la [guía técnica](corte-1/guia-tecnica.md) de entonces |
| [`corte-2/`](corte-2/organizacion.md) | Cómo nos organizamos para construir el Corte 2 con un plan por tareas |
