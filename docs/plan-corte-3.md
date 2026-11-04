# Plan del Corte 3: reparto de tareas

Meta: terminar el domingo 15 de noviembre de 2026. Enunciado y rúbrica: `Enunciado Corte 3.pdf` y `Rubrica Corte 3.pdf`. Este plan sale de una auditoría del repo contra la rúbrica el 4 de noviembre.

Para Claude Code: las reglas de `CLAUDE.md` siguen vigentes. Nunca hagas commit, tag ni push. Cada tarea va en una rama `corte3/<id>` y termina con su **Hecho cuando** comprobado. Revisa las dependencias antes de empezar.

## Decisiones

| ID | Pregunta | Recomendación | Por qué |
|---|---|---|---|
| D1 | ¿Cuentas de usuario o código de gestión? | Código de gestión más un código de administrador. | Resuelve lo que declaramos ("que solo quien publicó pueda cerrar su caso") sin registro ni contraseñas, y el enunciado no pide funcionalidades nuevas. Se sacrifica: quien pierde el código depende del administrador. |
| D2 | ¿Dónde desplegamos? | Render con imagen de GHCR, deploy hook y PostgreSQL gratis. Si la carga corta falla 2 de 3 veces, pasar el miércoles 11 al plan Starter (7 USD al mes, 0,5 CPU, cobro por segundo). | Ya existe y la URL queda activa. Riesgo real: medí Render gratis solo con lecturas y el p95 varió entre 303 y 670 ms frente a la meta de 500. Falta medir escrituras y PostgreSQL. Un servidor propio depende de que esté prendido, y una VM en la nube suma operación. |
| D3 | ¿Migraciones con Flyway o ddl update? | Flyway. | Es lo que declaramos como pendiente, y el código de gestión necesita una columna nueva. Cuesta un poco más que update y agrega una dependencia. El generador de IDs hay que arreglarlo de todos modos. |
| D4 | ¿Qué bonos hacemos? (máximo +10) | DAST sí. Observabilidad solo si queda tiempo. Kubernetes no. | DAST sale casi gratis con las cabeceras de M9. Observabilidad toma más, y Kubernetes es lo más riesgoso para dos semanas. |
| D5 | ¿Qué hacemos con fotos y reconocimiento con IA? | No se construyen en este corte: quedan en evolucion.md con su diseño futuro. Confirmarlo con el profesor. | Él los mencionó en su retroalimentación, pero el enunciado dice que no se piden funcionalidades nuevas. Un mensaje corto evita sorpresas al calificar. |
| D6 | ¿Quién revisa a quién? | Antonio revisa a Santi, Mateo a Antonio y Santi a Mateo. Respuesta en menos de 24 horas. | Hoy 0 de 32 PR tienen revisión, y la rúbrica pide PR aprobados por otro integrante. Eso solo puede salir de los PR de ahora en adelante. |

Estas son recomendaciones hasta que el equipo las confirme.

## Reglas del equipo

- Todo entra por pull request, en una rama por tarea y con merge commit, nunca squash.
- Revisión cruzada: Antonio revisa a Santi, Mateo a Antonio y Santi a Mateo, en menos de 24 horas.
- Si un check falla, se arregla la causa. Bajar un umbral o apagar una regla sin justificarlo cuenta como control no implementado.
- Los tres deben poder explicar cualquier parte.
- Cada quien entrega sus coevaluaciones: quien no lo haga pierde esos puntos en su nota individual.

## Base (Mié 4 a dom 8 nov)

Hito del Dom 8: un merge a main se despliega solo, y los cuatro controles de seguridad pueden bloquear.

### S1 · Ajustes del repositorio

**Santi** · esfuerzo S, solo el dueño del repo

Settings > General: dejar solo merge commit. Settings > Actions: habilitado y permisos de solo lectura. Settings > Code security: alertas de Dependabot, code scanning y Secret Protection. Crear el environment produccion.

**Hecho cuando:** gh api repos/DrearSanti/pet-finder-dyas muestra allow_squash_merge y allow_rebase_merge en false, y /vulnerability-alerts responde 204.

### S2 · Proteger main

**Santi** · esfuerzo S, solo el dueño del repo

Ruleset activo, sin lista de bypass: pull request con 1 aprobación, descartar aprobaciones al subir commits, ramas al día, checks obligatorios, sin force push ni borrado. No activar historial lineal, porque integramos con merge commit. Volver a agregar checks a medida que aparezcan.

**Hecho cuando:** Un git push directo a main se rechaza y un PR sin aprobación o con un check en rojo no se puede fusionar.

**Depende de:** A2: GitHub solo ofrece nombres de checks que ya corrieron

### S3 · Secretos de despliegue

**Santi** · esfuerzo S, solo el dueño del repo

En el environment produccion, crear RENDER_DEPLOY_HOOK_URL y lo que pida cd.yml. Mateo pasa los valores por privado. Dejar público el paquete en GHCR después de la primera publicación, para que Render lo baje sin credenciales.

**Hecho cuando:** gh api .../environments/produccion/secrets lista los nombres y cd.yml los usa sin que salgan en los logs.

**Depende de:** M5 para el paquete y M6 para el hook

### S4 · Endurecer las reglas de arquitectura

**Santi** · esfuerzo M

La regla actual solo vigila la web. La consola usa servicios directamente: 24 violaciones medidas en MenuConsola y EscenarioDemostracion. Pasar la consola a puertos y ampliar la regla a todo adaptadores.entrada. Agregar: persistencia e IA no se conocen entre sí, y nadie fuera de config usa adaptadores de salida concretos. Citar las reglas en ADR-001.

**Hecho cuando:** ReglasArquitecturaTest pasa con las reglas nuevas, y una violación a propósito pone en rojo el check reglas-arquitectura.

**Depende de:** A2 para ver el check

### S5 · Control de secretos con Gitleaks

**Santi** · esfuerzo S

Job Secretos en .github/workflows/seguridad.yml: gitleaks v8.30.1 en binario, con checksum validado, fetch-depth: 0 para recorrer el historial y --redact porque los logs son públicos. Agregar .gitleaks.toml si hace falta una excepción acotada. Umbral: cero secretos. Publicar el reporte como artefacto. Agregar a seguridad.yml una corrida diaria programada, para enterarnos de vulnerabilidades nuevas antes de que rompan main.

