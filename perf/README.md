# Pruebas de carga

Este documento se escribió **antes de correr cualquier prueba**. El SLO, el escenario y los tipos de prueba quedan fijados en el commit de la tarea `E3-T1` (tag `step-03-slo-y-k6`); los resultados y el análisis se agregan después, en la tarea `E3-T9`. La fecha de ese commit es la evidencia de que el objetivo no se acomodó a lo que salió.

## Reto y atributo de calidad

| Reto | Atributo de calidad | Por qué la carga lo pone a prueba |
|---|---|---|
| Captura de voz | Rendimiento | Una frase dictada tiene que convertirse en reporte y la persona tiene que ver la lista actualizada sin esperar. Si la respuesta tarda, hablar deja de ser más rápido que llenar el formulario. |

## SLO

Definido antes de ejecutar. Se mide sobre todas las peticiones de la prueba de carga.

| Qué se mide | Métrica de k6 | Objetivo | Umbral en `carga.js` |
|---|---|---|---|
| Latencia | `http_req_duration`, percentil 95 | ≤ 500 ms | `p(95)<500` |
| Errores | `http_req_failed` | < 1 % | `rate<0.01` |
| Throughput | `http_reqs` | al menos 30 req/s sostenidos | `rate>=30` |

Cada petición lleva una etiqueta para comparar las dos operaciones del escenario: `voz_registrar` y `voz_listar`. Además del SLO global, cada etiqueta tiene su propio umbral de p95, así el resumen de k6 muestra las dos latencias por separado.

## Escenario: hora pico de reportes por voz

Cada usuario virtual repite este ciclo:

1. `POST /api/voz` con la frase `perdí un perro llamado Max en Chía <usuario>-<iteración>` y un contacto. Espera **201**. Etiqueta `voz_registrar`.
2. `POST /api/voz` con la frase `ver reportes`. Espera **200**. Etiqueta `voz_listar`.
3. Pausa de 1 segundo, el tiempo que alguien tarda en mirar la lista.

Es el camino completo del reto de voz: texto transcrito → intérprete de comandos → caso de uso → H2 → respuesta JSON. El número de usuario e iteración va en la zona y no en el nombre, porque el intérprete solo acepta letras en el nombre de la mascota; así cada reporte es distinto y la frase se sigue reconociendo.

**Hipótesis antes de correr:** `ver reportes` devuelve todos los casos activos sin paginar (lo dejamos fuera de alcance a propósito), y cada iteración agrega un caso. Esperamos que `voz_listar` se vuelva más lenta a medida que avanza la prueba, mientras `voz_registrar` se mantiene estable.

## Tipos de prueba

| Prueba | Guion | Usuarios virtuales | Duración | Para qué |
|---|---|---|---|---|
| Baseline | `perf/scripts/baseline.js` | 5 | 1 min | Latencia de referencia sin presión |
| Carga | `perf/scripts/carga.js` | sube a 50 en 1 min, 50 durante 3 min, baja a 0 en 30 s | 4 min 30 s | Verificar el SLO en la hora pico |
| Estrés (opcional) | — | se suben los usuarios hasta que el SLO se rompa | — | Solo si queda tiempo: encontrar el punto de quiebre |

## Entorno

- **Máquina:** Apple M5, 10 núcleos, 24 GB de RAM, macOS 26.6.2. OpenJDK 17.0.20 y k6 v2.2.0.
- **La app:** el jar (`java -jar target/pet-finder.jar`) con las opciones por defecto de la JVM y **sin `ANTHROPIC_API_KEY`**. El endpoint de voz no usa Claude, así que la prueba mide solo nuestro código y no gasta créditos. Se corre con el jar y no con `./mvnw spring-boot:run`, porque este último arranca la JVM con solo el compilador rápido (`TieredStopAtLevel=1`) y no representa un despliegue.
- **La base:** H2 en memoria, **en el mismo proceso que la API**. La app se reinició entre el baseline y la carga: cada prueba empezó con los 4 casos de ejemplo.
- **k6 en la misma máquina** que la app, así que compiten por CPU (en la muestra que tomé, k6 usaba cerca del 20 % de un núcleo y la app llegó a más de cinco). Ejecución del 25 de septiembre de 2026, entre las 20:26 y las 20:33.
- **Una sola corrida por prueba**, sin repeticiones: no sé cuánta variación hay entre corridas.

## Cómo ejecutar

Terminal 1, la app (macOS o Git Bash):

```bash
./mvnw -q -DskipTests package
ANTHROPIC_API_KEY= java -jar target/pet-finder.jar
```

