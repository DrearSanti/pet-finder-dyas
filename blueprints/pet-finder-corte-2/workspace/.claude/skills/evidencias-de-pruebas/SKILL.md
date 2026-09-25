---
name: evidencias-de-pruebas
description: Corre cada nivel de pruebas de Pet Finder (unitarias, integración, sistema, UI, carga) y guarda los reportes en docs/evidencias/ con el nombre acordado, para la rúbrica y la exposición. Úsala cuando pidan "evidencias", "capturas de pruebas", "reporte de cobertura", "resultados de k6" o al cerrar las tareas de pruebas de sistema, carga o documentación.
---

# Evidencias de pruebas

La rúbrica pide evidencia de cada nivel de prueba. Esta skill la produce siempre igual, para que los tres guarden lo mismo.

## Nombre de los archivos

`docs/evidencias/AAAA-MM-DD_<persona>_<que-muestra>.<ext>` — por ejemplo `2026-09-26_antonio_pruebas-sistema.txt`. Persona en minúscula: `mateo`, `santi`, `antonio`.

## 1. Unitarias, ArchUnit y slice web

```bash
./mvnw -q clean test
```

Copia el resumen: `target/surefire-reports/*.txt` → `docs/evidencias/<fecha>_<persona>_pruebas-unitarias.txt` (concatena los `.txt`).

## 2. Integración y sistema + cobertura

```bash
./mvnw -q clean verify
test -f target/site/jacoco/index.html
```

- `target/failsafe-reports/*.txt` → `<fecha>_<persona>_pruebas-integracion-y-sistema.txt`.
- Captura de `target/site/jacoco/index.html` abierta en el navegador → `<fecha>_<persona>_cobertura.png`. Anota en `docs/pruebas.md` el porcentaje de líneas del paquete `domain`.

## 3. UI (bono, necesita Chrome)

```bash
./mvnw -q -Pui verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=FlujosPrincipalesUIT
```

`target/failsafe-reports/*UIT.txt` → `<fecha>_<persona>_pruebas-ui.txt`.

## 4. Carga (Mateo, con la app corriendo)

```bash
./mvnw -q -DskipTests package
java -jar target/pet-finder.jar
k6 run --summary-export=perf/resultados/carga.json perf/scripts/carga.js
```

(Lo primero en una terminal y k6 en otra.) Los JSON se quedan en `perf/resultados/`; la captura del resumen de k6 va a `perf/resultados/carga.png`.

## 5. Registro

Agrega una fila por evidencia a la tabla de `docs/pruebas.md`: fecha, persona, nivel, comando, resultado (pasaron / fallaron) y ruta del archivo. Nunca borres evidencias anteriores: si repites, el nombre lleva la fecha nueva.

El commit de las evidencias lo hace la persona, no el agente.
