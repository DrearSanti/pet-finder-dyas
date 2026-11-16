# Presentación del Corte 3

Guion de 15 minutos para la exposición final y preparación de la demo en vivo. La califican los compañeros (10 puntos): cuenta que se vea el pipeline funcionando de verdad, no capturas.

**La idea que tiene que quedar:** en el Corte 2 la arquitectura se verificaba a mano. Ahora cada cambio pasa por un pipeline que revisa la arquitectura, la seguridad y el rendimiento, y solo lo que pasa llega a producción, sin que nadie despliegue a mano.

## Reparto y tiempos

| Min | Parte | Quién | Qué se muestra |
|---|---|---|---|
| 0:00 | Apertura y arranque de la demo | Mateo | La app en vivo y el merge del PR que va a desplegarse |
| 1:30 | 1. Contenedores e integración continua | Antonio | `Dockerfile`, `docker-compose.yml` y el workflow CI con sus jobs |
| 4:00 | 2. Arquitectura verificada en el pipeline | Santi | Las 9 reglas de ArchUnit y el PR que el pipeline bloquea |
| 6:30 | 3. DevSecOps | Santi | El workflow Seguridad, el modelo de amenazas y lo que se corrigió |
| 9:30 | 4. Despliegue continuo y reversión | Mateo | El CD corriendo, el ADR-004 y el workflow Revertir |
| 12:00 | 5. Cierre de la demo y lecciones | Los tres | `/actuator/info` con el commit nuevo y lo que aprendimos |
| 14:00 | Preguntas | Los tres | — |

Si una parte se alarga, se recorta la 5: la demo ya habló por sí sola.

## Antes de entrar (la noche anterior y 30 minutos antes)

**La noche anterior:**