**Hecho cuando:** El check Secretos corre en verde sobre todo el historial, publica su reporte como artefacto, y se pone rojo en una rama desechable con una llave falsa.

### S6 · SAST con CodeQL y un paso que aplique el umbral

**Santi** · esfuerzo M

CodeQL (java-kotlin y javascript-typescript, build-mode none, security-extended) con output: sarif-results. CodeQL solo reporta, así que scripts/umbral-sarif.py lee el SARIF y falla con security-severity de 7,0 o más sin tratar. Las excepciones van en .github/codeql/excepciones.txt, con su fila en docs/seguridad.md. El SARIF se publica como artefacto.

**Hecho cuando:** El check SAST corre en PR y push, los resultados salen en la pestaña Security, el SARIF queda como artefacto y el paso falla en una rama desechable con un controlador de prueba.

**Depende de:** M1: si no, bloquea a todos desde el primer día

### S7 · Persistencia real con PostgreSQL y Flyway (pendiente 2)

**Santi** · esfuerzo M

Hoy compose usa ddl-auto create: cada reinicio borra los casos. Con update tampoco basta: medido, el siguiente POST devuelve 201 y pisa un reporte, porque el generador de IDs arranca de 0. Agregar migraciones Flyway con ddl validate. GeneradorIdReportes recibe el último número del repositorio, comparado como número. DatosDeEjemplo no inserta si ya hay casos. Nueva IT que simula el reinicio.

**Hecho cuando:** Con compose: POST, docker compose restart app, GET devuelve la misma mascota; el siguiente POST da un ID nuevo y la lista crece en 1.

**Depende de:** D3

### A1 · Umbral de cobertura en JaCoCo

**Antonio** · esfuerzo S

