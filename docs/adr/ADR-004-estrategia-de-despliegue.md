# ADR-004: Estrategia de despliegue del Corte 3

| | |
|---|---|
| **Estado** | Aceptada |
| **Fecha de registro** | 2026-11-11 (la decisión es la D2 del [plan del Corte 3](../plan-corte-3.md), tomada el 2026-11-07) |
| **Decide** | Equipo Pet Finder (Santiago Escobar, Antonio Benítez, Mateo Ramírez) |
| **Reemplaza** | El despliegue del Corte 2: Render construía la imagen desde el repositorio en cada push, con H2 en memoria |

## Contexto

En el Corte 2 la aplicación ya corría en Render, en https://pet-finder-akac.onrender.com, pero de una forma que el Corte 3 no admite:

- **La imagen la construía Render, no el pipeline.** El enunciado pide que la imagen se construya en el pipeline y que se despliegue esa misma imagen, etiquetada con el SHA del commit y nunca solo como `latest`.
- **No había verificación después de desplegar.** Si una versión arrancaba mal, nadie se enteraba hasta abrir la página. El Corte 3 pide prueba de humo y prueba de carga corta contra el entorno desplegado, y que el pipeline falle si no se cumple el SLO.
- **No había reversión probada.** Volver a la versión anterior dependía de los botones de Render, sin registro en GitHub.
- **Los datos vivían en H2 en memoria.** Cada reinicio, y el plan gratis reinicia al despertar, volvía a los casos de ejemplo.

Las restricciones son las del curso: sin presupuesto, una semana para dejarlo funcionando, tres personas en macOS y Windows, y una URL que ya está en el README, en la presentación y en la evidencia del Corte 2.

## Opciones consideradas

| Opción | A favor | En contra |
|---|---|---|
| **Render con la imagen de GHCR, deploy hook y PostgreSQL de Render** (elegida) | La imagen la construye y la publica el pipeline; Render solo la descarga. La URL no cambia. La base queda en la red interna de Render, en el mismo panel. No cuesta nada | El plan gratis se duerme a los 15 minutos y tiene poca CPU, así que el SLO queda justo. La base gratis vence a los 30 días. La cuenta de Render es de una sola persona |
| Render construyendo desde el repositorio (lo que había) | Cero cambios | No cumple "la imagen se construye en el pipeline": la que corre en Render no es la que probó el pipeline. No hay etiqueta por SHA ni reversión desde GitHub |
| Railway o Fly.io con la misma imagen | Técnicamente equivalentes: corren una imagen de un registro y aceptan despliegue desde Actions | No resuelven nada que Render no resuelva aquí, y obligan a cuentas nuevas y a cambiar la URL a una semana de la entrega |
| Azure for Students o AWS Academy (máquina o servicio de contenedores) | Más control sobre red, recursos y base | Más operación: red, certificado HTTPS, actualizaciones del sistema y créditos que vencen. Es más de lo que el equipo puede sostener y explicar en el tiempo del corte |
| Servidor propio con runner autohospedado y Docker Compose | Gratis, y es el mismo `docker-compose.yml` que ya existe | Depende de que un computador del equipo esté prendido y expuesto a internet, y el HTTPS sería manual |
| Kubernetes (kind, minikube o un clúster en la nube) | Daría el bono de Kubernetes | Es lo más riesgoso para dos semanas; el equipo lo descartó en la decisión D4 |

Para la base también se pensó en un PostgreSQL externo gratuito, pero obligaría a manejar otra cuenta y a salir a internet con la conexión. El de Render está en la misma red que la aplicación y se conecta por la URL interna.

## Decisión

**Cada commit que llega a `main` se empaqueta como imagen en GitHub Container Registry y se despliega en el servicio web de Render que ya existía, ahora en modo de imagen, con una base PostgreSQL de Render.**

El flujo lo hace el workflow `CD` (`.github/workflows/cd.yml`):