En PowerShell: `.\mvnw.cmd -q -DskipTests package`, luego `$env:ANTHROPIC_API_KEY=""` y `java -jar target/pet-finder.jar`.

Terminal 2, las pruebas (igual en macOS, Git Bash y PowerShell). La bandera `--summary-trend-stats` agrega el conteo de peticiones por operación al JSON:

```bash
k6 run --summary-export perf/resultados/baseline.json --summary-trend-stats "avg,min,med,max,p(90),p(95),p(99),count" perf/scripts/baseline.js
k6 run --summary-export perf/resultados/carga.json --summary-trend-stats "avg,min,med,max,p(90),p(95),p(99),count" perf/scripts/carga.js
```

Reinicia la app entre una y otra. Si la app está en otra máquina, agrega `-e BASE_URL=http://<ip>:8080` antes del nombre del guion.

Si el SLO no se cumple, k6 termina con un código distinto de 0. **Es un resultado válido**: se reporta tal cual y se analiza. Reconocer un límite con datos vale más que maquillarlo.

## Resultados

Los archivos de k6 están en `perf/resultados/baseline.json` y `perf/resultados/carga.json`, y su salida de consola, como imagen, en `perf/resultados/resumen-k6-baseline.png` y `perf/resultados/resumen-k6-carga.png`. En el JSON de k6, `"thresholds": {"p(95)<500": false}` significa que el umbral **no** se superó.

| Prueba | Usuarios | Duración | p95 global | p95 `voz_registrar` | p95 `voz_listar` | req/s | Errores | ¿Cumple el SLO? |
|---|---|---|---|---|---|---|---|---|
| Baseline | 5 | 1 min | 14,0 ms | 10,9 ms | 14,4 ms | 9,8 | 0 % (0 de 590) | Sí en latencia y errores. El mínimo de 30 req/s no aplica: 5 usuarios con una pausa de 1 s no pueden pasar de unas 10 req/s |
| Carga | 50 | 4 min 30 s | 89,2 ms | 15,7 ms | 106,1 ms | 79,2 | 0 % (0 de 21.442) | **Sí, los tres criterios**: p95 de 89 ms frente a 500, 0 % de errores frente a 1 % y 79 req/s frente a 30 |

### Las dos operaciones, lado a lado

| Prueba | Operación | Peticiones | Promedio | Mediana | p95 | p99 | Máximo |
|---|---|---|---|---|---|---|---|
| Baseline | `voz_registrar` | 295 | 7,6 ms | 7,0 ms | 10,9 ms | 20,5 ms | 21,9 ms |
| Baseline | `voz_listar` | 295 | 9,8 ms | 9,6 ms | 14,4 ms | 16,4 ms | 17,2 ms |
| Carga | `voz_registrar` | 10.721 | 3,7 ms | 1,3 ms | 15,7 ms | 25,3 ms | 59,5 ms |
| Carga | `voz_listar` | 10.721 | 48,5 ms | 45,7 ms | 106,1 ms | 156,2 ms | 291,5 ms |

### Recursos de la app

| Prueba | CPU máxima | CPU promedio | Memoria al empezar | Memoria máxima |
|---|---|---|---|---|
| Baseline | 108 % | 12,9 % | 392 MB | 415 MB |
| Carga | 535 % | 131,7 % | 471 MB | 2.479 MB |

100 % es un núcleo completo; la máquina tiene 10. Las muestras, una cada 5 s, están en `perf/resultados/recursos-baseline.json` y `perf/resultados/recursos-carga.json`.

### Concurrencia: no se perdió ni se duplicó nada

Después de la carga, con la app aún viva, se ejecutó en la consola de H2 (`http://localhost:8080/h2-console`, `jdbc:h2:mem:petfinder`):

```sql
SELECT COUNT(*) FROM REPORTES
```

| Dato | Valor |
|---|---|
| Resultado de `SELECT COUNT(*) FROM REPORTES` | **10.725** |
| Respuestas 201 que contó k6 en `voz_registrar` | 10.721 |
| Casos de ejemplo que la app carga al arrancar | 4 |
| Esperado (10.721 + 4) | 10.725 |

Coincide exacto: con 50 usuarios registrando a la vez no se perdió ni se duplicó ningún reporte, así que la generación atómica de IDs aguantó la concurrencia. En el baseline el mismo control, hecho por la API, dio 299 reportes frente a 295 + 4. Detalle en `perf/resultados/concurrencia.json`.

## Cuello de botella