1. `main` en verde en los tres badges del README. Si el CD está rojo, se arregla antes de cualquier otra cosa: sin eso la demo no tiene final.
2. Los dos PR de demo abiertos (ver [Los dos PR de la demo](#los-dos-pr-de-la-demo)):
   - **demo/desplegar**: checks en verde y **aprobado** por otro integrante. Si se actualiza la rama después de aprobarlo, hay que volver a aprobarlo.
   - **demo/bloqueado**: con el check `reglas-arquitectura` en rojo. No se aprueba.
3. Ensayo cronometrado con los tres, de punta a punta, con el reloj a la vista.

**30 minutos antes:**

1. Despertar la app: abrir https://pet-finder-akac.onrender.com y https://pet-finder-akac.onrender.com/actuator/info. Anotar el commit que sale.
2. Actualizar la rama de **demo/desplegar** si `main` cambió (`gh pr update-branch <número>`), esperar los checks y volver a pedir la aprobación.
3. Pestañas abiertas en este orden: la app, `/actuator/info`, el PR demo/desplegar, el PR demo/bloqueado, Actions → CD, Actions → Revertir, `docs/adr/ADR-004-estrategia-de-despliegue.md` en GitHub.
4. Navegador con el zoom al 125 % y las notificaciones silenciadas.

## Guion

### Apertura (Mateo, 1:30)

- "Pet Finder conecta a quien perdió una mascota con quien la vio. En el Corte 2 construimos la app con arquitectura hexagonal. En este corte la pregunta era: ¿cómo sabemos que cada cambio sigue respetando esa arquitectura, es seguro y aguanta la carga, sin revisarlo a mano?"
- Mostrar la app en vivo y `/actuator/info`: "este es el commit que está en producción ahora".
- **Hacer merge del PR demo/desplegar** en vivo: "acabo de integrar un cambio. Nadie va a tocar Render. Al final de la presentación volvemos a esta pestaña".

### 1. Contenedores e integración continua (Antonio, 2:30)

- `Dockerfile` de dos etapas: compila con el JDK y corre con un JRE, con usuario sin privilegios y un healthcheck. `docker-compose.yml` levanta la app con PostgreSQL; los datos sobreviven a los reinicios gracias a Flyway.
- El workflow **CI**: corre en cada push y en cada pull request. Mostrar el grafo de jobs: `construccion` y después, en paralelo, unitarias con umbral de cobertura del dominio, reglas de arquitectura, integración y la interfaz con Selenium.
- La protección de `main`: no se puede hacer push directo, cada PR necesita la aprobación de otro integrante y los checks en verde. "Ni siquiera el dueño del repositorio se lo salta".
- Versiones: tags `vX.Y.Z` y el `CHANGELOG.md`.

### 2. Arquitectura verificada en el pipeline (Santi, 2:30)

- Las 9 reglas de `ReglasArquitecturaTest`: el dominio es Java puro, las entradas solo hablan con puertos, persistencia e IA no se conocen, solo `config` conoce los adaptadores concretos, el asistente nunca publica.
- **Mostrar el PR demo/bloqueado:** "alguien tomó un atajo: el controlador pide el servicio concreto en vez del puerto. Compila y la app funcionaría. Pero el check `reglas-arquitectura` está en rojo y el botón de merge está bloqueado". Abrir el log y leer la regla que se violó.
- La carga corta (`perf/scripts/corta.js`) mide el SLO del Corte 2 después de cada despliegue: p95 por debajo de 500 ms, menos de 1 % de errores y al menos 30 peticiones por segundo. "La arquitectura no solo se revisa en el código, también en producción".

### 3. DevSecOps (Santi, 3:00)

- El workflow **Seguridad**: Gitleaks sobre todo el historial, CodeQL para Java y JavaScript con un umbral de severidad 7.0, y el análisis de dependencias. Corre en cada push y todos los días, porque aparecen vulnerabilidades nuevas sin que nadie toque el código.
- El modelo de amenazas en `docs/seguridad.md`: qué podía salir mal y qué se hizo. Tres ejemplos que se entienden en una frase:
  - **Un texto enorme trababa el asistente**: 100 KB ocupaban un núcleo 17 segundos. Ahora hay tope de 1000 caracteres, las expresiones están acotadas y un cuerpo de más de 64 KB responde 413.
  - **La consola de la base estaba abierta** en el sistema desplegado. Ahora solo se prende en el computador de quien desarrolla.
  - **Cabeceras de seguridad** en todas las respuestas, CSP sin `unsafe-inline` y Leaflet con verificación de integridad.
- Los secretos: el deploy hook vive solo en el entorno `produccion` de GitHub y las claves en el panel de Render. Ninguno está en el repositorio.

### 4. Despliegue continuo y reversión (Mateo, 2:30)

- Abrir **Actions → CD**: debería ir por la mitad la corrida del merge de la apertura. Explicar los pasos sobre la corrida real:
  1. Construye la imagen y la publica en GitHub Container Registry con el SHA del commit, nunca con `latest`.
  2. Arranca la imagen publicada y comprueba que reporta ese mismo SHA.
  3. Llama al deploy hook de Render y espera a que la URL pública muestre el SHA nuevo.
  4. Prueba de humo y carga corta. Si no se cumple el SLO, la corrida queda en rojo.
- Por qué Render con la imagen de GHCR, en una frase: se despliega exactamente la imagen que probó el pipeline (ADR-004).
- **Revertir:** mostrar el workflow y una corrida real que volvió a una versión anterior por su tag. "Volver atrás no es reconstruir: es desplegar una imagen que ya pasó el pipeline".

### 5. Cierre de la demo y lecciones (los tres, 2:00)

- **Mateo:** recargar `/actuator/info`. El commit cambió por el del merge de la apertura y aparece `"corte": "3"`. Mostrar la corrida del CD en verde, con la carga corta.
- **Antonio:** una lección. Por ejemplo, que un check obligatorio tiene que correr siempre, o los PR se quedan esperando para siempre.
- **Santi:** una lección. Por ejemplo, que la seguridad encontró cosas reales que el Corte 2 no veía, como el texto que trababa el asistente.
- **Mateo:** una lección. Por ejemplo, que el plan gratis de Render no aguanta el SLO: el pipeline lo detectó y la decisión quedó registrada en el ADR-004.

## Los dos PR de la demo

Los abre Mateo desde su computador. Cada uno es un cambio de una o dos líneas y se crea desde `main` actualizado.

### demo/desplegar: un cambio que pasa y llega a producción

Agrega una línea a `/actuator/info`, para que el cambio se vea en la URL pública sin tocar la interfaz.

```bash
git switch main && git pull && git switch -c demo/desplegar
```

En `src/main/resources/application.properties`, debajo de `info.app.version=...`, agregar:

```properties
info.app.corte=3
```

```bash
git commit -am "Demo: /actuator/info muestra el corte" && git push -u origin demo/desplegar
```

```bash
gh pr create --base main --head demo/desplegar --title "Demo: cambio que se despliega" --body "Agrega info.app.corte a /actuator/info. Se integra en vivo en la presentación."
```

### demo/bloqueado: un cambio que el pipeline detiene

El controlador de reportes pide el servicio concreto en vez del puerto. Compila y la app funcionaría, pero viola la regla `lasEntradasSoloHablanConPuertos`. Comprobado: compila, y 1 de las 9 reglas falla.

```bash
git switch main && git pull && git switch -c demo/bloqueado
```

En `src/main/java/petfinder/adaptadores/entrada/web/ReporteController.java`:

1. Debajo de `import petfinder.application.port.entrada.GestionReportes;` agregar `import petfinder.application.service.ServicioReportes;`.
2. Cambiar el constructor `public ReporteController(GestionReportes gestionReportes) {` por estas dos líneas:

```java
    // Atajo: pedir el servicio concreto en vez del puerto
    public ReporteController(ServicioReportes gestionReportes) {
```

```bash
git commit -am "Demo: el controlador usa el servicio concreto" && git push -u origin demo/bloqueado
```

```bash
gh pr create --base main --head demo/bloqueado --title "Demo: cambio que el pipeline bloquea" --body "El controlador depende de ServicioReportes y no del puerto GestionReportes. No se integra: es para mostrar el bloqueo."
```

Después de la presentación, **demo/bloqueado** se cierra sin integrar (`gh pr close <número> --delete-branch`). **demo/desplegar** queda en `main`.

## Preguntas que pueden hacer

| Pregunta | Respuesta corta | Quién |
|---|---|---|
| ¿Qué pasa si el despliegue sale mal? | El CD queda en rojo y se lanza Revertir con el tag anterior; despliega una imagen que ya pasó el pipeline | Mateo |
| ¿Por qué no Kubernetes? | Más riesgo del que cabía en el corte, para una sola instancia; está en la decisión D4 y en el ADR-004 | Mateo |
| ¿Cómo saben que no hay secretos en el repositorio? | Gitleaks revisa todo el historial en cada push y una vez al día | Santi |
| ¿Por qué las reglas de ArchUnit y no una revisión? | Una revisión se olvida; un check obligatorio no deja integrar | Santi |
| ¿Cómo lo instala otra persona? | Con el README desde un clon limpio: con Java o con Docker Compose; la guía de implementación cubre la operación | Antonio |
| ¿Qué pasa con los datos al reiniciar? | Viven en PostgreSQL y Flyway versiona el esquema; ya no se pierden como con H2 | Antonio |

## Plan B

| Si pasa esto | Se hace esto |
|---|---|
| El CD no termina a tiempo | Mostrar la corrida en curso y la última corrida verde de `main`; volver a la pestaña cuando termine, en las preguntas |
| Render tarda en despertar | Mostrar `/actuator/info` de la corrida verde en el log del CD, donde imprime el commit desplegado |
| No hay internet | Los enlaces a corridas ya terminadas, guardados en un documento, y el README con los badges |
| Alguien aprueba demo/bloqueado por error | No importa: el botón sigue bloqueado por el check rojo. Es justo lo que se quiere mostrar |
