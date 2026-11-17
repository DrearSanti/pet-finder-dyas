# Seguridad de Pet Finder

Modelo de amenazas, controles del pipeline con su umbral y registro de cada hallazgo con su decisión. Parte del borrador del Apéndice A de [`plan-corte-3.md`](plan-corte-3.md), que salió de la auditoría del 4 de noviembre de 2026, y lo lleva a la mitigación y la verificación finales.

**Estado de este documento (16 de noviembre de 2026, cierre del Corte 3).** Los cuatro controles corren en el pipeline como checks obligatorios de `main`, incluido el escaneo de la imagen (A6, [#74](https://github.com/DrearSanti/pet-finder-dyas/pull/74)). Lo único que no se conectó es el código de gestión en los controladores web (A9), que se recortó y queda como riesgo aceptado (sección 4).

## 1. Modelo de amenazas

Sobre el diagrama de contenedores del Corte 2 ([`diagramas/c4.md`](diagramas/c4.md)): navegador → app Spring Boot (API REST y página) → base de datos y API de Claude, con el pipeline de GitHub Actions que publica en GitHub Container Registry y despliega en Render.

| # | Amenaza | STRIDE | Componente o flujo | Mitigación | Cómo se verifica |
|---|---|---|---|---|---|
| T1 | Cualquiera cierra el caso de otra familia y la mascota desaparece del mapa. Verificado en local antes del Corte 3: sin credenciales, `POST /api/reportes/{id}/cerrar` respondía 204, también desde otra página web | Elevación de privilegios | Navegador → `POST /api/reportes/{id}/resolver` y `/cerrar` | **Código de gestión** por caso: **autenticación por posesión** (solo quien publicó recibe el código, una sola vez) y **autorización por caso** (el código de un caso no sirve para otro). En la base solo se guarda su huella SHA-256 y se compara en tiempo constante. Un código de administrador (`PETFINDER_CODIGO_ADMIN`) cierra casos abusivos o viejos; vacío nunca coincide. La cabecera propia `X-Codigo-Gestion` obliga a preflight, que no se concede a otros orígenes | `ServicioReportesCodigoTest` (sin código, código de otro caso, correcto, administrador, administrador vacío) y `CodigoGestionH2IT` (la fila tiene la huella y ninguna columna el código), integrados con S9 ([#48](https://github.com/DrearSanti/pet-finder-dyas/pull/48)). Mientras A9 no pase `ReporteController` al código, los endpoints web siguen aceptando cerrar sin código: la mitigación está en el núcleo y la voz, y su conexión a la API web queda pendiente (sección 4) |
| T2 | Publicar con el celular de un tercero, o avisar "La tengo yo" para estafar a la familia | Suplantación | Navegador → `POST /api/reportes` y `POST /api/reportes/{id}/avistamientos` | Longitud máxima por campo (M1), límite de publicaciones por IP (S10) y código de administrador para retirar casos abusivos (S9). Verificar la identidad queda como riesgo aceptado (sección 4) | `LimitesEntradaSistemaIT.campoLargoResponde400` (M1); `LimitePeticionesSistemaIT` (S10, [#47](https://github.com/DrearSanti/pet-finder-dyas/pull/47)) |
| T3 | Un CDN comprometido ejecuta JavaScript en el origen de la app | Manipulación | CDN y navegador → `index.html` y Leaflet | Integridad (SRI) en Leaflet, política de contenido (CSP) sin `unsafe-inline`, `X-Content-Type-Options`, `Strict-Transport-Security`, `Referrer-Policy` y `Permissions-Policy` (M9). El registro del service worker salió del HTML a `js/registro-sw.js` | `FiltroCabecerasSeguridadTest` y `CabecerasSeguridadSistemaIT` (M9); en Render, `curl -I` muestra `content-security-policy` y `strict-transport-security`; `FlujosPrincipalesUIT` pasa en Chrome con la CSP activa |
| T4 | Nadie puede saber quién cerró un caso, y un salto de línea en un avistamiento inyecta líneas falsas en el log | Repudio | App Spring Boot → cambios de estado y registro | Auditoría de cierres con evento, ID y huella del código, y saneo de CR/LF (tarea S16, opcional) | Pendiente: S16 se recorta si no alcanza el tiempo; queda como riesgo aceptado (sección 4) |
| T5 | Recolección masiva de celulares y ubicaciones exactas recorriendo el detalle de cada caso | Divulgación de información | Navegador → `GET /api/reportes/{id}` | En la lista el contacto va enmascarado y las coordenadas a 3 decimales; límite de 3000 peticiones por minuto por IP en `/api/reportes` (S10) | `FlujoReportePerdidaSistemaIT.listaEnmascaraYDetalleNo`; `FiltroLimitePeticionesTest` (S10). El detalle sigue mostrando el contacto completo: riesgo aceptado (sección 4) |
| T6 | Una sola petición sin autenticar ocupa un núcleo durante minutos (100 KB al asistente tardaban 17,4 s; 4 cuerpos de 100 MB agotaron la memoria) | Denegación de servicio | Navegador → `POST /api/asistente/turno` → `ExtractorRegex` | Frase del asistente de 1000 caracteres como máximo, expresión `CORREO` acotada, filtro de 64 KB que responde 413 y longitud por campo (M1) | `ExtractorRegexTest.textoLargoNoTrabaElExtractor` (100 KB en menos de 1 s); `LimitesEntradaSistemaIT` (400 y 413); CodeQL `java/polynomial-redos` sin hallazgos |
| T7 | Agotar el presupuesto de Anthropic con miles de turnos del asistente | Denegación de servicio | App Spring Boot → API de Claude | Ya existían `max_tokens` 1024, timeout de 10 s, 0 reintentos y respaldo con reglas. El Corte 3 agrega el tope de 1000 caracteres (M1) y 10 turnos por minuto por IP más 60 por minuto en total (S10) | `ServicioAsistenteTest` (M1); `LimitePeticionesSistemaIT`: el turno 11 responde 429 con `Retry-After: 60` (S10, [#47](https://github.com/DrearSanti/pet-finder-dyas/pull/47)). El límite de gasto en la consola de Anthropic lo fija quien tenga la cuenta de la clave |
| T8 | Inyección de prompt para que el asistente publique o actúe por su cuenta | Manipulación | Frase de la persona → `ExtractorClaude` | El modelo no tiene herramientas; la frase viaja como dato JSON; la salida tiene esquema y se valida. Quien implementa `AsistenteReportes` no tiene cómo llegar a `GestionReportes` ni al repositorio: publicar siempre es un `POST /api/reportes` que dispara la persona | Regla de ArchUnit `elAsistenteNuncaPublica` en `ReglasArquitecturaTest` (S4, check `reglas-arquitectura`); `ExtractorClaudeTest` |
| T9 | Acceso a la base por la consola web de H2 | Elevación de privilegios | Internet → `/h2-console` | Consola apagada por defecto; solo se prende con el perfil `local`. No se usa `server.forward-headers-strategy`, que dejaba pasar `X-Forwarded-For: 127.0.0.1` | `ConsolaH2SistemaIT` y `ConsolaH2PerfilLocalIT` (M2); en Render `/h2-console` responde 404 |
| T10 | Reiniciar el contenedor borraba todos los casos, y con `ddl-auto=update` el siguiente POST pisaba uno existente | Manipulación y denegación | App Spring Boot → PostgreSQL | Flyway crea el esquema y Hibernate solo lo valida; el generador de IDs arranca después del último guardado (S7) | `ReinicioPersistenciaIT` (S7). En Compose y en Render: el caso PF-005 sobrevivió al redespliegue y el siguiente fue PF-006 |
| T11 | Una acción de terceros comprometida roba el deploy hook o publica una imagen alterada | Manipulación | Pipeline de GitHub Actions → registro y Render | Acciones fijadas por SHA de 40 caracteres; `permissions: contents: read` por defecto; nunca `pull_request_target`; el deploy hook vive solo en el environment `produccion`, que solo pueden usar la rama `main` y los tags `v*` (S3); `main` protegida con 1 aprobación y sin force push (S2) | Check **acciones-fijadas** en `ci.yml` (A8, [#77](https://github.com/DrearSanti/pet-finder-dyas/pull/77)): falla si alguna acción de un workflow no está fijada por un SHA de 40 caracteres |

## 2. Controles del pipeline

| Control | Herramienta y versión | Umbral (detiene el pipeline) | Workflow y check | Artefacto |
|---|---|---|---|---|
| Secretos | Gitleaks 8.30.1, binario validado con su SHA-256 | **Cero secretos**, en todo el historial (`fetch-depth: 0`) | `seguridad.yml`, check **Secretos** | `reporte-secretos` (SARIF, con valores ocultos por `--redact`) |
| SAST | CodeQL v4.39.2 (`security-extended`), Java y JavaScript | **Ningún hallazgo con `security-severity` de 7,0 o más sin tratar**: lo aplica `scripts/umbral-sarif.py`, porque CodeQL solo reporta | `seguridad.yml`, checks **SAST (java-kotlin)** y **SAST (javascript-typescript)** | `sarif-java-kotlin` y `sarif-javascript-typescript`; resultados en la pestaña Security |
| SCA | Trivy v0.76.1 (`scan-type: fs`) en modo offline (`TRIVY_OFFLINE_SCAN`): resuelve las dependencias con `pom.xml` y la caché local de Maven, sin pedirle nada a Maven Central; la base de vulnerabilidades sí se descarga | **HIGH o CRITICAL con corrección disponible** (`ignore-unfixed: true`, `exit-code: '1'`) | `seguridad.yml`, check **SCA** | `reporte-sca` |
| Imagen | Trivy v0.76.1 sobre la imagen construida en el pipeline | **CRITICAL con corrección disponible**; los HIGH se reportan sin detener | `ci.yml`, check **imagen-y-escaneo** (A6, [#74](https://github.com/DrearSanti/pet-finder-dyas/pull/74)) | `trivy-imagen.txt` |

Las herramientas, los umbrales y por qué se eligieron están en [ADR-003](adr/ADR-003-seguridad-del-pipeline.md). Los controles de `seguridad.yml` corren en cada push, en cada PR a `main` y una vez al día (11:00 UTC), porque la base de vulnerabilidades cambia aunque nadie toque el código.

## 3. Registro de hallazgos

Cada hallazgo tiene una de tres decisiones: **Corregido** (con el commit), **Falso positivo** (con la justificación técnica) o **Riesgo aceptado** (con la justificación y lo que haría falta para corregirlo).

| ID | Fuente | Hallazgo | Severidad | Decisión |
|---|---|---|---|---|
| H-01 | Trivy (SCA), Corte 2 | Tomcat y Jackson con vulnerabilidades conocidas | Alta | **Corregido** en `2d624c0` (PR #32) |
| H-02 | Trivy (SCA) | Guava CVE-2026-102554, solo en pruebas a través de Selenium | Alta | **Corregido** en `6b58bee` (A5, [#56](https://github.com/DrearSanti/pet-finder-dyas/pull/56)): `pom.xml` fuerza Guava 33.7.2-jre |
| H-03 | Auditoría manual | ReDoS cuadrático en `ExtractorRegex`: 100 KB tardaban 17,4 s | Alta | **Corregido** en `20fce2b` (M1, PR #35) |
| H-04 | Auditoría manual | La consola de H2 respondía 200 en Render | Alta | **Corregido** en `e9f3ce9` (M2, PR #36) |
| H-05 | Auditoría manual | La página no tenía CSP, HSTS ni SRI | Media | **Corregido** en `818096b` (M9, PR #38) |
| H-06 | Auditoría manual | Cualquiera cerraba el caso de otra familia (T1) | Alta | Núcleo **corregido** en S9 ([#48](https://github.com/DrearSanti/pet-finder-dyas/pull/48)); la API web sigue sin pedir el código hasta A9 |
| H-07 | Auditoría manual | 3000 turnos seguidos al asistente sin ningún 429 (T7) | Alta | **Corregido** en S10 ([#47](https://github.com/DrearSanti/pet-finder-dyas/pull/47)): 10 turnos por minuto por IP y 60 en total; el 11 responde 429 |
| H-08 | Auditoría manual | Reiniciar el contenedor borraba los casos (T10) | Alta | **Corregido** en S7 ([#43](https://github.com/DrearSanti/pet-finder-dyas/pull/43)) |
| H-09 | Actions (SCA) | El check SCA salía en rojo por un 429 de Maven Central, sin ninguna vulnerabilidad | — | **Falso positivo** del entorno, no del código. Descargar las dependencias antes de escanear ([#56](https://github.com/DrearSanti/pet-finder-dyas/pull/56)) no alcanzó: Trivy seguía pidiendo a Maven Central poms que Maven no usa. Lo corrigió el modo offline de Trivy (`TRIVY_OFFLINE_SCAN`, [#73](https://github.com/DrearSanti/pet-finder-dyas/pull/73)), que encuentra los mismos 115 paquetes sin consultar Maven Central |

**Pestaña Security y Actions (16 de noviembre de 2026):** 0 alertas de code scanning, 0 de Dependabot y 0 de secret scanning. No hay excepciones: `.github/codeql/excepciones.txt` está vacío y no existen `.gitleaksignore` ni `.gitleaks.toml`.

## 4. Riesgos aceptados

Pasan a la parte 9 de la guía de implementación (A13).

| Riesgo | Por qué se acepta | Qué haría falta para corregirlo |
|---|---|---|
| La API web todavía cierra casos sin código (T1) | El código de gestión ya funciona en el núcleo, la voz y la consola; conectar los controladores web (A9) cambiaba código cuando la funcionalidad ya estaba congelada | A9: `ReporteController` pasa a `publicar`, `resolver(id, codigo)` y `cerrar(id, codigo)` con la cabecera `X-Codigo-Gestion`, y `CodigoGestionInvalidoException` responde 403 |
| No se verifica la identidad de quien publica (T2) | Exigiría SMS o correo, fuera del alcance del curso | Verificar el celular o el correo antes de publicar |
| El detalle de un caso muestra el contacto completo (T5) | La familia necesita que quien encontró la mascota la llame; el límite por IP frena la recolección masiva | Mostrar el contacto solo a quien tenga el código, o un canal de mensajes dentro de la app |
| El generador de IDs vive en el proceso (S7) | Con una sola instancia basta; dos instancias contra la misma base repetirían números | Una secuencia de la base de datos |
| Límites de 3000 peticiones por minuto en `/api/voz` y `/api/reportes` (S10) | La prueba corta del despliegue manda unas 2000 por minuto desde una sola IP | Que la prueba corta use otra IP o una cabecera de servicio, y bajar los límites |
| `CF-Connecting-IP` se puede falsificar fuera de Cloudflare (S10) | En Render la escribe Cloudflare; por eso el asistente tiene además un límite global | Aceptar esa cabecera solo desde los rangos de IP de Cloudflare |
| Las frases y contactos que se dictan al asistente llegan a Anthropic (T8) | Es lo que permite entender la frase; sin clave, la app usa solo reglas | Aviso de tratamiento de datos en la interfaz y quitar el contacto antes de enviar la frase |
| La app se conecta a la base con el usuario dueño del esquema (T10) | El plan gratuito de Render crea un solo usuario | Un usuario de aplicación sin permisos sobre el esquema |
| Sin auditoría de quién cerró un caso (T4) | S16 es opcional y se recorta si no alcanza el tiempo | Puerto de salida de auditoría con evento, ID, huella del código y hora, y saneo de CR/LF |

## 5. Evidencia

| Control | Corrida en verde | Corrida en rojo a propósito |
|---|---|---|
| Secretos | [main, 61 commits sin secretos](https://github.com/DrearSanti/pet-finder-dyas/actions/runs/43045206648) | [llave falsa en `prueba/gitleaks`](https://github.com/DrearSanti/pet-finder-dyas/actions/runs/43045628592) |
| SAST | [PR #41, 0 hallazgos](https://github.com/DrearSanti/pet-finder-dyas/pull/41) | [`java/command-line-injection` de 9,8 en `prueba/sast`](https://github.com/DrearSanti/pet-finder-dyas/actions/runs/43050677998) |
| Reglas de arquitectura | `ReglasArquitecturaTest`, 9 reglas en verde en cada PR | [`lasEntradasSoloHablanConPuertos` en el PR #78](https://github.com/DrearSanti/pet-finder-dyas/actions/runs/43858633435) |
| PR bloqueado | — | [PR de evidencia #78](https://github.com/DrearSanti/pet-finder-dyas/pull/78), cerrado sin fusionar ([corrida de Seguridad](https://github.com/DrearSanti/pet-finder-dyas/actions/runs/43858633385), [corrida de CI](https://github.com/DrearSanti/pet-finder-dyas/actions/runs/43858633435)) |

### El PR bloqueado

El [PR #78](https://github.com/DrearSanti/pet-finder-dyas/pull/78) introduce a propósito cuatro problemas falsos, uno por control. Con los checks obligatorios del ruleset de `main`, GitHub no permitió fusionarlo (estado `BLOCKED`), y se cerró sin fusionar.

| Problema introducido | Check en rojo | Qué reportó |
|---|---|---|
| Dependencia `org.apache.commons:commons-text` 1.9 en `pom.xml` | **SCA** | CVE-2022-42889, CRITICAL, corregida en 1.10.0 (`Total: 1 (CRITICAL: 1)`) |
| Propiedad inventada `petfinder.pago.api-secret` de 40 caracteres | **Secretos** | Regla `generic-api-key`, `leaks found: 1` |
| `GET /api/diagnostico` pasa un parámetro a `ProcessBuilder("sh", "-c", ...)` | **SAST (java-kotlin)** | `java/command-line-injection`, security-severity 9.8 |
| `MenuConsola` usa `ServicioReportes` en vez del puerto | **reglas-arquitectura** | `lasEntradasSoloHablanConPuertos` |

Todos los demás checks del mismo PR salieron en verde: el pipeline se detuvo solo por los problemas introducidos.

<img src="https://raw.githubusercontent.com/wiki/DrearSanti/pet-finder-dyas/imagenes/evidencia-seguridad-en-rojo.png" width="700" alt="Corrida de Seguridad del PR 78 con Secretos, SAST de Java y SCA en rojo"/>

*Figura 1. Corrida de `seguridad.yml` en el PR #78: Secretos, SAST (java-kotlin) y SCA en rojo; SAST de JavaScript en verde.*

<img src="https://raw.githubusercontent.com/wiki/DrearSanti/pet-finder-dyas/imagenes/evidencia-ci-reglas-en-rojo.png" width="700" alt="Corrida de CI del PR 78 con reglas-arquitectura en rojo y el resto en verde"/>

*Figura 2. Corrida de `ci.yml` en el PR #78: solo `reglas-arquitectura` en rojo; construcción, pruebas y `acciones-fijadas` en verde.*