**La hipótesis se confirmó en lo principal.** `voz_listar` se volvió más lenta al crecer la lista y `voz_registrar` se mantuvo bajo: en la carga, el p95 de `voz_listar` es de 106 ms frente a 15,7 ms de `voz_registrar` (unas 7 veces más), su promedio de 48,5 ms frente a 3,7 ms (13 veces) y su mediana de 45,7 ms frente a 1,3 ms. En el baseline, con menos de 300 casos, las dos eran casi iguales (14,4 ms frente a 10,9 ms de p95). Un detalle que conviene no leer mal: la mediana de `voz_registrar` es *menor* en la carga (1,3 ms) que en el baseline (7,0 ms), no porque haya más carga sino porque el baseline dura un minuto con la JVM fría y aún no ha calentado.

### Por qué `voz_listar` es el cuello de botella

1. **No está paginada.** Devuelve todos los casos activos en cada llamada. Con 10.725 casos, una sola respuesta pesa 5,2 MB (medido con la app en reposo, tras la prueba: entre 58 y 73 ms). Durante la carga k6 recibió **28 GB**, unos 103 MB/s, casi todos de esa operación. Todo pasó por la interfaz local del propio computador; esos 103 MB/s equivalen a unos 0,8 Gbit/s, más de lo que sostiene una conexión doméstica o el plan gratuito de un servicio en la nube.
2. **Además hace N+1 consultas.** Contando las consultas SQL de una sola llamada, con una instancia aparte y `spring.jpa.show-sql=true`:

| Casos en la lista | Consultas `SELECT` de una llamada a `ver reportes` |
|---|---|
| 4 | 4 |
| 10 | 10 |
| 30 | 30 |

Una consulta para la lista de reportes más una por cada pérdida para cargar sus avistamientos: crece en línea con el número de casos. Extrapolando con esa relación lineal (aritmética, no medición), cada `ver reportes` con 10.725 casos ejecuta unas 10.725 consultas. Detalle en `perf/resultados/consultas-n-mas-1.json`.

3. **El SLO se cumple porque H2 está en el mismo proceso.** Cada consulta cuesta apenas unos microsegundos, y por eso 10.725 consultas caben en unos 60 ms. Esto es un hallazgo, no una garantía: si cada consulta viajara por una red hasta una base remota y costara 1 ms, serían unos 10 s por listado. Es una cuenta, no una medición.

## Qué ofrece la arquitectura y qué no

**Lo que ofrece.** El repositorio es un puerto (`RepositorioReportes`) con `RepositorioReportesH2` como adaptador. Eso permite corregir el cuello de botella sin tocar el dominio ni los servicios:

- **`JOIN FETCH`** (o un grafo de entidades) en la consulta de `RepositorioReportesH2` carga los avistamientos junto con los reportes en una sola consulta y elimina el N+1. El cambio vive solo en el adaptador de persistencia.
- **Limitar o paginar la lista.** Si basta con un tope fijo (por ejemplo, los más recientes), también se resuelve dentro del adaptador. Si se quiere paginar de verdad, cambia la firma del puerto y con ella el servicio y el controlador, pero el dominio sigue igual.

**Lo que no ofrece.**

- **H2 vive en el mismo proceso que la API.** Comparten CPU y memoria: durante la carga la memoria pasó de 471 MB a 2.479 MB y la CPU llegó a 535 %. La base compite con las peticiones por los mismos recursos.
- **No se puede escalar la API en varias instancias**, porque cada una tendría su propia base en memoria con sus propios datos, y todo se pierde al reiniciar.
- **La arquitectura no decide por nosotros** que `ver reportes` devuelva todo: eso es una decisión de diseño que este resultado pone en duda.

## Límites de esta medición

- Una sola corrida por prueba, sin repeticiones.
- k6 y la app comparten máquina.
- Los datos crecen durante la prueba: un `ver reportes` con más de 10.000 casos es mucho más de lo que tendría la app en uso real. Un SLO cumplido con esta lista no garantiza el mismo resultado con una base remota.
- El conteo de consultas N+1 se midió con hasta 30 casos; el de 10.725 es una extrapolación.
- Las imágenes `perf/resultados/resumen-k6-baseline.png` y `perf/resultados/resumen-k6-carga.png` **no son capturas de pantalla**: son la salida de k6 de esas corridas, guardada como texto y dibujada como imagen. No hay captura de la consola de H2 con el conteo: la base es en memoria y desapareció al apagar la app. Lo que respalda el conteo es `perf/resultados/concurrencia.json` y el resultado anotado en este documento.
