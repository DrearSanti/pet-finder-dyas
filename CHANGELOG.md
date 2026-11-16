# Cambios de Pet Finder

Lo que cambió en cada versión, de la más nueva a la más vieja. Las versiones siguen el [versionado semántico](https://semver.org/lang/es/): el primer número cambia con cada corte del curso, el segundo cuando se agregan funciones sin romper nada y el tercero con correcciones. Cada versión tiene su tag `vX.Y.Z` en Git y su página en [GitHub Releases](https://github.com/DrearSanti/pet-finder-dyas/releases).

Desde el Corte 3, cada versión publicada por el pipeline queda como imagen en `ghcr.io/drearsanti/pet-finder` con la etiqueta de la versión, además de la del SHA de su commit. Las versiones 1.0.0 y 2.0.0 son anteriores al pipeline y no tienen imagen.

**Regla del equipo:** cada pull request que cambie algo visible para quien usa, instala u opera el sistema agrega su línea en **Sin publicar**. Al crear un tag, esa sección pasa a ser la de la versión nueva.

## Sin publicar

Corte 3: entrega continua y segura. Lo que ya está en `main`:

### Agregado

- Persistencia real en **PostgreSQL** con migraciones de **Flyway**; Hibernate solo valida el esquema. Los IDs de los reportes siguen la numeración después de un reinicio (S7).
- **Control de secretos con Gitleaks** sobre todo el historial de Git, en el workflow `Seguridad`, con corrida diaria programada (S5).
- **Análisis estático con CodeQL** para Java y JavaScript, con un paso propio que detiene el pipeline ante hallazgos de severidad 7.0 o más sin tratar (S6).
- **Prueba de carga corta** `perf/scripts/corta.js`: el mismo SLO de `carga.js` en un minuto y medio, a tasa fija, cerrando los casos que crea. Workflow manual `Carga larga` que corre `carga.js` contra Compose con PostgreSQL (M4).
- **Cabeceras de seguridad** en todas las respuestas: CSP sin `unsafe-inline`, `nosniff`, HSTS, `Permissions-Policy` y `Referrer-Policy`. Leaflet se carga con verificación de integridad (SRI) y el registro del service worker pasó a `js/registro-sw.js` (M9).
- **Contenedores:** Dockerfile de dos etapas con usuario sin privilegios y healthcheck, y `docker-compose.yml` con la aplicación y PostgreSQL (27 de octubre).
- Plan del corte, reparto de tareas y la guía de cada integrante en `docs/` (plan del Corte 3).

### Cambiado

- La **carga corta del CD** contra Render corre a 6 peticiones por segundo durante 330 s, con los mismos umbrales de latencia y errores; la capacidad de 30 req/s la exige `despliegue-y-verificacion` en cada PR. Render se queda en el plan gratis (revisión de ADR-004, M7).
- La lista de casos activos sale en **una sola consulta**, con un tope de 200 casos, y `ver reportes` por voz devuelve los 10 más recientes (S8).
- Las **reglas de arquitectura** de ArchUnit son más estrictas: la consola también entra solo por los puertos, y cada adaptador de salida queda aislado de los demás (S4).
- El **repositorio quedó ordenado**: el README empieza por "Empieza aquí" y el mapa de la rúbrica, y el material del Corte 1 pasó a `docs/corte-1/` (M3).

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

[Sin publicar]: https://github.com/DrearSanti/pet-finder-dyas/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/DrearSanti/pet-finder-dyas/compare/v1.0.0...v2.0.0
[1.0.0]: https://github.com/DrearSanti/pet-finder-dyas/releases/tag/v1.0.0