En pom.xml, execution con goal check: includes petfinder/domain/**, element BUNDLE, counter LINE, COVEREDRATIO, minimum 0.70. No usar element PACKAGE: petfinder.domain.observer da 33 % solo con unitarias y rompería el build. Agregar -Djacoco.skip=true a los comandos que corren pocas IT y actualizar CLAUDE.md.

**Hecho cuando:** ./mvnw clean verify imprime "All coverage checks have been met" (hoy el dominio está en 97,2 %), y con minimum 0.99 sale BUILD FAILURE.

### A2 · ci.yml: construcción y pruebas

**Antonio** · esfuerzo L

Un job por check: construccion, pruebas-unitarias, reglas-arquitectura, pruebas-integracion y pruebas-ui. En todo push (ramas y tags v*) y en cada PR, sin paths-ignore, en ubuntu-24.04 con Java 17 y cache: maven; artefactos con if: always(). El job pruebas-unitarias corre además jacoco:report y jacoco:check, para que el umbral de cobertura sea parte del check. Ojo: usar exactamente -Dtest='*Test,!ReglasArquitecturaTest'; sin el patrón positivo corren también las IT y las UIT. Avisarle a Santi apenas corra por primera vez, para S2.

**Hecho cuando:** En un PR salen esos 5 checks en verde, con reportes como artefactos. Con minimum 0.99 en una rama, pruebas-unitarias queda en rojo.

**Depende de:** A1

### A3 · Arreglar Compose, Dockerfile y .env.example

**Antonio** · esfuerzo S

image pet-finder:local en vez de proyecto:dev. Healthcheck explícito en app. Quitar --mount=type=cache del Dockerfile para que la caché de GitHub funcione, y corregir sus comentarios. Confirmar que .env.example trae todas las variables y ningún valor real. Cambiar la regla deny Read(./.env.*) de .claude/settings.json por nombres exactos: hoy también bloquea .env.example y nadie con Claude Code puede revisarlo.

**Hecho cuando:** cp .env.example .env && docker compose up -d --build --wait termina con app y db healthy; docker compose exec app id -u no da 0. La auditoría no pudo correr esto porque Docker no respondía en el Mac.

### A4 · scripts/humo-flujo.sh

**Antonio** · esfuerzo S

Recibe la URL base y prueba el flujo principal: readiness, POST /api/reportes, GET del caso comparando el nombre, y lista. Sale distinto de 0 si algo falla. Después de M2, también exige 404 en /h2-console/. Lo usan A6 y M7.

**Hecho cuando:** Con compose arriba sale 0; con la app apagada sale distinto de 0.

**Depende de:** A3

### A5 · Control SCA con Trivy y Dependabot

**Antonio** · esfuerzo S

Job SCA: trivy fs con HIGH,CRITICAL, --ignore-unfixed y exit-code 1, acción fijada por SHA. .github/dependabot.yml para maven, docker y github-actions. Corregir el hallazgo de hoy: forzar guava 33.7.2-jre (CVE-2026-102554, alta, solo en pruebas vía Selenium).

**Hecho cuando:** El check SCA pasa en main, publica su reporte como artefacto, se pone rojo con commons-text 1.9, y ./mvnw -Pui verify sigue en verde.

### A6 · Imagen, escaneo y verificación efímera en cada PR

**Antonio** · esfuerzo M

Job imagen-y-escaneo: construir con buildx y caché type=gha, trivy image con CRITICAL, --ignore-unfixed y exit-code 1, más reporte de HIGH como artefacto. Job despliegue-y-verificacion: compose con esa imagen, humo-flujo, k6 corta y logs; down -v siempre. Así la etapa de despliegue es un check real en el PR, y es lo único automático que toca PostgreSQL. El job de imagen falla si el jar contiene config-local.js.

**Hecho cuando:** Los dos checks salen en verde en un PR y publican reportes; en rojo con commons-text 1.9 o con config-local.js dentro del jar.

**Depende de:** A2, A3, A4, A7 y M4

### A7 · Clave de CARTO en tiempo de ejecución

**Antonio** · esfuerzo S

Hoy queda dentro de la imagen como build arg. Quitar el ARG y servir /js/config-local.js desde un controlador que lea PETFINDER_CLAVE_CARTO, con 200 y cuerpo vacío si no hay clave. Agregarla a .env.example y docker-compose.yml; Mateo la crea en Render. Tiene que entrar antes de publicar la primera imagen.

**Hecho cuando:** Con la variable el mapa usa CARTO y sin ella OpenStreetMap, con la misma imagen; docker compose exec app env muestra la variable.

### M0 · Cerrar las decisiones y escribirle al profesor

**Mateo** · esfuerzo S

Reunir a los tres para confirmar D1 a D6, a más tardar el jueves 5, porque S7, S9 y M6 dependen de ellas. Mandarle un mensaje corto al profesor sobre D5: fotos y reconocimiento con IA quedan como trabajo futuro, porque el enunciado no pide funcionalidades nuevas.

**Hecho cuando:** D1 a D6 quedan marcadas en la página, y la respuesta del profesor se guarda y se cita en evolucion.md.

### M1 · Cerrar la denegación de servicio del asistente

**Mateo** · esfuerzo M

Medido: un POST de 100 KB ocupa un núcleo 17 s por una regex cuadrática en ExtractorRegex. Acotar CORREO, máximo 1000 caracteres por frase (400), longitud máxima por campo en los DTO y filtro de 64 KB (413). Prueba con @Timeout(1) sobre 100 KB. Debe entrar antes de S6.

**Hecho cuando:** ./mvnw clean verify pasa con esa prueba; un POST de 2 KB responde 400 sin llamar a Claude y uno de 1 MB responde 413.

### M2 · Apagar la consola H2 por defecto

**Mateo** · esfuerzo S

Hoy responde 200 en Render, aunque sin conexiones remotas. Dejarla solo en un perfil local y actualizar README y CLAUDE.md.

**Hecho cuando:** Una IT espera 404 en /h2-console/, y curl a la URL desplegada también.

### M3 · Ordenar el repositorio

**Mateo** · esfuerzo S

El README tiene 760 líneas con el Corte 1 mezclado y README_TECNICO.md todavía pide instalar Maven. Pasar el Corte 1 a docs/corte-1/ (con uml y comic), dejar un README con "Empieza aquí" y el mapa de criterios del Corte 3, e índices por carpeta. Hacerlo primero, para no chocar con los demás en el README.

**Hecho cuando:** Ningún enlace roto, y las verificaciones del plan del Corte 2 sobre el README siguen pasando.

### M4 · perf/scripts/corta.js

**Mateo** · esfuerzo S

Mismo SLO de carga.js en menos de 2 minutos: 30 s de calentamiento y 60 s de medición a tasa fija de 33 peticiones por segundo. p95 menor a 500 ms en total y por operación, errores menores a 1 %, al menos 1800 peticiones y cero iteraciones perdidas. Cierra los casos que crea, para no llenar el mapa. El borrador, ya probado en local, está en el apéndice de docs/plan-corte-3.md. Agregar también carga-larga.yml con workflow_dispatch, que corre carga.js contra Compose en el runner.

**Hecho cuando:** BASE_URL=http://localhost:8080 k6 run perf/scripts/corta.js sale 0, y con el servicio caído sale distinto de 0.

### M5 · Publicar la imagen desde el pipeline

**Mateo** · esfuerzo M

Primera parte de cd.yml: al terminar CI en verde sobre main, construir y publicar en GHCR con el SHA, nunca solo latest. Pasar el SHA como build arg y exponerlo en /actuator/info, para poder comprobar qué versión está corriendo. Va antes de Render, porque la imagen no se puede subir a mano.

**Hecho cuando:** Un merge a main deja ghcr.io/drearsanti/pet-finder:<sha> en Packages, y al correrla /actuator/info devuelve ese SHA.

**Depende de:** A2 y A7

### M6 · Render con imagen y PostgreSQL

**Mateo** · esfuerzo M

Pasar el servicio a Existing Image desde GHCR, con su deploy hook, y crear la base PostgreSQL gratis (dura 30 días: anotar su vencimiento y compararlo con la fecha de calificación). Crear en el panel las variables de A7, S9 y S10. Pasarle a Santi los valores por privado. Si Render no permite cambiar el servicio actual, crear uno nuevo y actualizar la URL en todas partes.

**Hecho cuando:** El servicio corre la imagen de GHCR y un caso sobrevive a un reinicio.

**Depende de:** D2, S7 y M5

### M7 · cd.yml: desplegar solo

**Mateo** · esfuerzo M

Segunda parte de cd.yml: llamar al deploy hook con imgURL y esperar a que /actuator/info reporte el SHA nuevo, porque la instancia anterior sigue sana mientras Render despliega. Luego correr humo-flujo y corta.js contra la URL, con sus reportes como artefactos, y fallar si algo no cumple. Empujar un tag v* publica la imagen con esa versión. No exigirlo como check del PR.

**Hecho cuando:** Un merge a main termina con el SHA nuevo en /actuator/info sin pasos manuales, un SLO incumplido deja la corrida en rojo, y empujar v2.1.0 publica la imagen con esa versión.

**Depende de:** A2, S3, M4, M5 y M6

## Endurecer (Lun 9 a mié 11 nov)

Hito del Mié 11: funcionalidad congelada, reversión ejecutada y PR bloqueado cerrado. Último día para decidir el plan de Render.

### S8 · Quitar el N+1 y limitar la lista (pendiente 3)

**Santi** · esfuerzo M

JOIN FETCH en RepositorioReportesH2 y tope o paginación en GET /api/reportes y en "ver reportes" por voz, sin romper la interfaz. Volver a medir con k6 y actualizar perf/README.md. Esto también ayuda a cumplir el SLO en Render.

**Hecho cuando:** Con show-sql, listar hace una sola consulta; p95 de voz_listar queda cerca del de voz_registrar en perf/README.md.

### S9 · Código de gestión: núcleo (pendiente 4)

**Santi** · esfuerzo L

Al publicar se genera un código con SecureRandom (128 bits); se guarda solo su SHA-256 y se devuelve una sola vez. Resolver y cerrar lo exigen, con comparación en tiempo constante. PETFINDER_CODIGO_ADMIN permite al operador retirar casos abusivos y cubre el paso de la guía sobre el primer administrador. Incluye alta por voz y consola (la respuesta de voz también devuelve el código) y la migración. Agregar PETFINDER_CODIGO_ADMIN a .env.example y docker-compose.yml; Mateo la crea en Render.

**Hecho cuando:** Una IT espera 403 sin código, 403 con el de otro caso y 204 con el correcto o el de administrador; el código no aparece en la lista ni en el detalle.

**Depende de:** D1 y S7. Se hace con A9 y M10

### S10 · Límite de peticiones

**Santi** · esfuerzo M

Filtro con token bucket por IP (CF-Connecting-IP, porque Render está detrás de Cloudflare) y un límite global que protege el gasto en Claude: asistente con 10 por minuto por IP y 60 globales. /api/voz y /api/reportes configurables por variable de entorno, para que k6 no reciba 429. Agregar esas variables a .env.example y docker-compose.yml; Mateo las crea en Render. Fijar el límite de gasto en la consola de Anthropic.

**Hecho cuando:** Una IT espera 429 en la llamada 11 por minuto al asistente, y perf/scripts/corta.js sigue cumpliendo sus umbrales.

**Depende de:** M2

### S12 · Pull request bloqueado de evidencia

**Santi** · esfuerzo S

Rama evidencia/controles-seguridad con tres cosas falsas, nunca fusionadas: commons-text 1.9 en pom.xml; petfinder.pago.api-secret con 40 caracteres aleatorios; y un DiagnosticoController que pasa un parámetro a ProcessBuilder. No usar formas sk-ant ni ghp_: el push protection de GitHub lo rechazaría antes del pipeline. Decir en el PR que todo es falso, cerrarlo sin fusionar y enlazarlo.

**Hecho cuando:** Secretos, SAST, SCA e Imagen quedan en rojo, GitHub muestra el merge bloqueado y el PR queda Closed, no Merged.

**Depende de:** S2, S5, S6, A5 y A6

### A8 · Endurecer los workflows

**Antonio** · esfuerzo S

Fijar todas las acciones por SHA de 40 caracteres, permissions: contents: read por defecto, secretos solo en el job de despliegue, y un paso que rompa si encuentra un uses: sin SHA. Trivy tuvo un aviso crítico de cadena de suministro en marzo de 2026.

**Hecho cuando:** Una acción referenciada por tag hace fallar el paso, todos los workflows pasan con SHA, y una corrida completa, sin contar despliegue ni carga, dura menos de 10 minutos, con el enlace en devops.md.

**Depende de:** A2, A5, A6, S5, S6 y M7

### A9 · Código de gestión: web

**Antonio** · esfuerzo M

Encabezado X-Codigo-Gestion en resolver y cerrar, 403 en ManejadorErrores, y devolver el código solo en la respuesta de publicar. Actualizar corta.js para que cierre sus casos con el código que recibe; si no, k6 suma 403 como errores y el despliegue queda en rojo.

**Hecho cuando:** Una prueba de capa web y una de sistema cubren 403, 403 y 204; el código no sale en GET, y despliegue-y-verificacion sigue en verde con corta.js.

**Depende de:** S9

### A10 · Menos datos personales en casos cerrados

**Antonio** · esfuerzo S

Contacto enmascarado y ubicación aproximada en casos RESUELTO o CERRADO, también en HallazgoDTO. El caso activo sigue con el contacto completo, porque quien encuentra la mascota necesita llamar.

**Hecho cuando:** Una IT cierra un caso y GET /api/reportes/{id} devuelve el contacto con • y coordenadas de 3 decimales.

### A11 · Selenium para Cerca de ti y coincidencias (pendiente 6)

**Antonio** · esfuerzo M

Dos flujos *UIT más, con Page Objects. La ubicación se simula con el protocolo de Chrome. Pasar pruebas-ui a check obligatorio tras 3 corridas seguidas en verde.

**Hecho cuando:** ./mvnw -Pui verify corre 4 flujos en Actions.

**Depende de:** A2

### M8 · Reversión probada

**Mateo** · esfuerzo M

revertir.yml con workflow_dispatch que recibe un tag de imagen, la despliega, confirma el SHA y corre humo y corta.js. Comprobar que la imagen anterior arranca con la base ya migrada. Ejecutarla de verdad una vez y dejar el enlace en docs/devops.md.

**Hecho cuando:** Hay una ejecución de la reversión en Actions, y /actuator/info vuelve a mostrar la versión anterior.

**Depende de:** M7 y una versión anterior publicada (v2.1.0)

### M9 · Cabeceras, CSP y SRI

**Mateo** · esfuerzo M

integrity en Leaflet, mover el script en línea a js/registro-sw.js y subir la versión de sw.js. CSP, nosniff, HSTS, Permissions-Policy para micrófono y ubicación, y Referrer-Policy strict-origin-when-cross-origin (OpenStreetMap exige Referer). Pasa por la skill de diseño y se prueba en Safari.

**Hecho cuando:** curl -sI de la URL muestra content-security-policy, y -Pui verify y la voz en Safari siguen funcionando.

### M10 · Código de gestión: interfaz

**Mateo** · esfuerzo M

Mostrar el código al publicar, con opción de copiar; recordarlo en ese navegador; pedirlo en Ya apareció si no está, y manejar el 403 con un mensaje claro. Pasa por la skill de diseño.

**Hecho cuando:** -Pui verify cubre publicar y cerrar con el código, y sin código no se cierra el caso.

**Depende de:** S9 y A9

## Documentar y ensayar (Jue 12 a dom 15 nov)

Hito del Dom 15: v3.0.0 publicada y pipeline en verde en el último commit de main.

### S11 · docs/seguridad.md

**Santi** · esfuerzo M

Modelo STRIDE (T1 a T11, con borrador en el apéndice de docs/plan-corte-3.md), controles con su umbral y registro de hallazgos: corregido con enlace al commit, falso positivo o riesgo aceptado. Filas iniciales: Tomcat y Jackson (2d624c0), Guava, el ReDoS y la consola H2. Los riesgos aceptados pasan a la parte 9 de la guía.

**Hecho cuando:** Al menos 5 amenazas con las 5 columnas del enunciado y el código de gestión como mitigación de autenticación y autorización. Cada hallazgo de Actions y cada alerta de la pestaña Security tiene fila con decisión, y está el enlace al PR bloqueado.

**Depende de:** S5, S6, A5, A6 y S12

### S13 · ADR-003: seguridad del pipeline

**Santi** · esfuerzo S

Herramientas, umbrales y alternativas descartadas (Semgrep, Dependency-Check, TruffleHog…), por qué SCA usa HIGH y la imagen CRITICAL, qué se sacrificó en tiempo, y la decisión del código de gestión frente a cuentas.

**Hecho cuando:** El ADR tiene contexto, decisión, alternativas y consecuencias, y sus umbrales coinciden con los workflows.

**Depende de:** S5, S6, A5 y A6

### S14 · docs/evolucion.md

**Santi** · esfuerzo M

Los tres cortes: qué decisiones se mantuvieron, cuáles cambiaron y por qué; cada pendiente del Corte 2 y de la retroalimentación del profesor, con cómo se resolvió; y los límites del sistema final.

**Hecho cuando:** Están los 6 pendientes y los 3 temas del profesor, cada uno con enlace a su PR o a su razón.

**Depende de:** Casi todo lo demás

### S15 · Tabla de trazabilidad ampliada en el README

**Santi** · esfuerzo S

Agregar dos columnas: etapa del pipeline y umbral que lo detiene. Declarar con su razón lo que no tenga verificación automática, como la usabilidad.

**Hecho cuando:** Cada fila de la tabla tiene etapa y umbral, o "sin verificación automática" con el motivo.

**Depende de:** A2, A6 y M7

### S16 · Auditoría de quién hizo qué

**Santi** · esfuerzo M, opcional

Puerto de salida RegistroAuditoria con adaptador de log para publicar, resolver, cerrar y avistar, sin datos de contacto. Mitiga la amenaza de repudio (T4). Si no alcanza, queda como riesgo aceptado en seguridad.md.

**Hecho cuando:** Una IT ve CASO_CERRADO con el ID, y una descripción con salto de línea no crea una línea nueva.

**Depende de:** S9

### A12 · docs/devops.md

**Antonio** · esfuerzo M

Diagrama del pipeline, qué hace cada etapa, qué umbral la detiene y por qué ese valor, y el procedimiento de reversión. Mateo aporta la parte de despliegue.

**Hecho cuando:** Están el diagrama del pipeline, cada check de CI y cada job de cd.yml con su umbral y justificación, y la reversión con el enlace a su ejecución.

**Depende de:** A6, M7 y M8

### A13 · docs/implementacion.md: la guía

**Antonio** · esfuerzo L

Las 9 partes del enunciado, para alguien que no conoce el proyecto. Comandos completos con su resultado esperado, costos (Render gratis y el plan recomendado), tabla de todas las variables, copia de seguridad y restauración con pg_dump, y al menos 5 problemas con síntoma, causa y solución. La prueba una persona fuera del grupo, en un fork con cuentas nuevas de Render y GHCR, anotando cada duda. Si no se consigue, la prueba Santi siguiendo solo la guía.

**Hecho cuando:** Esa persona llega al sistema funcionando siguiendo solo la guía; están las 9 partes, cada paso con su resultado esperado, y los nombres coinciden con .env.example, docker-compose.yml y los workflows.

**Depende de:** S7, S9, S10, S11, A7, M7, M8 y M12

### A14 · Comprobación de cierre

**Antonio** · esfuerzo S

El domingo 15 contra la rúbrica: cada etapa de la tabla 2.2 es un check requerido en main; el repo es público con Actions visible; la URL y la base están vivas durante la calificación; y cada quien entregó sus coevaluaciones. Antonio queda de turno el 14 y Mateo el 15, por si una vulnerabilidad nueva pone main en rojo.

**Hecho cuando:** gh api repos/DrearSanti/pet-finder-dyas/rules/branches/main lista todos los checks, y CI y CD están en verde sobre el último commit de main.

**Depende de:** S2, M7 y M11

### M11 · Versiones

**Mateo** · esfuerzo S

CHANGELOG.md y GitHub Releases para todas las versiones: v2.0.0 en el cierre del Corte 2 (c6f2404), sin imagen porque es anterior al pipeline; v2.1.0, ya publicada para la reversión; y v3.0.0 al final.

**Hecho cuando:** git tag -l 'v*' lista las versiones, Releases las describe y, desde v2.1.0, la imagen de GHCR lleva esa versión.

**Depende de:** M7

### M12 · ADR-004, vista de despliegue y arquitectura.md

**Mateo** · esfuerzo M

ADR de despliegue con alternativas y consecuencias. Diagrama C4 de despliegue que coincida con lo desplegado. Actualizar docs/arquitectura.md.

**Hecho cuando:** El ADR trae alternativas y consecuencias, y el diagrama nombra los mismos servicios, imagen y base que usa cd.yml.

**Depende de:** M7

### M13 · README final

**Mateo** · esfuerzo S

Badge del pipeline, URL desplegada, cómo levantar con docker compose desde cero y cómo corre el pipeline.

**Hecho cuando:** Alguien sigue solo el README en un clon limpio y llega a http://localhost:8080.

**Depende de:** A2, A3 y M3

### M14 · Bono DAST con ZAP (+5)

**Mateo** · esfuerzo S, opcional

Paso zaproxy/action-baseline en cd.yml, después de la prueba de humo, con reglas en .zap/rules.tsv y reporte como artefacto. Cada alerta se registra en seguridad.md. M9 elimina casi todo lo que encontraría.

**Hecho cuando:** La corrida de CD muestra ZAP contra la URL, y cada alerta tiene fila con decisión.

**Depende de:** M7 y M9

### M15 · Presentación final

**Mateo** · esfuerzo M

Coordinar el material y el guion de 15 minutos con las 5 partes. Preparar la demo: un PR pequeño que pase y quede desplegado, y otro que se bloquee. Se lanza al inicio y se revisa al final. Ensayo con los tres.

**Hecho cuando:** Ensayo cronometrado, con los dos PR de demo listos.

**Depende de:** Todo lo demás

### B1 · Bono de observabilidad (+5)

**Quien termine primero** · esfuerzo L, opcional

Micrometer con Prometheus y un tablero de Grafana con p95, tasa de error y throughput ligados al SLO, en observabilidad/ y en un perfil de Compose. Lo toma quien termine primero, y solo con lo obligatorio en verde. Exponer las métricas solo en la red interna.

**Hecho cuando:** El tablero muestra las tres métricas con la carga corriendo.

**Depende de:** A3

## Cómo estamos frente a la rúbrica (4 de noviembre)

| Criterio | Pts | Hoy | Qué pasa | Tareas |
|---|---|---|---|---|
| Contenerización | 5 | Bueno | Dockerfile bien, pero cada reinicio borra los datos. Sin ejecutar: Docker no respondía en el Mac. | A3, A4, S7, M13 |
| Integración continua | 15 | Insuficiente | 0 workflows, main sin proteger, 0 de 32 PR aprobados y sin umbral de cobertura. Las pruebas pasan: 251 en menos de 30 s. | S1, S2, A1, A2, A6, A8, A14 |
| Arquitectura verificada | 15 | Insuficiente | Las 5 reglas existen, pero no corren en un pipeline; una deja por fuera la consola. El SLO solo se mide a mano. | S4, A2, M4, M7, S15 |
| DevSecOps | 20 | Insuficiente | Ningún control automático ni modelo de amenazas. Bueno: no apareció ningún secreto en el historial. | S5, S6, S10, S11, S12, S13, A5, A6, A7, A10, M1, M2, M9 |
| Despliegue continuo | 15 | Insuficiente | Render construye desde el repo, sin job de despliegue, tags, prueba de humo ni reversión. | S3, M5, M6, M7, M8, M11 |
| Documentación | 10 | Básico | Lo del Corte 2 está. Faltan devops.md, seguridad.md, evolucion.md, 2 ADR y la vista de despliegue. | S11, S13, S14, S15, A12, M3, M12, M13 |
| Guía de implementación | 10 | No existe | Depende de que el despliegue y la persistencia ya estén listos. | A13 |
| Presentación (compañeros) | 10 | Por preparar | Demo de un cambio desplegado y otro bloqueado, con el pipeline a la vista. | M15 y los tres |

## Pendientes que hay que cerrar

Se registran en `docs/evolucion.md`.

| Origen | Pendiente | Cómo se cierra |
|---|---|---|
| Corte 2, punto 1 | Pipeline de CI/CD con DevSecOps | A2, A6, S5, S6, M7 |
| Corte 2, punto 2 | PostgreSQL con migraciones | S7 |
| Corte 2, punto 3 | Paginación y JOIN FETCH | S8 |
| Corte 2, punto 4 | Cuentas y permisos | S9, A9, M10 (código de gestión) |
| Corte 2, punto 5 | Fotos | Fuera de este corte: se justifica en evolucion.md (D5) |
| Corte 2, punto 6 | Selenium para Cerca de ti y coincidencias | A11 |
| Profesor | Seguridad, incluido si la PWA permite meter un virus | S11, M9, A8, M14 |
| Profesor | Fotos de las mascotas | Trabajo futuro documentado en evolucion.md (D5) |
| Profesor | Reconocimiento de parecidos con IA | Trabajo futuro documentado en evolucion.md (D5) |

## Riesgos

| Riesgo | Consecuencia | Cómo evitarlo |
|---|---|---|
| Main en rojo en el último commit | Los criterios 2, 3 y 5 (45 puntos) quedan en máximo Básico. | Desde el viernes 13 solo entran documentos, con revisión y pipeline completo; comprobar en A14 que el último commit terminó en verde. |
| Una vulnerabilidad nueva aparece a última hora | La base de Trivy cambia a diario, sin que nadie toque el código, y puede poner main en rojo. | Corrida diaria programada (S5). Antonio de turno el 14 y Mateo el 15. |
| Un secreto real entra al repo | DevSecOps pasa directo a Insuficiente (hasta 20 puntos) y hay que rotarlo. | S5 es de lo primero que entra. Revisar git diff --cached antes de cada commit. |
| Render gratis no cumple el SLO | El despliegue queda en rojo en cada merge. | Calentar antes de medir, S8 para aligerar la lista, y plan Starter como salida (D2). |
| Santi no está cuando se necesita | Sin él no hay regla de protección ni secretos, y S2, S3 y M7 se detienen. | Agendar con él 30 minutos el jueves 5, justo después de la primera corrida de ci.yml. |
| El límite de peticiones rompe la prueba de carga | k6 manda todo desde una IP y recibiría 429. | Límite estricto solo en el asistente; /api/voz y /api/reportes configurables. |

## Lo que no se pudo verificar

- docker compose up de principio a fin: Docker no respondió en la auditoría. Lo confirma A3.
- El contenido de .env.example: una regla del proyecto bloquea su lectura. Lo confirma A3.
- Gitleaks real: la búsqueda de secretos usó sus reglas replicadas sobre todo el historial. La primera corrida en Actions es la confirmación.
- Los tiempos en GitHub Actions: en el Mac el build y las pruebas tardan menos de 30 s, y se estiman 6 a 8 minutos en paralelo con CodeQL e imagen.
- Render con PostgreSQL y con escrituras: solo se midieron lecturas sobre H2.
- La sección de documentos y pendientes se revisó sin el agente que se cortó: se armó leyendo el repo directamente.

## Apéndice A: borrador del modelo de amenazas

Salió de la auditoría del 4 de noviembre y parte de ejecutar el sistema. S11 lo pasa a `docs/seguridad.md`, agrega la mitigación y la verificación finales de cada una, y confirma cada medición. Los números T1 a T11 son de amenazas; no confundir con las tareas.

### T1 · Elevación de privilegios · navegador -> POST /api/reportes/{id}/resolver y /cerrar

Amenaza: cualquiera cierra el caso de otra familia y la mascota desaparece de la lista y del mapa. VERIFICADO en local: sin credenciales responde 204; con Origin ajeno y text/plain (petición simple, sin preflight) también 204, así que cualquier página web puede dispararlo desde el navegador de una víctima. ReporteController.java:61-71, ServicioReportes.java:71-81. Mitigación: código de gestión por caso en cabecera X-Codigo-Gestion (autenticación por posesión y autorización por caso); la cabecera propia obliga a preflight, que hoy no se concede. Verificación: IT que espera 403 sin código y con el código de otro caso, y 204 con el correcto; la prueba de humo tras desplegar espera 403 sin cabecera.

### T2 · Suplantación · navegador -> POST /api/reportes y POST /api/reportes/{id}/avistamientos ('La tengo yo')

Amenaza: publicar con el celular de un tercero para acosarlo, o avisar 'La tengo yo' para estafar a la familia con una recompensa. VERIFICADO: medioContacto es texto libre sin validar (SolicitudReporteDTO.java:20-30, AvistamientoDTO.java); HallazgoDTO publica ese contacto en el detalle (ReporteDTO.java:79-92). Mitigación: formato y longitud de contacto, límite de publicaciones por IP y código administrador para retirar casos abusivos. Lo demás queda como riesgo aceptado, porque verificar identidad exigiría SMS o correo. Verificación: IT que espera 400 con contacto inválido y 429 al superar el límite.

### T3 · Manipulación · CDN y navegador -> index.html y Leaflet

Amenaza: un CDN comprometido ejecuta JS en el origen de la app, lee los contactos que se escriben y dispara la API. VERIFICADO: index.html:19 y :212 sin integrity; index.html:18 fuentes de Google; script en línea en :217. VERIFICADO en vivo: la respuesta no trae CSP, HSTS, X-Frame-Options, X-Content-Type-Options, Referrer-Policy ni Permissions-Policy. Mitigación: SRI, CSP estricta y cabeceras (ver tarea, con hashes ya calculados). Las fuentes de Google no admiten SRI porque el CSS cambia según el navegador: acotar con CSP y aceptar el riesgo, o alojarlas en el proyecto. Verificación: contiene.sh sobre index.html con el hash, humo que exige content-security-policy, prueba UIT sin violaciones de CSP, ZAP 10038 y 90003.

### T4 · Repudio · app Spring Boot -> cambios de estado y registro

Amenaza: nadie puede saber quién cerró un caso, y se pueden falsificar líneas de log. VERIFICADO en local: tras cerrar PF-002 hay 0 líneas de log que lo mencionen. AuditoriaObserver.java:19 y :34 solo oye avistamientos y guarda en un ArrayList en memoria, sin límite y sin sincronizar. VERIFICADO: un avistamiento con salto de línea inyectó una línea falsa 'caso PF-001 cerrado por el administrador' en el log (AlertaPropietarioObserver.java:24-25, System.out sin sanear). Mitigación: puerto de salida de auditoría con evento, id, huella del código, IP y hora, más saneo de CR/LF. Verificación: IT con OutputCaptureExtension que exige la línea CASO_CERRADO y una prueba de que un salto de línea no crea otra línea.

### T5 · Divulgación de información · navegador -> GET /api/reportes/{id}

Amenaza: recolección masiva de celulares y ubicaciones exactas. VERIFICADO en vivo en Render: PF-001 a PF-004 responden 200 con contacto sin enmascarar y coordenadas de 4 decimales, sin autenticación, mientras la lista sí enmascara (ReporteDTO.java:100-104, :160-161). VERIFICADO en local: un caso RESUELTO o CERRADO conserva el contacto completo, y los IDs consecutivos (GeneradorIdReportes.java:28) permiten llegar a esos casos, que la lista ya no muestra. Ojo: pasar a UUID no arregla los casos activos, porque GET /api/reportes entrega todos sus IDs. Mitigación: enmascarar contacto y ubicación en casos no activos, y límite por IP en el detalle. Verificación: IT que espera contacto con '•' en un caso cerrado y 429 al superar el límite.

### T6 · Denegación de servicio · navegador -> POST /api/asistente/turno -> ExtractorRegex

Amenaza: una sola petición sin autenticar ocupa un núcleo durante minutos. VERIFICADO en local (Mac, -Xmx384m, sin clave): texto de 'a' de 10 KB 0,23 s; 25 KB 1,06 s; 50 KB 4,74 s; 100 KB 17,4 s; 200 KB 69,6 s; 400 KB más de 90 s. Es cuadrático. jstack lo ubica en ExtractorRegex.java:89, expresión CORREO de la línea 40. El proceso seguía en 200 % de CPU después de que el cliente se fue. /api/voz no sufre porque InterpreteComandoVoz.java:30 corta en 200, pero ese límite no protege a ExtractorRegex. También se alcanza con clave de Claude: una frase sin datos devuelve vacío (ExtractorClaude.java:195) y el turno sigue con regex (ServicioAsistente.java:39-46). VERIFICADO: 4 cuerpos paralelos de 100 MB produjeron OutOfMemoryError con 384 MB (6 en el log; el proceso sobrevivió). Campos largos dan 500 en vez de 400. Mitigación: 1000 caracteres máximo, CORREO acotado, filtro de 64 KB con 413, longitudes por campo. Verificación: @Timeout(1) con 100 KB, IT de 400 y 413, CodeQL java/polynomial-redos.

### T7 · Denegación de servicio económica · app Spring Boot -> API de Claude

Amenaza: agotar el presupuesto de Anthropic y, de paso, dejar a todos con la regex. VERIFICADO: ExtractorClaude.java:100-104 y :149 mandan la frase completa, y el borrador también va completo (:134-146). VERIFICADO: 3000 POST con concurrencia 30 dieron 3000 respuestas 200, ningún 429. El tope cuenta llamadas, no tokens, y vive en memoria (:78-79, :119-130), así que vuelve a cero al reiniciar; Render reinicia al despertar tras 15 minutos dormido, según docs/evidencias/despliegue-render.md. Cuerpos de 50 MB se aceptan; el corte de Jackson aparece cerca de 100 MB. SUPUESTO con claude-sonnet-5 a 2 USD/M entrada y 10 USD/M salida y 3,5 caracteres por token: 200 KB cuestan unos 0,12 USD por llamada, 12 USD por cada tope de 100 y hasta unos 1000 USD al día si se aprovecha el reinicio (85 ciclos). Con el límite de 1000 caracteres baja a unos 0,012 USD por llamada. Ya mitigado: max_tokens 1024, timeout 10 s, 0 reintentos, respaldo regex. Mitigación pendiente: límite de texto, 10 turnos por minuto por IP más un límite global, y límite de gasto en la consola de Anthropic. Verificación: prueba de que el doble de Claude no recibe llamada con más de 1000 caracteres; IT del 429 en la llamada 11.

### T8 · Manipulación · frase de la persona -> ExtractorClaude (inyección de prompt)

VERIFICADO bien: el modelo no tiene herramientas; ServicioAsistente no recibe GestionReportes, así que no puede publicar; la frase viaja como dato JSON y el prompt la declara dato (ExtractorClaude.java:46-68); hay esquema con additionalProperties false (ClienteModeloAnthropic.java:100-116); la salida se valida (:169-196); ExtractorClaudeTest.java:334-339 prueba el escape. Riesgo residual, SUPUESTO porque no llamé a Claude: pedir en el campo descripcion que el modelo responda cualquier cosa lo convierte en un LLM gratis de hasta 1024 tokens, o en un eco del prompt, porque los textos de salida no tienen tope. Además, celulares y correos llegan a Anthropic. Mitigación: máximo por campo de salida (500 caracteres) y aviso de tratamiento de datos. Verificación: doble que devuelve descripcion de 5000 caracteres y espera rechazo; regla ArchUnit nueva que prohíba a ServicioAsistente depender de GestionReportes (hoy no existe en ReglasArquitecturaTest).

### T9 · Elevación de privilegios · internet -> /h2-console de la app Spring Boot

VERIFICADO en vivo: en Render /h2-console/ responde 200 con 'remote connections (webAllowOthers) are disabled'; una cabecera X-Forwarded-For 127.0.0.1 no cambia eso, así que hoy no es explotable. Compose la apaga (docker-compose.yml:21), Render no (application.properties:17). VERIFICADO en local, la trampa: con server.forward-headers-strategy=framework, que es lo que alguien pondría para limitar por IP, las cabeceras X-Forwarded-For: 127.0.0.1 o Forwarded: for=127.0.0.1 desde otra máquina hacen desaparecer el rechazo y responde otra página (938 bytes en vez de 556). No intenté iniciar sesión; el usuario sa no tiene clave, así que quedaría acceso a la base. Mitigación: spring.h2.console.enabled=false por defecto, encendida solo con perfil local. Verificación: IT y prueba de humo que esperan 404 en /h2-console/.

### T10 · Manipulación y denegación · app Spring Boot -> PostgreSQL

Amenaza: reiniciar el contenedor borra todos los casos, y si se relaja, los pisa. VERIFICADO: docker-compose.yml:19 usa ddl-auto create y su comentario :17-18 explica que el generador PF-001 vive en memoria y que con update pisaría reportes. La app se conecta con el mismo usuario dueño de la base (:15-16, :35-36). Mitigación: Flyway con ddl-auto validate, ID desde secuencia de la base, usuario de aplicación sin privilegios de dueño (pendiente 2 de docs/arquitectura.md:150). Verificación: IT con PostgreSQL que crea un reporte, reinicia el contexto y lo encuentra, y crea otro sin repetir ID.

### T11 · Manipulación · pipeline GitHub Actions -> registro y Render, y clave de CARTO

Amenaza: una acción de terceros comprometida roba el hook de despliegue o publica una imagen alterada. VERIFICADO: la propia herramienta recomendada tuvo el aviso GHSA-69fq-xp46-6x23 (crítico, 2026-03-21, 'Trivy ecosystem supply chain temporarily compromised'). VERIFICADO: hoy Mateo tiene push sin protección en main. Y la clave de CARTO, si sigue en la imagen, queda legible en el registro. Mitigación: fijar acciones por SHA de 40 caracteres, permissions: contents: read por defecto, nunca pull_request_target, secretos solo en el job de despliegue con GitHub Environment, clave de CARTO en tiempo de ejecución. Verificación: paso que falla si grep -E 'uses: +[^ ]+@' .github/workflows/* encuentra algo sin @[0-9a-f]{40}; paso que falla si el jar de la imagen contiene config-local.js.

## Apéndice B: borrador de `perf/scripts/corta.js`

Probado en local: 2526 peticiones, 0 % de errores, p95 de 5 a 6 ms y 0 iteraciones perdidas. Falta medirlo contra Render. Cuando entre A9, la limpieza debe mandar el código en `X-Codigo-Gestion`.

```js
import http from 'k6/http';
import { check } from 'k6';

// Mismo SLO y mismas operaciones de carga.js, en menos de 2 minutos.
// Modelo abierto: fija 33 peticiones por segundo (11 iteraciones de 3 peticiones). Si el sistema no da
// abasto, se pierden iteraciones y el umbral de throughput falla; no depende de cuántos VUs se elijan.
// Cierra cada caso que crea para no dejar reportes de prueba en el mapa del entorno desplegado.
const BASE = __ENV.BASE_URL;
const CABECERAS = { 'Content-Type': 'application/json' };
const TASA = Number(__ENV.ITERACIONES_POR_SEGUNDO || 11);

export const options = {
  scenarios: {
    calentamiento: {
      executor: 'constant-arrival-rate', rate: Math.ceil(TASA / 2), timeUnit: '1s',
      duration: __ENV.CALENTAMIENTO || '30s', preAllocatedVUs: 10, maxVUs: 40,
    },
    medicion: {
      executor: 'constant-arrival-rate', rate: TASA, timeUnit: '1s',
      duration: __ENV.MEDICION || '60s', startTime: __ENV.CALENTAMIENTO || '30s',
      preAllocatedVUs: 20, maxVUs: 60,
    },
  },
  thresholds: {
    'http_req_duration{scenario:medicion}': ['p(95)<500'],
    'http_req_failed{scenario:medicion}': ['rate<0.01'],
    'http_reqs{scenario:medicion}': ['count>=1800'], // 30 req/s x 60 s de medición
    'http_req_duration{scenario:medicion,name:voz_registrar}': ['p(95)<500'],
    'http_req_duration{scenario:medicion,name:voz_listar}': ['p(95)<500'],
    'dropped_iterations{scenario:medicion}': ['count==0'],
  },
};

export default function () {
  const registro = http.post(
    `${BASE}/api/voz`,
    JSON.stringify({
      texto: `perdí un perro llamado Max en Chía ${__VU}-${__ITER}`,
      nombreContacto: 'Prueba de carga',
      medioContacto: '3001112233',
    }),
    { headers: CABECERAS, tags: { name: 'voz_registrar' } },
  );
  check(registro, { 'voz_registrar responde 201': (r) => r.status === 201 });

  const listado = http.post(`${BASE}/api/voz`, JSON.stringify({ texto: 'ver reportes' }), {
    headers: CABECERAS, tags: { name: 'voz_listar' },
  });
  check(listado, { 'voz_listar responde 200': (r) => r.status === 200 });

  // Limpieza: la tercera petición del ciclo. También cuenta para el SLO.
  if (registro.status === 201) {
    const id = registro.json('reportes.0.id');
    const cierre = http.post(`${BASE}/api/reportes/${id}/cerrar`, null, { tags: { name: 'limpieza' } });
    check(cierre, { 'limpieza responde 204': (r) => r.status === 204 });
  }
}
```
