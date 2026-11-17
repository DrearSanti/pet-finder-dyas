# Cambios de Pet Finder

Lo que cambió en cada versión, de la más nueva a la más vieja. Las versiones siguen el [versionado semántico](https://semver.org/lang/es/): el primer número cambia con cada corte del curso, el segundo cuando se agregan funciones sin romper nada y el tercero con correcciones. Cada versión tiene su tag `vX.Y.Z` en Git y su página en [GitHub Releases](https://github.com/DrearSanti/pet-finder-dyas/releases).

Desde el Corte 3, cada versión publicada por el pipeline queda como imagen en `ghcr.io/drearsanti/pet-finder` con la etiqueta de la versión, además de la del SHA de su commit. Las versiones 1.0.0 y 2.0.0 son anteriores al pipeline y no tienen imagen.

**Regla del equipo:** cada pull request que cambie algo visible para quien usa, instala u opera el sistema agrega su línea en **Sin publicar**. Al crear un tag, esa sección pasa a ser la de la versión nueva.

## Sin publicar

Nada todavía.

## [3.0.0] - 2026-11-16

Corte 3: entrega continua y segura. Cada cambio pasa por un pipeline que verifica la arquitectura, la seguridad y el rendimiento, y lo que pasa llega solo a producción.

El tag intermedio `v2.1.0` (mismo día, commit `a2b8887`) marca el primer despliegue continuo en verde y sirvió para probar la reversión; su contenido está incluido en esta versión.

### Agregado

- **Integración continua** (`ci.yml`): construcción y, en paralelo, unitarias con umbral de cobertura del dominio, reglas de arquitectura, integración e interfaz con Selenium, en cada push y cada pull request. `main` protegida: sin push directo, con aprobación de otro integrante y checks obligatorios (A1, A2).
- **Despliegue continuo** (`cd.yml`): cada merge a `main` publica la imagen en GHCR con el SHA del commit (y con la versión en un tag), la despliega en Render con un deploy hook, espera a que `/actuator/info` muestre ese SHA y corre la prueba de humo y la carga corta (M5, M6, M7). Decisión en [ADR-004](docs/adr/ADR-004-estrategia-de-despliegue.md).
- **Reversión** (`revertir.yml`): vuelve a desplegar una versión ya publicada por su tag o su SHA, sin reconstruir, y la verifica igual que el CD (M8).
- **Verificación en cada PR**: `imagen-y-escaneo` construye y escanea la imagen, y `despliegue-y-verificacion` la levanta con PostgreSQL y corre la prueba de humo y la carga corta a 30 req/s (A6). Prueba de humo del flujo principal en `scripts/humo-flujo.sh` (A4).
- **Análisis de dependencias** con Trivy y Dependabot, sin consultas a Maven Central en el pipeline (A5), y check `acciones-fijadas`, que exige que toda acción de GitHub esté fijada por su SHA completo (A8).
- **Código de gestión** en el núcleo para resolver o cerrar casos, con código de administrador opcional (S9), y **límite de peticiones** por IP y global para el asistente, la voz y los reportes (S10).
- Documentación del corte: modelo de amenazas y hallazgos en [`docs/seguridad.md`](docs/seguridad.md) y [ADR-003](docs/adr/ADR-003-seguridad-del-pipeline.md), el pipeline en [`docs/devops.md`](docs/devops.md), la [guía de implementación](docs/implementacion.md), la [evolución desde el Corte 2](docs/evolucion.md) y el [guion de la presentación](docs/presentacion-corte-3.md).
- Persistencia real en **PostgreSQL** con migraciones de **Flyway**; Hibernate solo valida el esquema. Los IDs de los reportes siguen la numeración después de un reinicio (S7).
- **Control de secretos con Gitleaks** sobre todo el historial de Git, en el workflow `Seguridad`, con corrida diaria programada (S5).
- **Análisis estático con CodeQL** para Java y JavaScript, con un paso propio que detiene el pipeline ante hallazgos de severidad 7.0 o más sin tratar (S6).
- **Prueba de carga corta** `perf/scripts/corta.js`: el mismo SLO de `carga.js` en un minuto y medio, a tasa fija, cerrando los casos que crea. Workflow manual `Carga larga` que corre `carga.js` contra Compose con PostgreSQL (M4).
- **Cabeceras de seguridad** en todas las respuestas: CSP sin `unsafe-inline`, `nosniff`, HSTS, `Permissions-Policy` y `Referrer-Policy`. Leaflet se carga con verificación de integridad (SRI) y el registro del service worker pasó a `js/registro-sw.js` (M9).
- **Contenedores:** Dockerfile de dos etapas con usuario sin privilegios y healthcheck, y `docker-compose.yml` con la aplicación y PostgreSQL (27 de octubre).
- Plan del corte, reparto de tareas y la guía de cada integrante en `docs/` (plan del Corte 3).

### Cambiado

- La **clave de CARTO** del mapa se lee al arrancar desde `PETFINDER_CLAVE_CARTO` y ya no queda dentro de la imagen (A7).
- La **carga corta del CD** contra Render corre a 6 peticiones por segundo durante 330 s, con los mismos umbrales de latencia y errores; la capacidad de 30 req/s la exige `despliegue-y-verificacion` en cada PR. Render se queda en el plan gratis (revisión de ADR-004, M7).
- La lista de casos activos sale en **una sola consulta**, con un tope de 200 casos, y `ver reportes` por voz devuelve los 10 más recientes (S8).
- Las **reglas de arquitectura** de ArchUnit son más estrictas: la consola también entra solo por los puertos, y cada adaptador de salida queda aislado de los demás (S4).
- El **repositorio quedó ordenado**: el README empieza por "Empieza aquí" y el mapa de la rúbrica, y el material del Corte 1 pasó a `docs/corte-1/` (M3).

### Corregido

- El **CD y Revertir ya no despliegan al mismo tiempo**: el job `desplegar` de `cd.yml` entró al grupo de concurrencia `despliegue-produccion`, el mismo de `revertir.yml`. Lo encontró la primera prueba real de reversión, en la que un merge desplegó en medio de su carga (M8).

### Seguridad

- **Denegación de servicio del asistente cerrada:** una frase tiene un tope de 1000 caracteres, las expresiones regulares de correo y zona quedaron acotadas (100 KB de texto ya no traban un núcleo 17 segundos), cada campo tiene un tope que responde 400 en vez de 500, y un cuerpo de más de 64 KB responde 413 (M1).
- La **consola de H2** quedó apagada; solo se prende con el perfil `local` en el computador de quien desarrolla (M2).
- Actualización de Tomcat y Jackson por las vulnerabilidades que encontró Trivy (27 de octubre).

## [2.0.0] - 2026-10-03

Corte 2: la aplicación web con arquitectura hexagonal.

### Agregado

- **Arquitectura hexagonal** en Spring Boot 4.1.1 sobre Java 17: dominio puro, puertos de entrada y salida, y adaptadores intercambiables, verificada con 5 reglas de ArchUnit ([ADR-001](docs/adr/ADR-001-estilo-arquitectonico.md)).
- **API REST** para reportes, avistamientos, comandos de voz y el asistente, con un solo formato de error `{"error": "..."}`.
- **Página web instalable (PWA)** con mapa de Leaflet, funcionamiento sin conexión para la interfaz y diseño propio.
- **Captura de voz** con Web Speech e intérprete de comandos por reglas.
- **Asistente con tarjeta viva:** Claude extrae los datos de la frase y, si falla o no hay clave, responde un extractor por reglas ([ADR-002](docs/adr/ADR-002-extraccion-de-intencion.md)).
- **"La tengo yo"**, **"Ya apareció"**, **"Cerca de ti"** y posibles coincidencias al reportar un hallazgo.
- Persistencia en **H2** con JPA y casos de ejemplo al arrancar.
- **Pruebas** unitarias, de capa web, de integración con H2, de sistema por HTTP, de interfaz con Selenium y de carga con k6 frente a un SLO fijado antes de medir.
- Despliegue en **Render** en https://pet-finder-akac.onrender.com.

## [1.0.0] - 2026-08-23

Corte 1: el problema y el diseño.

### Agregado

- Núcleo de consola en Java con el dominio de reportes de mascotas perdidas y encontradas.
- Patrones **Factory Method** (creación de reportes por tipo) y **Observer** (aviso de avistamientos).
- Repositorio en memoria detrás de una interfaz, demostración automática y menú de consola.
- Modelado UML y cómic de presentación del problema ([`docs/corte-1/`](docs/corte-1/README.md)).

[Sin publicar]: https://github.com/DrearSanti/pet-finder-dyas/compare/v3.0.0...HEAD
[3.0.0]: https://github.com/DrearSanti/pet-finder-dyas/compare/v2.0.0...v3.0.0
[2.0.0]: https://github.com/DrearSanti/pet-finder-dyas/compare/v1.0.0...v2.0.0
[1.0.0]: https://github.com/DrearSanti/pet-finder-dyas/releases/tag/v1.0.0