1. **Publicar imagen.** Construye la imagen con el `Dockerfile` y la publica como `ghcr.io/drearsanti/pet-finder:<SHA>`. Un tag `v*` la publica además como `ghcr.io/drearsanti/pet-finder:<versión>`. Nunca se usa `latest`. Antes de seguir, arranca la imagen publicada y comprueba que `/actuator/info` diga ese mismo SHA.
2. **Desplegar en Render y verificar.** Solo en `main` y dentro del entorno `produccion` de GitHub, llama al deploy hook de Render (secreto `RENDER_DEPLOY_HOOK_URL`) con la imagen de ese commit. Espera hasta que `/actuator/info` de la URL pública muestre el SHA nuevo, porque mientras Render cambia de versión la anterior sigue respondiendo sana. Después corre la prueba de humo y `perf/scripts/corta.js`; si el SLO no se cumple, el despliegue queda en rojo.

La base se conecta con `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`, configuradas en el panel de Render. Flyway crea y migra el esquema al arrancar. Las claves de Anthropic y de CARTO también viven en el panel de Render, nunca en la imagen ni en el repositorio.

**Reversión.** Volver a una versión anterior es pedirle a Render la imagen de su SHA o de su tag con el mismo deploy hook, y esperar a que `/actuator/info` la muestre. Se hace desde GitHub Actions para que quede registrada (tarea M8).

**Plan de Render.** Se empieza en el plan gratis. Si la carga corta falla dos de tres veces por falta de CPU, se pasa el servicio al plan Starter (7 USD al mes, 0,5 CPU, cobro por segundo), según la decisión D2.

## Consecuencias positivas

- **Despliegue sin pasos manuales:** un merge a `main` termina con la versión nueva en la URL, comprobada por el SHA, por la prueba de humo y por el SLO.
- **Se despliega lo que se probó:** la imagen que corre en Render es la que construyó y arrancó el pipeline, no una reconstrucción.
- **Se sabe qué corre:** `/actuator/info` dice el commit y la versión, así que cualquiera puede comprobar qué está en producción.
- **Reversión simple:** cada versión publicada sigue en GHCR con su SHA y su tag, y volver a ella es un deploy hook.
- **Los datos sobreviven a los reinicios** gracias a PostgreSQL y Flyway, y el esquema queda versionado en el repositorio.
- **La URL no cambió:** sirve la del Corte 2 en el README, la presentación y la evidencia.

## Consecuencias negativas

- **El SLO queda justo en el plan gratis:** medido solo con lecturas, el p95 en Render varió entre 303 y 670 ms frente a la meta de 500 ms. La carga corta puede fallar por la plataforma y no por el código; por eso existe la salida al plan Starter.
- **El plan gratis se duerme a los 15 minutos** y la primera visita tarda cerca de un minuto. El paso de espera del despliegue lo absorbe, pero una demo debe despertar la app antes.
- **La base gratis vence a los 30 días**, más 14 de gracia. Alcanza para la calificación, pero no para operar de verdad; la guía de implementación lo trata como algo que el negocio debe cambiar.
- **La cuenta de Render es de una sola persona del equipo.** Los cambios en el panel (variables, base, deploy hook) dependen de que esa persona los haga; el resto del equipo trabaja por GitHub.
- **El paquete de GHCR tiene que ser público** para que Render lo descargue sin credenciales. La imagen no lleva secretos, así que publicarla no expone nada.
- **El deploy hook es un secreto:** quien lo tenga puede desplegar cualquier imagen. Vive solo en el entorno `produccion` de GitHub.

## Cuándo se revisaría

- Si la carga corta falla dos de tres veces en el plan gratis: pasar a Starter.
- Si el sistema lo adopta una organización real: plan pagado de Render para la aplicación y la base, copias de seguridad programadas y un dominio propio. Lo detalla `docs/implementacion.md`.
- Si se necesita más de una instancia o un segundo entorno de pruebas: un entorno `pruebas` en GitHub con su propio servicio en Render, y promoción a `produccion` con aprobación manual.
