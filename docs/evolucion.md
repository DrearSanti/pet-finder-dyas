# Evolución de Pet Finder en los tres cortes

Pet Finder conecta a quien perdió una mascota con quien la vio o la encontró. Este documento cuenta cómo pasó de un módulo de consola a un sistema desplegado con entrega continua y segura: qué se decidió en cada corte, qué se mantuvo, qué cambió y por qué, cómo se resolvió cada pendiente del Corte 2 y cuáles son los límites del sistema final.

## Corte 1: el dominio y sus patrones

**Qué se construyó.** El modelo de dominio en Java puro: `ReporteMascota` con sus dos tipos (`ReportePerdida` y `ReporteEncontrada`), avistamientos, ubicaciones y contactos, y los estados de un caso (`ACTIVO`, `RESUELTO`, `CERRADO`) con sus transiciones en `EstadoReporte`. Se usaba desde una consola con una demo guiada y un menú interactivo.

**Decisiones de diseño.**
- **Factory Method** para crear reportes: cada tipo tiene su creador, y el servicio pide un reporte sin nombrar la clase concreta.
- **Observer** para los avistamientos: el dueño recibe una alerta y la auditoría registra el evento, sin que el reporte sepa quién escucha.
- Repositorio en memoria detrás de una interfaz.

La documentación del Corte 1 (guía técnica, diagramas UML y cómic) quedó en [`docs/corte-1/`](corte-1/).

## Corte 2: arquitectura hexagonal y los dos retos

**Retos asignados.** Menú interactivo (usabilidad y modificabilidad) y captura de voz (rendimiento y modificabilidad).

**Qué se construyó.** Una aplicación web sobre Spring Boot con **arquitectura hexagonal** ([ADR-001](adr/ADR-001-estilo-arquitectonico.md)): el dominio y los casos de uso en Java puro, puertos de entrada y salida, y adaptadores para la API REST, la voz, el asistente con tarjeta viva, la consola del Corte 1, H2 y Claude. La página es una PWA con mapa. El asistente entiende la frase con Claude y, si no hay clave o falla, con reglas ([ADR-002](adr/ADR-002-extraccion-de-intencion.md)).

**Pruebas.** 253 pruebas automáticas (unitarias, ArchUnit, slice web, integración con H2, sistema por HTTP y Selenium), 83 % de cobertura y 97 % en el dominio, y una prueba de carga con k6 que cumplió el SLO (p95 de 89 ms frente a 500 ms, 0 % de errores). Todo se corría a mano, en el computador de alguno del equipo. Detalle en [`docs/pruebas.md`](pruebas.md) y [`perf/README.md`](../perf/README.md).

## Corte 3: entrega continua y segura

**Qué se construyó.**
- **Contenedores:** Dockerfile multietapa con usuario sin privilegios y `docker compose up` con la app y PostgreSQL.
- **Integración continua** (`ci.yml`): construcción, unitarias con umbral de cobertura del dominio, reglas de arquitectura, integración y UI, cada una como check obligatorio en `main`.
- **DevSecOps** (`seguridad.yml` y `ci.yml`): secretos con Gitleaks, SAST con CodeQL y un umbral propio, SCA con Trivy y escaneo de la imagen. Herramientas y umbrales en [ADR-003](adr/ADR-003-seguridad-del-pipeline.md); amenazas y hallazgos en [`docs/seguridad.md`](seguridad.md).
- **Despliegue continuo** (`cd.yml`): la imagen se publica en GitHub Container Registry con el SHA del commit, se despliega en Render, y una prueba de humo y una carga corta verifican el SLO contra la URL real. Reversión con `revertir.yml`. Detalle en [ADR-004](adr/ADR-004-estrategia-de-despliegue.md) y [`docs/devops.md`](devops.md).
- **`main` protegida:** solo entra por PR con un integrante que apruebe y los checks en verde; nadie puede saltarse la regla, ni el dueño del repositorio.
- **Guía de implementación** para la organización: [`docs/implementacion.md`](implementacion.md).

## Decisiones que se mantuvieron

