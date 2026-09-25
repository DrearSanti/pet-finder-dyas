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

- La app corre con el jar o con `./mvnw spring-boot:run`, JDK 17, **sin `ANTHROPIC_API_KEY`**. El endpoint de voz no usa Claude, así que la prueba mide solo nuestro código y no gasta créditos.
- La base es H2 en memoria: arranca vacía cada vez. Se reinicia la app entre el baseline y la carga para que las dos empiecen desde cero.
- k6 corre en la misma máquina que la app o en otra (PC o VM) apuntando con `BASE_URL`. Si comparten máquina, compiten por CPU; se anota en los resultados junto con el modelo de la máquina, la CPU y la RAM.

## Cómo ejecutar

Terminal 1, la app (macOS o Git Bash):

```bash
./mvnw spring-boot:run
```

En PowerShell: `.\mvnw.cmd spring-boot:run`.

Terminal 2, las pruebas (igual en macOS, Git Bash y PowerShell):

```bash
k6 run --summary-export perf/resultados/baseline.json perf/scripts/baseline.js
k6 run --summary-export perf/resultados/carga.json perf/scripts/carga.js
```

Si la app está en otra máquina, agrega `-e BASE_URL=http://<ip>:8080` antes del nombre del guion.

Si el SLO no se cumple, k6 termina con un código distinto de 0. **Es un resultado válido**: se reporta tal cual y se analiza. Reconocer un límite con datos vale más que maquillarlo.

## Resultados

Se llenan en la tarea `E3-T9`.

| Prueba | Usuarios | Duración | p95 global | p95 `voz_registrar` | p95 `voz_listar` | req/s | Errores | ¿Cumple el SLO? |
|---|---|---|---|---|---|---|---|---|
| Baseline | 5 | 1 min | __ | __ | __ | __ | __ | __ |
| Carga | 50 | 4 min 30 s | __ | __ | __ | __ | __ | __ |

Máquina de la corrida: __

### Cuello de botella

__