| Decisión | Por qué se mantuvo |
|---|---|
| Arquitectura hexagonal ([ADR-001](adr/ADR-001-estilo-arquitectonico.md)) | Fue lo que hizo baratos los cambios del Corte 3: PostgreSQL entró como otro adaptador del mismo puerto, el código de gestión entró en el núcleo sin tocar los adaptadores de salida, y las reglas pasaron de 5 a 9 sin reorganizar paquetes ([S4](https://github.com/DrearSanti/pet-finder-dyas/pull/40)) |
| Claude con respaldo de reglas ([ADR-002](adr/ADR-002-extraccion-de-intencion.md)) | El asistente sigue funcionando sin clave, que es como corren todas las pruebas y el pipeline. El Corte 3 solo le puso topes de tamaño y de frecuencia |
| El asistente nunca publica | Pasó de convención a regla de ArchUnit: `elAsistenteNuncaPublica` |
| La consola del Corte 1 | Sigue funcionando (la demo llega hasta el `PASO 11`), ahora a través de los puertos de entrada |

## Decisiones que cambiaron

| Antes | Ahora | Por qué |
|---|---|---|
| H2 en memoria; cada reinicio borraba los casos | PostgreSQL con migraciones de Flyway y Hibernate en modo `validate` (D3, [S7](https://github.com/DrearSanti/pet-finder-dyas/pull/43)) | El plan gratis de Render reinicia al despertar y se perdían los datos. Con `update`, además, el siguiente POST pisaba un caso existente |
| Render construía la imagen desde el repositorio | El pipeline construye, escanea y publica la imagen en GHCR; Render despliega esa imagen por su SHA (D2, [ADR-004](adr/ADR-004-estrategia-de-despliegue.md)) | El enunciado pide que la imagen se construya en el pipeline y que se despliegue exactamente la que se probó |
| Cualquiera podía cerrar cualquier caso | Código de gestión por caso, sin cuentas (D1, [ADR-003](adr/ADR-003-seguridad-del-pipeline.md), [S9](https://github.com/DrearSanti/pet-finder-dyas/pull/48)) | Resuelve "solo quien publicó cierra su caso" sin registro, contraseñas ni sesiones |
| Pruebas y carga a mano | Pipeline con checks obligatorios y carga corta después de cada despliegue | Un cambio que rompe una prueba, una regla o el SLO ya no llega a `main` |

## Pendientes del Corte 2 y de la retroalimentación

| Pendiente | Cómo se resolvió |
|---|---|
| 1. Pipeline de CI/CD con DevSecOps | `ci.yml` ([#50](https://github.com/DrearSanti/pet-finder-dyas/pull/50)), umbral de cobertura ([#49](https://github.com/DrearSanti/pet-finder-dyas/pull/49)), Secretos ([#39](https://github.com/DrearSanti/pet-finder-dyas/pull/39)), SAST ([#41](https://github.com/DrearSanti/pet-finder-dyas/pull/41)), SCA ([#56](https://github.com/DrearSanti/pet-finder-dyas/pull/56)), imagen ([#74](https://github.com/DrearSanti/pet-finder-dyas/pull/74)), publicación ([#45](https://github.com/DrearSanti/pet-finder-dyas/pull/45)) y despliegue en Render ([#46](https://github.com/DrearSanti/pet-finder-dyas/pull/46)) |
| 2. PostgreSQL con migraciones | Flyway con `V1__esquema_inicial.sql` y `validate`, y un generador de IDs que sigue después del último guardado ([#43](https://github.com/DrearSanti/pet-finder-dyas/pull/43)). Probado en Compose y en Render: el caso sobrevivió al redespliegue y el siguiente ID fue nuevo |
| 3. Paginación y JOIN FETCH | La lista sale en una sola consulta con `left join fetch` y un tope de 200 casos; "ver reportes" por voz nombra los 10 más recientes ([#44](https://github.com/DrearSanti/pet-finder-dyas/pull/44)). El p95 de `voz_listar` bajó de 106 ms a 9,4 ms |
| 4. Cuentas y permisos | Código de gestión por caso en lugar de cuentas: núcleo, base, voz y consola ([#48](https://github.com/DrearSanti/pet-finder-dyas/pull/48)). La conexión con los controladores web (A9) queda pendiente; ver límites |
| 5. Fotos | Fuera de este corte (D5): el enunciado no pide funcionalidades nuevas. Diseño futuro: un puerto de salida de almacenamiento de imágenes, con un adaptador a un almacenamiento de objetos y la foto como referencia en el reporte |
| 6. Selenium para Cerca de ti y coincidencias | No se alcanzó (A11). Esos flujos los cubren las pruebas de sistema por HTTP (`FlujoHallazgoSistemaIT`); los dos flujos de Selenium del Corte 2 corren en el check `pruebas-ui` |
| Profesor: seguridad, incluido si la PWA permite meter un virus | Modelo de amenazas y cuatro controles en el pipeline ([`seguridad.md`](seguridad.md)); CSP sin `unsafe-inline` e integridad en Leaflet ([#38](https://github.com/DrearSanti/pet-finder-dyas/pull/38)); acciones fijadas por SHA ([#77](https://github.com/DrearSanti/pet-finder-dyas/pull/77)). La PWA no puede instalar nada en el celular: el service worker solo guarda en caché la interfaz del mismo sitio, y la CSP impide cargar scripts de otros orígenes |
| Profesor: fotos de las mascotas | Trabajo futuro (D5), con el diseño del punto 5 |
| Profesor: reconocimiento de parecidos con IA | Trabajo futuro (D5): un puerto de salida de comparación de imágenes, alimentado por las fotos del punto 5. Las "posibles coincidencias" de hoy solo comparan texto: el nombre de la mascota, la especie y la cercanía al pin o a la zona |

## Límites conocidos del sistema final

- **Una sola instancia.** El generador de IDs vive en el proceso: dos instancias contra la misma base repetirían números. Hace falta una secuencia de la base para escalar.
- **La base gratuita de Render vence a los 30 días.** Hay que pasarla a un plan pago o exportar los datos antes.
- **El plan gratis de Render no sostiene la carga corta del SLO** (unas 33 peticiones por segundo): la misma imagen cumple en local (p95 de 11 ms) y no en Render gratis. ADR-004 decide pasar a Starter si falla.
- **La API web todavía cierra casos sin código** mientras A9 no conecte los controladores; el resto del sistema ya lo exige.
- **Quien pierde el código de gestión depende del administrador** para cerrar su caso.
- **Sin auditoría de quién cerró un caso** ni verificación de identidad de quien publica.
- Los demás riesgos aceptados, con lo que haría falta para corregirlos, están en la sección 4 de [`docs/seguridad.md`](seguridad.md).
