# Corte 3 · Tareas de Antonio (A1 a A14)

**Para:** Antonio (`antobeni07`) y su agente de Claude Code. Foco: contenedores, integración continua, pruebas y la guía de implementación.
**Página del reparto:** https://claude.ai/artifact/VsxyLQaD2CfTw7srJPbFwn (ahí se marca cada tarea cuando entra a `main`).
**Repositorio:** https://github.com/DrearSanti/pet-finder-dyas · dueño y admin: Santi (`DrearSanti`).

| Tramo | Fechas | Hito |
|---|---|---|
| 1 · Base | hasta el dom 8 nov | Un merge a `main` se despliega solo y los cuatro controles de seguridad pueden bloquear. |
| 2 · Endurecer | lun 9 a mié 11 nov | Funcionalidad congelada, reversión ejecutada y PR bloqueado cerrado. Mié 11: último día para decidir el plan de Render. |
| 3 · Documentar y ensayar | jue 12 a dom 15 nov (entrega) | v3.0.0 publicada y pipeline en verde en el último commit de `main`. Desde el vie 13 solo entran documentos. |

**Revisiones (D6):** Mateo (`technologybyteo`) revisa tus PR. Tú revisas los de Santi. Respuesta en menos de 24 h.

## Reglas

1. Una rama por tarea, desde `main` actualizado:
   ```bash
   git switch main && git pull
   git switch -c corte3/A1
   ```
2. Un PR por tarea hacia `main`, con Mateo como revisor. Se integra con **merge commit**, nunca squash ni rebase.
3. Antes de abrir el PR: `./mvnw -q clean verify` en verde, con JDK 17 (`./mvnw -v` dice `Java version: 17`). En PowerShell: `.\mvnw.cmd -q clean verify`.
4. Cuando el PR entra a `main`, marca la tarea en la página del reparto.
5. Commits, tags, push y PR los haces tú. El agente nunca hace `git commit`, `git tag`, `git push` ni `git reset --hard`.
6. No toques `pom.xml` salvo lo que la tarea pida (A1 y A5). No agregues dependencias.
7. Nunca pegues `.env` ni una clave en el chat, en un commit o en un PR.
8. Para probar algo en rojo usa una rama desechable (`prueba/...`) que nunca se fusiona, y bórrala al terminar: `git push origin --delete prueba/<nombre>`.

## Qué ya está probado y qué no

- **Probado en local:** el contenido de A1 (umbral de JaCoCo), A2 (`ci.yml`), A4 (`humo-flujo.sh`), A5 (`seguridad.yml` y `dependabot.yml`) y A7 (controlador y su prueba) ya pasó `./mvnw -q clean verify` en una copia del repo: 230 unitarias y 24 IT en verde. Está copiado tal cual en cada sección.
- **Sin probar todavía:** ningún workflow ha corrido en GitHub Actions. Tampoco nada que pase por Docker (Docker no respondió en el Mac durante la auditoría), `humo-flujo.sh` contra una app arriba, ni los pasos en la interfaz de GitHub o de Render. El gate no ejecuta workflows ni scripts: que pase solo dice que nada se rompió.

## Choques con el trabajo de los demás

Lo de Santi y Mateo que se cruza con tus archivos. Antes de abrir cada PR: `git switch main && git pull`, luego `git switch corte3/<id> && git merge main`, y el gate otra vez.

| Trabajo | Qué toca | Choca con | Qué hacer |
|---|---|---|---|
| S7 (Santi, Flyway y PostgreSQL; ya probado en local) | `docker-compose.yml` (bloque de `SPRING_JPA_HIBERNATE_DDL_AUTO`), `pom.xml` (Flyway), `application.properties` | A3 y A7 en Compose; A1 y A5 en el pom | Tus cambios van en otras líneas: conserva los de Santi al fusionar. En el pom, Flyway va en `<dependencies>`, el umbral en el plugin de JaCoCo y Guava en `<dependencyManagement>`. |
| S4 (Santi, reglas de ArchUnit; ya probado en local) | `ReglasArquitecturaTest`, consola | A2 (check `reglas-arquitectura`) | Ninguno en archivos. Su "Hecho cuando" necesita tu check: avísale cuando A2 entre. Tú revisas su PR. |
| S5 y S6 (Santi) | `.github/workflows/seguridad.yml` | A5 | El segundo en entrar agrega su job al archivo que ya existe. |
| S9 y S10 (Santi) | Núcleo del código de gestión; límite de peticiones y variables en `.env.example` y Compose | A9; A3, A6 y A7 | A9 empieza cuando S9 esté en `main`. Los límites de S10 no pueden frenar a k6. |
| M1 (Mateo, sin integrar) | `VozController`, `AsistenteController`, `SolicitudReporteDTO`, `AvistamientoDTO`, `ControladoresWebTest`, `FiltroTamanoCuerpo` nuevo | A9 | Haz A9 sobre `main` con M1 ya integrado y pon tus pruebas web en una clase nueva. |
| M2 (Mateo, sin integrar) | `application.properties` (consola H2 apagada), `application-local.properties`, `CLAUDE.md`, `docs/pruebas.md` | A1 (`CLAUDE.md`, `docs/pruebas.md`); A4 (el 404 de `/h2-console/`) | Líneas distintas. Al fusionar, la consola queda apagada. |
| M3 (Mateo, sin integrar) | README reorganizado, Corte 1 en `docs/corte-1/`, índices `docs/README.md` y `scripts/README.md` | A4 y A12 | Cada script o documento nuevo agrega su fila en el índice de su carpeta. |
| M4 (Mateo, sin integrar) | `perf/scripts/corta.js`, `.github/workflows/carga-larga.yml` (k6 v2.2.0 por tarball con SHA-256) | A2, A6 y A9 | `carga-larga.yml` es otro workflow y solo corre a mano. A6 necesita `corta.js` en `main`. A9 cambia la limpieza de `corta.js`. |

## Tu semana en orden

| Tarea | Cuándo | ¿Puede empezar? | Desbloquea (o alimenta) |
|---|---|---|---|
| A1 Umbral de JaCoCo | sáb 7 | Ya | A2 |
| A3 Compose, Dockerfile y `.env.example` | sáb 7 | Ya | A4, A6, M13, B1 |
| A7 Clave de CARTO al arrancar | sáb 7 (mejor después de A3: los dos tocan el Dockerfile) | Ya | M5 (primera imagen), A6, A13 |
| A5 SCA y Dependabot | sáb 7 a dom 8 | Ya | A8, S11, S12, S13 |
| A2 `ci.yml` | dom 8 | Cuando A1 esté en `main` (prepáralo mientras lo revisan) | S2, S4, S15, M5, M7, M13, A6, A8, A11 |
| A4 `humo-flujo.sh` | dom 8 | Ya; para probarlo con Compose, después de A3 | A6; M7 lo usa contra Render |
| A6 Imagen y verificación en cada PR | dom 8 (último día del tramo 1; si M4 o A7 se atrasan, avisa ese mismo día) | Espera A2, A3, A4, A7 y M4 | A8, A12, S11, S12, S13, S15 |
| A8 Endurecer los workflows | lun 9 a mié 11 | Espera A2, A5, A6, S5, S6 y M7 | Alimenta A12 (el enlace del tiempo) |
| A9 Código de gestión: web | lun 9 a mar 10 | Espera S9 | M10; alimenta A10 |
| A10 Menos datos en casos cerrados | mar 10 a mié 11 | Ya, pero mejor después de A9 | Alimenta S11 (mitigación de T5) |
| A11 Selenium: Cerca de ti y coincidencias | lun 9 a mié 11 | Espera A2 | Alimenta S14 (pendiente 6) |
| A12 `docs/devops.md` | jue 12 a vie 13 | Espera A6, M7 y M8 | Alimenta A13 (la guía lo enlaza) |
| A13 `docs/implementacion.md` | jue 12 a sáb 14 | Espera S7, S9, S10, S11, A7, M7, M8 y M12 | La entrega (criterio Guía, 10 pts) |
| A14 Comprobación de cierre | dom 15 (de turno el sáb 14) | Espera S2, M7 y M11 | La entrega |

---

## A1 · Umbral de cobertura en JaCoCo

Tramo 1 · esfuerzo S · **probado en local** (gate en verde).

**Para qué.** Integración continua (15 pts): la rúbrica pide "cobertura del dominio ≥ 70 % que rompe la construcción", configurada en la herramienta y no solo como reporte. Sin esto el criterio no pasa de Bueno. Hoy el `pom.xml` solo tiene `prepare-agent` y `report`.

**Depende de:** nada. **Desbloquea:** A2 (el job `pruebas-unitarias` llama a `jacoco:check@umbral-dominio`).

**Archivos:** `pom.xml`, `CLAUDE.md`, `docs/pruebas.md`, `.claude/skills/evidencias-de-pruebas/SKILL.md`.

**Cómo hacerlo.**
1. `git switch main && git pull && git switch -c corte3/A1`
2. En `pom.xml`, dentro de `jacoco-maven-plugin`, después de la execution `reporte-unitarias` y antes de `</executions>`, agrega:
   ```xml
                       <!-- Umbral de la rúbrica: 70 % de líneas del dominio o la construcción falla.
                            Se mide sobre todo petfinder/domain (BUNDLE) y no por paquete: con solo unitarias,
                            petfinder.domain.observer queda en 33 % aunque el dominio completo pase de 85 %. -->
                       <execution>
                           <id>umbral-dominio</id>
                           <phase>verify</phase>
                           <goals><goal>check</goal></goals>
                           <configuration>
                               <includes>
                                   <include>petfinder/domain/**</include>
                               </includes>
                               <rules>
                                   <rule>
                                       <element>BUNDLE</element>
                                       <limits>
                                           <limit>
                                               <counter>LINE</counter>
                                               <value>COVEREDRATIO</value>
                                               <minimum>0.70</minimum>
                                           </limit>
                                       </limits>
                                   </rule>
                               </rules>
                           </configuration>
                       </execution>
   ```
3. Agrega `-Djacoco.skip=true` a los comandos que corren solo algunas IT: con pocas IT el dominio no llega al 70 % y el build falla sin que haya un problema real.
   - `CLAUDE.md`, fila "Una sola IT": `./mvnw -q verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=Clase -Djacoco.skip=true`
   - `CLAUDE.md`, debajo de la línea del gate: "El umbral de cobertura (70 % de líneas de `petfinder/domain`) corre en `verify`. En comandos que corren solo algunas IT, agrega `-Djacoco.skip=true`."
   - `docs/pruebas.md`, fila "Solo las pruebas de sistema" (línea 28): al final de la columna de macOS/Git Bash agrega ` -Djacoco.skip=true`, y al final de la de PowerShell ` "-Djacoco.skip=true"` (con comillas, como los demás `-D` de esa columna).
   - `.claude/skills/evidencias-de-pruebas/SKILL.md`, el comando de UI (línea 35): queda `./mvnw -q -Pui verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=FlujosPrincipalesUIT -Djacoco.skip=true`.
   - No cambies las filas del registro de evidencias de `docs/pruebas.md` (son historia) ni los blueprints del Corte 2.
4. Gate: `./mvnw -q clean verify`. Commit, push y PR.

**Cómo comprobarlo.**
```bash
./mvnw clean verify | grep -E 'All coverage checks have been met|BUILD'
# Esperado: "All coverage checks have been met" y "BUILD SUCCESS" (el dominio está en 97,2 % con unitarias e IT)
```
En rojo, sin subirlo: cambia `<minimum>0.70</minimum>` por `0.99` y corre **sin `-q`** (con `-q` Maven oculta la línea `Rule violated`, que es un aviso, y el letrero `BUILD FAILURE`):
```bash
./mvnw clean verify | grep -E 'Rule violated|Coverage checks have not been met|BUILD'
# Esperado: "Rule violated for bundle ...: lines covered ratio is 0.9..., but expected minimum is 0.99" y "BUILD FAILURE"
```
Vuelve a `0.70` y confirma con `git diff pom.xml`.

**Trampas.**
- `BUNDLE`, no `PACKAGE`: con solo unitarias, `petfinder.domain.observer` está en 33 % y rompería el build aunque el dominio completo esté en 85 %.
- M2 (Mateo) también cambia `CLAUDE.md` y `docs/pruebas.md`, en otras líneas (la consola H2). Si M2 entra antes, haz `git merge main` en tu rama antes del PR.

**Pedido para tu agente:** "Aplica la sección A1 de esta guía tal cual, corre el gate y la prueba en rojo con 0.99, deja el pom en 0.70 y muéstrame `git diff`. No hagas commit."

---

## A2 · `ci.yml`: construcción y pruebas

Tramo 1 · esfuerzo L · **probado en local** el gate; el workflow **sin probar todavía** en Actions.

**Para qué.** Integración continua (15) y Arquitectura verificada (15): pipeline en cada push y PR, con cada etapa como un check que lo detiene, reportes como artefactos y caché. Es el pendiente 1 del Corte 2, y de este workflow cuelgan CD (M5, M7) y la protección de `main` (S2).

**Depende de:** A1 en `main`. **Desbloquea:** S2 (GitHub solo deja marcar como obligatorios los checks que ya corrieron), S4, S15, M5, M7, M13, A6, A8, A11.

**Archivos:** `.github/workflows/ci.yml` (nuevo).

**Cómo hacerlo.**
1. Desde `main` con A1 ya integrado: `git switch main && git pull && git switch -c corte3/A2`. Si lo preparaste antes, haz `git merge main` en tu rama cuando A1 entre.
2. Crea `.github/workflows/ci.yml` con estos cinco jobs. Los jobs `imagen-y-escaneo` y `despliegue-y-verificacion` se agregan después, en A6:
   ```yaml
   # Integración continua de Pet Finder: un job por etapa, y cada job es un check del pull request.
   # Los nombres de los jobs son los que Santi marca como obligatorios en el ruleset de main:
   # no los cambies sin avisarle, porque un check obligatorio que no aparece bloquea todos los PR.
   name: CI

   on:
     # Todo push, en cualquier rama y en los tags de versión (v2.1.0, v3.0.0...). Sin paths-ignore:
     # un check obligatorio que no corre deja el PR esperando para siempre.
     push:
       branches: ['**']
       tags: ['v*']
     pull_request:
       branches: [main]
     # Una corrida diaria sobre main: la base de Trivy cambia sin que nadie toque el código, y así una
     # vulnerabilidad nueva aparece antes del día de la entrega (riesgo del plan). 11:00 UTC = 6:00 en Bogotá.
     schedule:
       - cron: '0 11 * * *'
     # Botón "Run workflow" para el turno de los días 14 y 15 (A14).
     workflow_dispatch:

   # Solo lectura por defecto: ningún job de CI necesita escribir en el repositorio ni usa secretos.
   permissions:
     contents: read

   # Una corrida nueva del mismo PR cancela la anterior; en main y en tags nunca se cancela.
   concurrency:
     group: ci-${{ github.event_name }}-${{ github.ref }}
     cancel-in-progress: ${{ github.event_name == 'pull_request' }}

   # Con shell: bash explícito, GitHub usa -eo pipefail: un comando que falla dentro de un tubo
   # (por ejemplo sh scripts/humo-flujo.sh ... | tee) también hace fallar el paso.
   defaults:
     run:
       shell: bash

   jobs:
     construccion:
       runs-on: ubuntu-24.04
       timeout-minutes: 15
       steps:
         - uses: actions/checkout@fbc6f3992d24b796d5a048ff273f7fcc4a7b6c09 # v5.1.0
         - uses: actions/setup-java@b6effb05e454b25005698d916606bdc6ffcbf961 # v5.7.0
           with:
             distribution: temurin
             java-version: '17'
             cache: maven
         - name: Compilar y empaquetar sin pruebas
           run: ./mvnw -B -ntp -DskipTests package

     pruebas-unitarias:
       needs: construccion
       runs-on: ubuntu-24.04
       timeout-minutes: 15
       steps:
         - uses: actions/checkout@fbc6f3992d24b796d5a048ff273f7fcc4a7b6c09 # v5.1.0
         - uses: actions/setup-java@b6effb05e454b25005698d916606bdc6ffcbf961 # v5.7.0
           with:
             distribution: temurin
             java-version: '17'
             cache: maven
         # El patrón positivo *Test es obligatorio: con solo '!ReglasArquitecturaTest', Surefire corre
         # también las *IT y las *UIT. ArchUnit va en su propio check (reglas-arquitectura).
         # jacoco:check@umbral-dominio aplica el umbral del pom (70 % de líneas de petfinder/domain).
         - name: Unitarias y umbral de cobertura del dominio
           run: ./mvnw -B -ntp test '-Dtest=*Test,!ReglasArquitecturaTest' jacoco:report jacoco:check@umbral-dominio
         - name: Publicar reportes de pruebas y cobertura
           if: always()
           uses: actions/upload-artifact@330a01c490aca151604b8cf639adc76d48f6c5d4 # v5.0.0
           with:
             name: reportes-pruebas-unitarias
             path: |
               target/surefire-reports/
               target/site/jacoco/
             if-no-files-found: warn

     reglas-arquitectura:
       needs: construccion
       runs-on: ubuntu-24.04
       timeout-minutes: 15
       steps:
         - uses: actions/checkout@fbc6f3992d24b796d5a048ff273f7fcc4a7b6c09 # v5.1.0
         - uses: actions/setup-java@b6effb05e454b25005698d916606bdc6ffcbf961 # v5.7.0
           with:
             distribution: temurin
             java-version: '17'
             cache: maven
         - name: Reglas de la arquitectura hexagonal (ArchUnit)
           run: ./mvnw -B -ntp test -Dtest=ReglasArquitecturaTest
         - name: Publicar reporte de ArchUnit
           if: always()
           uses: actions/upload-artifact@330a01c490aca151604b8cf639adc76d48f6c5d4 # v5.0.0
           with:
             name: reportes-reglas-arquitectura
             path: target/surefire-reports/
             if-no-files-found: warn

     pruebas-integracion:
       needs: construccion
       runs-on: ubuntu-24.04
       timeout-minutes: 15
       steps:
         - uses: actions/checkout@fbc6f3992d24b796d5a048ff273f7fcc4a7b6c09 # v5.1.0
         - uses: actions/setup-java@b6effb05e454b25005698d916606bdc6ffcbf961 # v5.7.0
           with:
             distribution: temurin
             java-version: '17'
             cache: maven
         # -Dtest=NINGUNA salta las unitarias (ya tienen su check). -Djacoco.skip=true porque el umbral
         # de cobertura se aplica en pruebas-unitarias, no aquí.
         - name: Integración y sistema (*IT con H2 y la app por HTTP)
           run: ./mvnw -B -ntp verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Djacoco.skip=true
         - name: Publicar reportes de Failsafe
           if: always()
           uses: actions/upload-artifact@330a01c490aca151604b8cf639adc76d48f6c5d4 # v5.0.0
           with:
             name: reportes-pruebas-integracion
             path: target/failsafe-reports/
             if-no-files-found: warn

     # Corre en cada PR desde el primer día, pero pasa a obligatorio solo después de 3 corridas
     # seguidas en verde en Actions (A11): todavía no se ha visto en Linux.
     pruebas-ui:
       needs: construccion
       runs-on: ubuntu-24.04
       timeout-minutes: 20
       steps:
         - uses: actions/checkout@fbc6f3992d24b796d5a048ff273f7fcc4a7b6c09 # v5.1.0
         - uses: actions/setup-java@b6effb05e454b25005698d916606bdc6ffcbf961 # v5.7.0
           with:
             distribution: temurin
             java-version: '17'
             cache: maven
         - name: Interfaz con Selenium y Chrome sin ventana (*UIT)
           run: ./mvnw -B -ntp verify -Pui -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false '-Dit.test=*UIT' -Djacoco.skip=true
         - name: Publicar reportes de la interfaz
           if: always()
           uses: actions/upload-artifact@330a01c490aca151604b8cf639adc76d48f6c5d4 # v5.0.0
           with:
             name: reportes-pruebas-ui
             path: target/failsafe-reports/
             if-no-files-found: warn
   ```
3. Antes de subir, corre en local los comandos de los jobs. En PowerShell cambia `./mvnw` por `.\mvnw.cmd` y usa comillas dobles en los `-D` con `*` o `!`.
   ```bash
   ./mvnw -B -ntp -DskipTests package
   ./mvnw -B -ntp test '-Dtest=*Test,!ReglasArquitecturaTest' jacoco:report jacoco:check@umbral-dominio
   ./mvnw -B -ntp test -Dtest=ReglasArquitecturaTest
   ./mvnw -B -ntp verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Djacoco.skip=true
   ./mvnw -B -ntp verify -Pui -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false '-Dit.test=*UIT' -Djacoco.skip=true   # necesita Chrome
   ```
   La segunda debe imprimir "All coverage checks have been met" y no correr ninguna clase `*IT`. Estos comandos, tal como están en los jobs, no se han corrido todavía: si alguno falla en local, arréglalo antes del PR.
4. Gate, commit, push y PR.
5. Apenas aparezcan los checks, escríbele a Santi los nombres exactos para S2: `construccion`, `pruebas-unitarias`, `reglas-arquitectura` y `pruebas-integracion` (obligatorios ya), y `pruebas-ui` (obligatorio solo después de A11).
6. Cuando entre a `main`, confirma que la corrida sobre `main` también quedó en verde y avísales a Mateo (M5, M7) y a Santi (S4).

**Cómo comprobarlo** (sin probar todavía en Actions).
- En el PR salen los 5 checks en verde. Cada corrida publica `reportes-pruebas-unitarias` (Surefire y JaCoCo), `reportes-reglas-arquitectura`, `reportes-pruebas-integracion` y `reportes-pruebas-ui`. Desde la terminal: `gh run list --workflow CI --limit 3`.
- Rama desechable `prueba/umbral-099` sacada de la tuya, con `<minimum>0.99</minimum>`: al hacer push, `pruebas-unitarias` queda en rojo. Guarda el enlace de esa corrida (sirve para `devops.md` y la presentación) y borra la rama.

**Trampas.**
- Usa exactamente `'-Dtest=*Test,!ReglasArquitecturaTest'`. Con solo el patrón negativo, Surefire corre también las IT y las UIT (medido: 26 clases en vez de 19) y Selenium descarga unos 384 MB.
- Nada de `paths-ignore`: un check obligatorio que no corre deja el PR esperando para siempre.
- Los nombres de los jobs son los nombres de los checks. Si cambias uno después de S2, avísale a Santi o todos los PR quedan bloqueados.
- Cada PR muestra los checks dos veces (eventos `push` y `pull_request`). Es normal.
- `.github/workflows/carga-larga.yml` de Mateo (M4) es otro workflow (`Carga larga`, solo con `workflow_dispatch`) y no choca con `ci.yml`. Si entra antes, la carpeta ya existe y solo agregas tu archivo.
- El `schedule` diario solo corre sobre `main`.

**Pedido para tu agente:** "Crea `.github/workflows/ci.yml` exactamente como en la sección A2 de esta guía, corre los cinco comandos del paso 3 y dime cuáles pasan. No hagas commit ni push."

---

## A3 · Arreglar Compose, Dockerfile y `.env.example`

Tramo 1 · esfuerzo S · **sin parche: todo sin probar todavía.**

**Para qué.** Contenerización (5): `docker compose up` levanta el sistema completo, con `.env.example` y healthcheck. También deja la imagen con el nombre que usa A6, hace que la caché de Docker funcione en Actions y permite que tu agente revise `.env.example`.

**Depende de:** nada. **Desbloquea:** A4, A6, M13, B1.

**Archivos:** `docker-compose.yml`, `Dockerfile`, `.env.example`, `.claude/settings.json`.

**Cómo hacerlo.**
1. `git switch main && git pull && git switch -c corte3/A3`
2. `.claude/settings.json`: **este cambio lo haces tú a mano**, porque son los permisos del agente. Hoy `Read(./.env.*)` también bloquea `.env.example`, y `Bash(cat .env:*)` puede bloquear `cat .env.example` si Claude Code lo compara como prefijo sin espacio (no está confirmado). Cambia esas dos reglas por las de abajo y deja el resto de `deny` igual. `Bash(cat .env)` y `Bash(cat .env *)` siguen bloqueando `cat .env` con o sin más argumentos, pero no `cat .env.example`:
   ```json
       "deny": [
         "Read(./.env)",
         "Read(./.env.local)",
         "Read(./.env.produccion)",
         "Bash(git commit:*)",
         "Bash(git push:*)",
         "Bash(git tag -a:*)",
         "Bash(git tag step-:*)",
         "Bash(git reset --hard:*)",
         "Bash(cat .env)",
         "Bash(cat .env *)"
       ]
   ```
   Si alguien usa otro archivo con secretos (`.env.algo`), agrégalo con su nombre exacto.
3. `docker-compose.yml`, servicio `app`:
   - Cambia `image: proyecto:dev` por `image: pet-finder:local`. El job de A6 construye con ese nombre y Compose la usa con `--no-build`.
   - Agrega un healthcheck explícito después de `depends_on`:
     ```yaml
         # Mismo endpoint que el HEALTHCHECK del Dockerfile, escrito aquí para que se vea qué espera
         # docker compose up --wait (Spring Boot listo, no solo el contenedor arrancado) sin abrir la imagen.
         healthcheck:
           test: ["CMD-SHELL", "curl -fsS http://localhost:8080/actuator/health/readiness || exit 1"]
           interval: 10s
           timeout: 3s
           start_period: 60s
           retries: 5
     ```
   - No toques `SPRING_JPA_HIBERNATE_DDL_AUTO` ni su comentario: los cambia Santi en S7 (Flyway).
4. `Dockerfile`, etapa 1: quita los dos `--mount=type=cache,target=/root/.m2` y corrige los comentarios. Las líneas quedan así:
   ```dockerfile
   # ---------- Etapa 1: compilar el .jar con Maven (las pruebas corren antes, en el pipeline) ----------
   FROM maven:3.9.16-eclipse-temurin-17-noble AS build
   WORKDIR /src

   # Primero solo el pom: las dependencias quedan en una capa propia que se reutiliza mientras el pom
   # no cambie. Sin --mount=type=cache: esa caché vive solo en la máquina que construye y no viaja en
   # la caché type=gha de GitHub Actions; una capa normal sí.
   COPY pom.xml .
   RUN mvn -B -ntp -q dependency:go-offline
   ```
   y, más abajo:
   ```dockerfile
   RUN mvn -B -ntp -q package -DskipTests \
    && cp target/pet-finder.jar /app.jar
   ```
   El bloque `ARG PETFINDER_CLAVE_CARTO` lo quita A7. Si haces las dos en paralelo, la segunda en entrar resuelve un conflicto en estas líneas.
5. `.env.example`: ábrelo tú (o tu agente, después del paso 2) y confirma tres cosas.
   - Están todas las variables que usa Compose. Este comando imprime las que faltan:
     ```bash
     for v in $(grep -oE '\$\{[A-Z_]+' docker-compose.yml | cut -c3- | sort -u); do grep -q "^$v=" .env.example || echo "Falta en .env.example: $v"; done
     ```
     Hoy Compose usa `ANTHROPIC_API_KEY`, `PETFINDER_IA_MODELO`, `PETFINDER_IA_TOPE_DIARIO`, `POSTGRES_DB`, `POSTGRES_PASSWORD` y `POSTGRES_USER`.
   - Ningún valor real: `ANTHROPIC_API_KEY=` vacío, y `POSTGRES_PASSWORD` con un valor de ejemplo **no vacío** (por ejemplo `cambia-esta-clave`). Compose usa `${POSTGRES_PASSWORD:?...}`, que también falla con un valor vacío, y el job de A6 hace `cp .env.example .env`.
   - Cada variable con una línea de comentario: para qué sirve y si es obligatoria.
6. Gate, commit, push y PR. En el PR pega la salida de `docker compose ps`.

**Cómo comprobarlo** (es la primera vez que alguien lo corre de punta a punta). Se prueba `.env.example` tal cual, como pide el "Hecho cuando" (`cp .env.example .env`), pero con `--env-file` para no pisar tu `.env` si ya tienes uno con tu clave:
```bash
dc() { docker compose --env-file .env.example "$@"; }   # equivale a cp .env.example .env
dc up -d --build --wait      # termina sin error
dc ps                        # app y db: (healthy)
dc exec app id -u            # un número distinto de 0
curl -fsS http://localhost:8080/actuator/health/readiness   # {"status":"UP"}
dc down
```
Si no tienes `.env`, también vale el comando literal del plan: `cp .env.example .env && docker compose up -d --build --wait`.

**Trampas.**
- En Windows, Docker Desktop tiene que estar abierto.
- Si `--wait` no termina y `docker compose ps` dice `unhealthy`, confirma que la imagen trae `curl`: `docker compose exec app curl --version`. El HEALTHCHECK del Dockerfile ya depende de él.
- `.env` no se sube: `git status` no debe mostrarlo.
- Después de S7 (Flyway), un volumen local creado antes con `ddl-auto create` hace que la app no arranque, porque Flyway encuentra tablas que no creó. Se arregla con `docker compose down -v`, que borra la base local.
- S7, A7 y S10 también tocan `docker-compose.yml` o `.env.example`. Haz `git merge main` antes de abrir el PR.

**Pedido para tu agente:** "Ya cambié `.claude/settings.json`. Aplica los pasos 3 a 5 de la sección A3, corre el comando que lista variables faltantes y dime qué falta en `.env.example`. Docker lo corro yo."

---

## A4 · `scripts/humo-flujo.sh`

Tramo 1 · esfuerzo S · **probado en local** el gate (que no ejecuta el script); contra una app arriba, **sin probar todavía**.

**Para qué.** Despliegue continuo (15) y Arquitectura verificada (15): la etapa "Despliegue y verificación" se detiene si falla la prueba de humo. La usan A6 (en el PR, contra Compose) y M7 (contra Render). También exige 404 en `/h2-console/` (amenaza T9).

**Depende de:** A3, para probarlo con Compose. **Desbloquea:** A6, M7.

**Archivos:** `scripts/humo-flujo.sh` (nuevo) y `scripts/README.md` (una fila, si M3 ya entró).

**Cómo hacerlo.**
1. `git switch main && git pull && git switch -c corte3/A4`
2. Crea `scripts/humo-flujo.sh`:
   ```sh
   #!/usr/bin/env sh
   # Prueba de humo del flujo principal contra una URL ya desplegada (Compose, Render o un jar local).
   # A diferencia de humo.sh y humo-docker.sh, no arranca nada: recibe la URL y recorre lo que hace
   # una persona: el servicio está listo, se publica un caso, se ve su detalle y aparece en la lista.
   # También exige que la consola de H2 no responda (amenaza T9) y cierra el caso que creó, para no
   # dejar reportes de prueba en el mapa.
   #
   # Uso (macOS, Linux y Git Bash de Windows):
   #   sh scripts/humo-flujo.sh http://localhost:8080
   # Variables opcionales: HUMO_ESPERA (segundos que espera la salud, 120 por defecto).
   # Sale con 0 solo si todos los pasos dieron lo esperado; con 1 en el primer paso que falla.
   set -u

   if [ "$#" -ne 1 ]; then
     echo "Uso: sh scripts/humo-flujo.sh URL_BASE   (por ejemplo http://localhost:8080)"
     exit 2
   fi
   BASE="${1%/}"
   ESPERA="${HUMO_ESPERA:-120}"
   CUERPO="$(mktemp)"
   trap 'rm -f "$CUERPO"' EXIT

   # Hace una petición, deja el cuerpo en $CUERPO e imprime solo el código HTTP (000 si no hubo respuesta).
   pedir() {
     curl -s -o "$CUERPO" -w '%{http_code}' --max-time 20 "$@" || true
   }

   fallar() {
     echo "FALLA: $1"
     echo "Cuerpo recibido: $(head -c 300 "$CUERPO" 2>/dev/null)"
     exit 1
   }

   # 1. Salud: un plan gratis puede estar dormido, así que se espera antes de dar el servicio por caído.
   codigo=000
   segundos=0
   while [ "$segundos" -lt "$ESPERA" ]; do
     codigo=$(pedir "$BASE/actuator/health/readiness")
     [ "$codigo" = "200" ] && break
     sleep 5
     segundos=$((segundos + 5))
   done
   [ "$codigo" = "200" ] || fallar "/actuator/health/readiness respondió $codigo después de ${ESPERA} s"
   echo "ok  GET /actuator/health/readiness -> 200"

   # 2. La consola de H2 no se ofrece en un entorno desplegado (amenaza T9). Va antes de crear
   # el caso para que, si falla, no quede un reporte de prueba abierto.
   codigo=$(pedir "$BASE/h2-console/")
   [ "$codigo" = "404" ] || fallar "GET /h2-console/ respondió $codigo (se esperaba 404)"
   echo "ok  GET /h2-console/ -> 404"

   # 3. Publicar un caso con un nombre que no puede existir: así el detalle se compara sin suponer un id.
   NOMBRE="Humo$(date +%s)"
   codigo=$(pedir -X POST "$BASE/api/reportes" -H 'Content-Type: application/json' -d "{
     \"tipo\": \"PERDIDA\", \"zona\": \"Prueba de humo\", \"referencia\": \"Pipeline\",
     \"descripcion\": \"Caso creado por scripts/humo-flujo.sh; se cierra al terminar\",
     \"nombreContacto\": \"Prueba de humo\", \"medioContacto\": \"3000000000\",
     \"mascota\": { \"nombre\": \"$NOMBRE\", \"especie\": \"perro\" } }")
   [ "$codigo" = "201" ] || fallar "POST /api/reportes respondió $codigo (se esperaba 201)"
   ID=$(grep -o '"id":"[^"]*"' "$CUERPO" | head -n 1 | cut -d '"' -f 4)
   [ -n "$ID" ] || fallar "POST /api/reportes no devolvió un id"
   echo "ok  POST /api/reportes -> 201 ($ID)"

   # 4. El detalle devuelve la misma mascota que se publicó.
   codigo=$(pedir "$BASE/api/reportes/$ID")
   [ "$codigo" = "200" ] || fallar "GET /api/reportes/$ID respondió $codigo"
   grep -q "\"nombreMascota\":\"$NOMBRE\"" "$CUERPO" || fallar "GET /api/reportes/$ID no trae la mascota $NOMBRE"
   echo "ok  GET /api/reportes/$ID -> 200 con $NOMBRE"

   # 5. La lista pública incluye el caso nuevo.
   codigo=$(pedir "$BASE/api/reportes")
   [ "$codigo" = "200" ] || fallar "GET /api/reportes respondió $codigo"
   grep -q "\"id\":\"$ID\"" "$CUERPO" || fallar "GET /api/reportes no incluye $ID"
   echo "ok  GET /api/reportes -> 200 e incluye $ID"

   # 6. Limpieza: se cierra el caso de prueba para que no quede en la lista ni en el mapa.
   codigo=$(pedir -X POST "$BASE/api/reportes/$ID/cerrar")
   [ "$codigo" = "204" ] || fallar "POST /api/reportes/$ID/cerrar respondió $codigo (se esperaba 204)"
   echo "ok  POST /api/reportes/$ID/cerrar -> 204"

   echo "Prueba de humo del flujo: todo en orden contra $BASE"
   ```
3. Si `scripts/README.md` ya está en `main` (llega con M3), agrega su fila a la tabla:
   ```markdown
   | [`humo-flujo.sh`](humo-flujo.sh) | Contra una URL ya levantada (Compose, Render o un jar): readiness 200, `/h2-console/` 404, publica un caso (201), lo consulta (200, mismo nombre), lo encuentra en la lista y lo cierra (204) | `sh scripts/humo-flujo.sh http://localhost:8080` |
   ```
4. Gate, commit, push y PR.

**Cómo comprobarlo.**
```bash
docker compose up -d --build --wait
sh scripts/humo-flujo.sh http://localhost:8080; echo "salida=$?"                  # seis líneas "ok" y salida=0
docker compose stop app
HUMO_ESPERA=10 sh scripts/humo-flujo.sh http://localhost:8080; echo "salida=$?"   # una línea FALLA y salida=1
docker compose down
```

**Trampas.**
- Contra `./mvnw spring-boot:run` falla en el paso 2 hasta que entre M2, porque hoy la consola H2 está prendida por defecto. Es lo esperado. En Compose ya está apagada (`SPRING_H2_CONSOLE_ENABLED: "false"`), y contra Render el script queda en rojo hasta que M2 se despliegue.
- Después de A9 el paso 6 (cerrar) necesita el código de gestión. A9 actualiza este script.
- El nombre `Humo<segundos>` evita suponer un ID. Nunca pongas un ID fijo.
- `.gitattributes` fuerza LF en `*.sh`. Si `sh` dice `$'\r': command not found`, tu editor guardó el archivo con CRLF.

**Pedido para tu agente:** "Crea `scripts/humo-flujo.sh` como en la sección A4 y agrega su fila en `scripts/README.md` si existe. Con Compose arriba, córrelo y muéstrame la salida."

---

## A5 · Control SCA con Trivy y Dependabot

Tramo 1 · esfuerzo S · `seguridad.yml` y `dependabot.yml` **probados en local** con el gate. El cambio de Guava no estaba en esa prueba y los workflows están **sin probar todavía** en Actions.

**Para qué.** DevSecOps (20): análisis de dependencias que detiene el pipeline con hallazgos HIGH o CRITICAL que tengan corrección, con reporte como artefacto, y el hallazgo de hoy corregido: Guava CVE-2026-102554 (GHSA-xxph-c9ww-hj94, alta, solo en pruebas, llega por Selenium).

**Depende de:** nada. **Desbloquea:** A8, S11, S12, S13.

**Archivos:** `.github/workflows/seguridad.yml`, `.github/dependabot.yml`, `pom.xml` (solo Guava) y `.trivyignore` solo si hace falta una excepción.

**Cómo hacerlo.**
1. `git switch main && git pull && git switch -c corte3/A5`
2. `.github/workflows/seguridad.yml`. Si Santi ya lo creó con S5 o S6, agrega solo el job `sca` debajo de `jobs:` y conserva lo suyo:
   ```yaml
   # Controles de seguridad de Pet Finder. Santi agrega aquí Secretos (S5) y SAST (S6).
   name: Seguridad

   on:
     push:
       branches: ['**']
       tags: ['v*']
     pull_request:
       branches: [main]
     schedule:
       - cron: '0 11 * * *'
     workflow_dispatch:

   permissions:
     contents: read

   defaults:
     run:
       shell: bash

   jobs:
     sca:
       name: SCA
       runs-on: ubuntu-24.04
       timeout-minutes: 10
       steps:
         - uses: actions/checkout@fbc6f3992d24b796d5a048ff273f7fcc4a7b6c09 # v5.1.0
         # Umbral del enunciado: ninguna vulnerabilidad HIGH o CRITICAL con corrección disponible.
         # Trivy lee pom.xml y resuelve las dependencias transitivas; exit-code 1 detiene el pipeline.
         - name: SCA de dependencias (HIGH y CRITICAL con corrección detienen el pipeline)
           uses: aquasecurity/trivy-action@ed142fd0673e97e23eac54620cfb913e5ce36c25 # v0.36.0
           with:
             version: v0.76.1
             scan-type: fs
             scan-ref: .
             scanners: vuln
             severity: HIGH,CRITICAL
             ignore-unfixed: true
             exit-code: '1'
             format: table
             output: trivy-sca.txt
         - name: Mostrar hallazgos en el log
           if: always()
           run: cat trivy-sca.txt 2>/dev/null || echo "Trivy no generó reporte"
         - name: Publicar reporte de SCA
           if: always()
           uses: actions/upload-artifact@330a01c490aca151604b8cf639adc76d48f6c5d4 # v5.0.0
           with:
             name: reporte-sca
             path: trivy-sca.txt
             if-no-files-found: warn
   ```
3. `.github/dependabot.yml`:
   ```yaml
   # Mantiene al día dependencias, imágenes base y acciones. No reemplaza al check SCA: solo abre PR,
   # y cada PR de Dependabot pasa por el pipeline y por la revisión de otra persona como cualquier otro.
   version: 2
   updates:
     - package-ecosystem: maven
       directory: /
       schedule:
         interval: weekly
       open-pull-requests-limit: 5
     - package-ecosystem: docker
       directory: /
       schedule:
         interval: weekly
     # Las acciones están fijadas por SHA; Dependabot sube el SHA y deja el comentario de versión.
     - package-ecosystem: github-actions
       directory: /
       schedule:
         interval: weekly
   ```
4. `pom.xml`: fuerza Guava. Entre `</properties>` (línea 31) y `<dependencies>`:
   ```xml
       <!-- SCA (A5): selenium-java trae guava con CVE-2026-102554 (alta, solo en pruebas).
            Se fuerza la versión corregida mientras Selenium no la suba por su cuenta. -->
       <dependencyManagement>
           <dependencies>
               <dependency>
                   <groupId>com.google.guava</groupId>
                   <artifactId>guava</artifactId>
                   <version>33.7.2-jre</version>
               </dependency>
           </dependencies>
       </dependencyManagement>
   ```
5. Comprueba Guava y la interfaz:
   ```bash
   ./mvnw dependency:tree -Dincludes=com.google.guava:guava | grep guava   # debe decir 33.7.2-jre
   ./mvnw -q -Pui verify                                                   # sigue en verde (necesita Chrome)
   ```
6. Gate, commit, push y PR.

**Cómo comprobarlo** (sin probar todavía en Actions).
- El check `SCA` sale en verde en el PR y en `main`, con el artefacto `reporte-sca`.
- Rama desechable `prueba/sca-rojo`: agrega a `pom.xml` la dependencia `org.apache.commons:commons-text:1.9` y haz push. `SCA` queda en rojo (CVE-2022-42889, crítica). Guarda el enlace y borra la rama. No es el PR de evidencia de S12: ese lo arma Santi.
- `./mvnw -Pui verify` en verde con la Guava nueva.

**Trampas.**
- Sin el cambio de Guava, `SCA` queda en rojo desde la primera corrida en `main`.
- La compatibilidad de Guava 33.7.2 con Selenium no se ha probado. Si `-Pui verify` falla, no bajes el umbral: avísale a Santi para registrarlo en `seguridad.md` como riesgo aceptado, con una línea acotada en `.trivyignore`.
- El SHA de `aquasecurity/trivy-action` (v0.36.0) está confirmado. La versión de Trivy `v0.76.1` no: si la acción no la encuentra, el job falla al instalar y no por un hallazgo.
- `seguridad.yml` lo comparten S5 (Secretos) y S6 (SAST). El que entre segundo hace `git merge main` y agrega su job. Este archivo ya trae la corrida diaria (`schedule`) que pide S5: avísale a Santi para que no agregue otro `schedule` bajo `on:`, porque GitHub rechaza un workflow con la clave repetida.
- Dependabot abre PR que también necesitan revisión y checks en verde. Revísalos como cualquier otro PR.
- Opcional, sin probar: para que Dependabot también suba `postgres` en `docker-compose.yml`, agrega un bloque `package-ecosystem: docker-compose`.

**Pedido para tu agente:** "Aplica los pasos 2 a 5 de la sección A5 (si `seguridad.yml` ya existe, solo agrega el job `sca`), corre `dependency:tree` y `-Pui verify` y dime el resultado."

---

## A6 · Imagen, escaneo y verificación efímera en cada PR

Tramo 1 · esfuerzo M · los jobs pasaron el gate junto con A2, pero el gate no ejecuta workflows: **sin probar todavía** en Actions.

**Para qué.** Integración continua (15) y DevSecOps (20): las etapas "Imagen y escaneo" y "Despliegue y verificación" aparecen como checks del PR. Trivy revisa la imagen con CRITICAL como umbral y deja los HIGH en un reporte. Es lo único automático que toca PostgreSQL antes de `main`, y falla si el jar trae `config-local.js` (T11).

**Depende de:** A2, A3, A4, A7 y M4 en `main`. **Desbloquea:** A8, A12, S11, S12, S13, S15.

**Archivos:** `.github/workflows/ci.yml`.

**Cómo hacerlo.**
1. `git switch main && git pull && git switch -c corte3/A6`, con A2, A3, A4, A7 y M4 ya integrados.
2. Agrega estos dos jobs al final de `ci.yml`:
   ```yaml
     imagen-y-escaneo:
       needs: construccion
       runs-on: ubuntu-24.04
       timeout-minutes: 20
       steps:
         - uses: actions/checkout@fbc6f3992d24b796d5a048ff273f7fcc4a7b6c09 # v5.1.0
         - uses: docker/setup-buildx-action@f87e5991a6d7451dcb8d9637bfbc97413f497069 # v4.4.1
         # La imagen se construye aquí y no en el computador de nadie. La caché type=gha guarda las capas
         # entre corridas: si el pom no cambió, la capa de dependencias no se vuelve a descargar.
         # provenance: false para no publicar metadatos de construcción que nadie usa.
         - name: Construir la imagen (sin publicarla)
           uses: docker/build-push-action@c3c9e263c25d99ce0380d002d59b67737d91b0dc # v7.4.0
           with:
             context: .
             load: true
             push: false
             tags: pet-finder:local
             provenance: false
             cache-from: type=gha
             cache-to: type=gha,mode=max
         # Amenaza T11: la clave de CARTO llega al arrancar (PETFINDER_CLAVE_CARTO), nunca dentro del jar.
         - name: El jar de la imagen no trae config-local.js
           run: |
             contenedor=$(docker create pet-finder:local)
             docker cp "$contenedor":/app/app.jar app.jar
             docker rm "$contenedor" > /dev/null
             if unzip -l app.jar | grep -q 'static/js/config-local\.js'; then
               echo "::error::La imagen trae static/js/config-local.js; la clave de CARTO va en PETFINDER_CLAVE_CARTO."
               exit 1
             fi
             echo "El jar de la imagen no contiene static/js/config-local.js."
         # Umbral del enunciado: ninguna vulnerabilidad CRITICAL con corrección disponible.
         - name: Escaneo de la imagen (CRITICAL con corrección detiene el pipeline)
           uses: aquasecurity/trivy-action@ed142fd0673e97e23eac54620cfb913e5ce36c25 # v0.36.0
           with:
             version: v0.76.1
             scan-type: image
             image-ref: pet-finder:local
             scanners: vuln
             severity: CRITICAL
             ignore-unfixed: true
             exit-code: '1'
         # HIGH no detiene la imagen (se explica en ADR-003), pero queda en el reporte y en seguridad.md.
         - name: Reporte de HIGH y CRITICAL de la imagen
           if: always()
           uses: aquasecurity/trivy-action@ed142fd0673e97e23eac54620cfb913e5ce36c25 # v0.36.0
           with:
             version: v0.76.1
             scan-type: image
             image-ref: pet-finder:local
             scanners: vuln
             severity: HIGH,CRITICAL
             exit-code: '0'
             format: table
             output: trivy-imagen.txt
         - name: Publicar reporte de la imagen
           if: always()
           uses: actions/upload-artifact@330a01c490aca151604b8cf639adc76d48f6c5d4 # v5.0.0
           with:
             name: reporte-imagen
             path: trivy-imagen.txt
             if-no-files-found: warn
         # El job siguiente prueba exactamente la imagen que se acaba de escanear.
         - name: Guardar la imagen para despliegue-y-verificacion
           run: docker save pet-finder:local | gzip > pet-finder-imagen.tar.gz
         - uses: actions/upload-artifact@330a01c490aca151604b8cf639adc76d48f6c5d4 # v5.0.0
           with:
             name: imagen-pet-finder
             path: pet-finder-imagen.tar.gz
             retention-days: 1
             compression-level: 0

     # Etapa "Despliegue y verificación" dentro del PR: la imagen escaneada corre con PostgreSQL en
     # Compose, se le pasa la prueba de humo y la carga corta con el SLO. Es lo único automático que toca
     # PostgreSQL. El despliegue real a Render lo hace cd.yml (Mateo) después de fusionar a main.
     despliegue-y-verificacion:
       needs: [pruebas-unitarias, reglas-arquitectura, pruebas-integracion, imagen-y-escaneo]
       runs-on: ubuntu-24.04
       timeout-minutes: 20
       steps:
         - uses: actions/checkout@fbc6f3992d24b796d5a048ff273f7fcc4a7b6c09 # v5.1.0
         - uses: actions/download-artifact@634f93cb2916e3fdff6788551b99b062d0335ce0 # v5.0.0
           with:
             name: imagen-pet-finder
         - name: Cargar la imagen escaneada
           run: gunzip -c pet-finder-imagen.tar.gz | docker load
         # .env sale de .env.example, como lo haría alguien que clona el repositorio.
         - name: Levantar Compose con la imagen y PostgreSQL
           run: |
             cp .env.example .env
             docker compose up -d --no-build --wait --wait-timeout 180
             docker compose ps
         - name: Prueba de humo del flujo principal
           run: sh scripts/humo-flujo.sh http://localhost:8080 | tee humo-flujo.txt
         - uses: grafana/setup-k6-action@43b9fc21641a76002687994433dd586f56e791b1 # v1.2.2
           with:
             k6-version: '2.2.0'
         # Los umbrales del SLO viven en el script: si no se cumplen, k6 sale distinto de 0.
         - name: Prueba de carga corta con el SLO
           env:
             BASE_URL: http://localhost:8080
           run: k6 run --summary-export=k6-resumen.json perf/scripts/corta.js
         - name: Logs de Compose
           if: always()
           run: docker compose logs --no-color > compose.log 2>&1 || true
         - name: Publicar reportes de humo, carga y logs
           if: always()
           uses: actions/upload-artifact@330a01c490aca151604b8cf639adc76d48f6c5d4 # v5.0.0
           with:
             name: reportes-despliegue-y-verificacion
             path: |
               humo-flujo.txt
               k6-resumen.json
               compose.log
             if-no-files-found: warn
         - name: Bajar Compose y borrar el volumen
           if: always()
           run: docker compose down -v
   ```
3. Confirma los SHAs nuevos: no estaban en la lista verificada.
   ```bash
   gh api repos/docker/setup-buildx-action/commits/v4.4.1 --jq .sha   # f87e5991a6d7451dcb8d9637bfbc97413f497069
   gh api repos/docker/build-push-action/commits/v7.4.0 --jq .sha     # c3c9e263c25d99ce0380d002d59b67737d91b0dc
   gh api repos/actions/download-artifact/commits/v5.0.0 --jq .sha    # 634f93cb2916e3fdff6788551b99b062d0335ce0
   gh api repos/grafana/setup-k6-action/commits/v1.2.2 --jq .sha      # 43b9fc21641a76002687994433dd586f56e791b1
   ```
   Si alguno no coincide, usa el SHA que devuelve el comando, con su comentario de versión.
4. Recomendado: cambia `grafana/setup-k6-action` por el paso "Instalar k6" de `.github/workflows/carga-larga.yml` (M4), que instala k6 v2.2.0 desde el tarball y valida su SHA-256. Copia también `K6_VERSION` y `K6_SHA256` al `env:` del job. Es una acción de terceros menos (T11, A8) y la misma instalación en los dos workflows.
5. Push y PR. Cuando salgan los checks, avísale a Santi para marcar `imagen-y-escaneo` y `despliegue-y-verificacion` como obligatorios.

**Cómo comprobarlo** (sin probar todavía).
- Los dos checks en verde, con los artefactos `reporte-imagen` y `reportes-despliegue-y-verificacion` (`humo-flujo.txt`, `k6-resumen.json` y `compose.log`).
- Rojo 1: rama `prueba/imagen-rojo` con `commons-text 1.9` en `pom.xml`. `imagen-y-escaneo` queda en rojo, por una CRITICAL dentro del jar (y `SCA` también).
- Rojo 2: rama `prueba/config-local`.
  ```bash
  echo "window.PETFINDER_CLAVE_CARTO = 'falsa';" > src/main/resources/static/js/config-local.js
  # quita la línea src/main/resources/static/js/config-local.js de .dockerignore
  git add -f src/main/resources/static/js/config-local.js .dockerignore
  ```
  Commit y push: el paso "El jar de la imagen no trae config-local.js" queda en rojo.
- Guarda los enlaces de las corridas en rojo (para S11, `devops.md` y la presentación) y borra las ramas.

**Trampas.**
- `docker compose up --no-build` necesita `image: pet-finder:local` en Compose (A3), y `cp .env.example .env` necesita un `POSTGRES_PASSWORD` no vacío en `.env.example` (A3).
- `corta.js` manda 33 peticiones por segundo (11 iteraciones de 3 peticiones). Si el p95 pasa de 500 ms, mira `compose.log` antes que nada. Nunca bajes un umbral para que pase.
- Después de S10, el límite de peticiones no puede frenar a k6: las variables de `/api/voz` y `/api/reportes` tienen que quedar altas en `.env.example` o en el paso que levanta Compose.
- Después de A9, `corta.js` y `humo-flujo.sh` tienen que mandar el código de gestión, o este check queda en rojo por 403.
- El artefacto `imagen-pet-finder` dura un día: solo pasa la imagen al job siguiente.

**Pedido para tu agente:** "Agrega a `ci.yml` los dos jobs de la sección A6, verifica los cuatro SHAs con `gh api` y cambia la instalación de k6 por la de `carga-larga.yml`. Muéstrame el diff antes de que yo haga commit."

---

## A7 · Clave de CARTO en tiempo de ejecución

Tramo 1 · esfuerzo S · controlador y prueba **probados en local** (gate en verde). Los cambios en Dockerfile, Compose y `.env.example` no estaban en esa prueba: **sin probar todavía**.

**Para qué.** DevSecOps (20), amenaza T11: hoy la clave queda dentro de la imagen como build arg, y la imagen se va a publicar en un GHCR público. Leída al arrancar, una sola imagen sirve para todos los entornos. **Tiene que entrar antes de que M5 publique la primera imagen.**

**Depende de:** nada (mejor con A3 ya integrado, porque los dos tocan el Dockerfile). **Desbloquea:** M5, A6, A13.

**Archivos:** `src/main/java/petfinder/adaptadores/entrada/web/ConfiguracionMapaController.java` (nuevo), `src/test/java/petfinder/adaptadores/entrada/web/ConfiguracionMapaControllerTest.java` (nuevo), `Dockerfile`, `docker-compose.yml`, `.env.example`.

**Cómo hacerlo.**
1. `git switch main && git pull && git switch -c corte3/A7`
2. Controlador:
   ```java
   package petfinder.adaptadores.entrada.web;

   import java.nio.charset.StandardCharsets;

   import org.springframework.beans.factory.annotation.Value;
   import org.springframework.http.CacheControl;
   import org.springframework.http.MediaType;
   import org.springframework.http.ResponseEntity;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.RestController;

   /**
    * Entrega al navegador la clave de CARTO para las teselas del mapa, leída al
    * arrancar desde la variable de entorno {@code PETFINDER_CLAVE_CARTO}.
    *
    * Antes la clave se escribía en {@code js/config-local.js} al construir la
    * imagen, así que quedaba dentro de una imagen que se publica en un registro
    * público y había que reconstruir para cambiarla. Leerla al arrancar deja una
    * sola imagen para todos los entornos y ningún valor de configuración adentro.
    * La clave sigue siendo visible en el navegador por diseño: viaja en cada
    * petición de teselas.
    *
    * Responde en la misma ruta que ya carga {@code index.html}, y con 200 y cuerpo
    * vacío si no hay clave, para que el mapa caiga a OpenStreetMap sin un error en
    * la consola. Solo se dejan letras, números, guion y guion bajo: el valor
    * termina dentro de un script y no puede cerrar la cadena.
    */
   @RestController
   public class ConfiguracionMapaController {

       private static final MediaType JAVASCRIPT = new MediaType("text", "javascript", StandardCharsets.UTF_8);

       private final String claveCarto;

       public ConfiguracionMapaController(@Value("${PETFINDER_CLAVE_CARTO:}") String claveCarto) {
           this.claveCarto = claveCarto.replaceAll("[^A-Za-z0-9_-]", "");
       }

       @GetMapping("/js/config-local.js")
       public ResponseEntity<String> configuracion() {
           String cuerpo = claveCarto.isEmpty() ? "" : "window.PETFINDER_CLAVE_CARTO = '" + claveCarto + "';\n";
           return ResponseEntity.ok().contentType(JAVASCRIPT).cacheControl(CacheControl.noCache()).body(cuerpo);
       }
   }
   ```
3. Prueba:
   ```java
   package petfinder.adaptadores.entrada.web;

   import static org.junit.jupiter.api.Assertions.assertEquals;
   import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
   import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
   import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

   import org.junit.jupiter.api.DisplayName;
   import org.junit.jupiter.api.Test;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
   import org.springframework.test.web.servlet.MockMvc;

   /**
    * La clave de CARTO llega por variable de entorno y nunca rompe el script:
    * sin clave, 200 y cuerpo vacío; con una clave rara, solo sus caracteres válidos.
    */
   @WebMvcTest(controllers = ConfiguracionMapaController.class, properties = "PETFINDER_CLAVE_CARTO=")
   class ConfiguracionMapaControllerTest {

       @Autowired
       private MockMvc mvc;

       @Test
       @DisplayName("Sin PETFINDER_CLAVE_CARTO, /js/config-local.js responde 200 con cuerpo vacío")
       void sinClaveRespondeVacio() throws Exception {
           // Act & Assert
           mvc.perform(get("/js/config-local.js"))
                   .andExpect(status().isOk())
                   .andExpect(content().contentTypeCompatibleWith("text/javascript"))
                   .andExpect(content().string(""));
       }

       @Test
       @DisplayName("Con clave, el script define window.PETFINDER_CLAVE_CARTO con ese valor")
       void conClaveDefineLaVariable() {
           // Arrange
           ConfiguracionMapaController controlador = new ConfiguracionMapaController("clave_de-prueba123");

           // Act
           String cuerpo = controlador.configuracion().getBody();

           // Assert
           assertEquals("window.PETFINDER_CLAVE_CARTO = 'clave_de-prueba123';\n", cuerpo);
       }

       @Test
       @DisplayName("Una clave con comillas o paréntesis no puede cerrar la cadena del script")
       void claveConCaracteresRarosSeSanea() {
           // Arrange
           ConfiguracionMapaController controlador = new ConfiguracionMapaController("abc'); alert(1); //");

           // Act
           String cuerpo = controlador.configuracion().getBody();

           // Assert
           assertEquals("window.PETFINDER_CLAVE_CARTO = 'abcalert1';\n", cuerpo);
       }
   }
   ```
4. `Dockerfile`: borra el bloque completo que va desde el comentario `# Clave gratuita de CARTO para las teselas del mapa (opcional)...` hasta el `fi`, es decir, el `ARG PETFINDER_CLAVE_CARTO` y el `RUN` que escribe `config-local.js`.
5. `docker-compose.yml`, en `environment` de `app`, debajo de las variables de IA:
   ```yaml
         # Clave pública de CARTO para el mapa (opcional): se lee al arrancar; sin ella, OpenStreetMap
         PETFINDER_CLAVE_CARTO: ${PETFINDER_CLAVE_CARTO:-}
   ```
6. `.env.example`:
   ```bash
   # Clave gratuita de CARTO para las teselas del mapa (opcional; sin ella, OpenStreetMap).
   # Es pública por diseño: viaja en cada petición del navegador. https://carto.com/basemaps/apikey
   PETFINDER_CLAVE_CARTO=
   ```
7. No toques `static/` (es zona de la skill de diseño). Avísale a Mateo que el comentario de `index.html` (línea 213) y `js/config-local.ejemplo.js` quedan desactualizados: la clave ahora va en `.env`.
8. Gate, commit, push y PR. Cuando entre, avísale a Mateo: M5 ya puede publicar la imagen y M6 tiene que crear `PETFINDER_CLAVE_CARTO` en Render.

**Cómo comprobarlo.**
```bash
./mvnw -q test -Dtest=ConfiguracionMapaControllerTest              # 3 pruebas en verde
docker compose up -d --build --wait
curl -s -i http://localhost:8080/js/config-local.js | head -5        # 200, text/javascript y cuerpo vacío
# En .env: PETFINDER_CLAVE_CARTO=<tu-clave-de-carto>
docker compose up -d --wait                                           # recrea app con la variable, misma imagen
docker compose exec app env | grep PETFINDER_CLAVE_CARTO
curl -s http://localhost:8080/js/config-local.js                     # window.PETFINDER_CLAVE_CARTO = '...';
```
En el navegador: con la clave, teselas de CARTO; sin ella, OpenStreetMap. Todo con la misma imagen. La parte con Docker está sin probar todavía.

**Trampas.**
- Si tenías `src/main/resources/static/js/config-local.js` en tu computador, deja de usarse: el controlador tiene prioridad sobre el archivo estático. Pon la clave en `.env`.
- `.gitignore` y `.dockerignore` siguen excluyendo `config-local.js`. Déjalos así.
- No pegues tu clave en el PR, aunque sea pública.
- Solo pasan letras, números, guion y guion bajo. Cualquier otro carácter se recorta.

**Pedido para tu agente:** "Crea el controlador y la prueba de la sección A7 tal cual, aplica los pasos 4 a 6 y corre `ConfiguracionMapaControllerTest` y el gate. No toques `static/`."

---

## A8 · Endurecer los workflows

Tramo 2 · esfuerzo S · **sin parche.** Los workflows de A2, A5 y A6 ya vienen con SHAs y `permissions: contents: read`. La revisión de SHAs del paso 5 se probó en local sobre esos workflows y sobre `carga-larga.yml`; en Actions está **sin probar todavía**.

**Para qué.** Integración continua (15): un pipeline de menos de 10 minutos, con el enlace que lo demuestra. DevSecOps (20) y el tema de seguridad del profesor: cadena de suministro (T11). Trivy tuvo el aviso GHSA-69fq-xp46-6x23 (crítico, marzo de 2026).

**Depende de:** A2, A5, A6, S5, S6 y M7. **Alimenta:** A12 (el enlace del tiempo va en `devops.md`).

**Archivos:** `.github/workflows/*.yml`.

**Cómo hacerlo.**
1. `git switch main && git pull && git switch -c corte3/A8`
2. Inventario: `grep -rn 'uses:' .github/workflows`. Cada acción externa va con SHA de 40 caracteres y un comentario `# vX.Y.Z`. Para sacar un SHA: `gh api repos/<dueño>/<acción>/commits/<tag> --jq .sha`. Comprueba también que cada comentario de versión corresponde a su SHA: los de `actions/checkout` (`# v5.1.0`), `actions/setup-java` (`# v5.7.0`) y `actions/upload-artifact` (`# v5.0.0`) solo están confirmados como de la línea v5. Si el comando devuelve otro SHA para ese tag, deja el SHA y corrige el comentario con la versión real (`gh api repos/<dueño>/<acción>/tags --jq '.[] | select(.commit.sha=="<sha>") | .name'`).
3. `permissions: contents: read` arriba en cada workflow. Los permisos extra solo en el job que los necesita (por ejemplo, `security-events: write` en CodeQL o `packages: write` en el job de cd.yml que publica).
4. Secretos solo en el job de despliegue: `grep -n 'secrets\.' .github/workflows/*.yml` solo debe mostrar líneas de los jobs de `cd.yml` y `revertir.yml` que llevan `environment: produccion`. Si aparece en otro lado, avísale a Mateo.
5. Job nuevo en `ci.yml`, sin `needs`, para que corra en paralelo:
   ```yaml
     # Amenaza T11: una acción referenciada por tag puede cambiar por debajo; por SHA, no.
     acciones-fijadas:
       runs-on: ubuntu-24.04
       timeout-minutes: 5
       steps:
         - uses: actions/checkout@fbc6f3992d24b796d5a048ff273f7fcc4a7b6c09 # v5.1.0
         - name: Toda acción externa va fijada por un SHA de 40 caracteres
           run: |
             malas=$(grep -RnE '^[[:space:]]*(-[[:space:]]+)?uses:[[:space:]]*[^.[:space:]]' .github/workflows \
               | grep -vE 'uses:[[:space:]]*[^@[:space:]]+@[0-9a-f]{40}([[:space:]]|$)' || true)
             if [ -n "$malas" ]; then
               echo "::error::Acciones sin SHA de 40 caracteres:"
               echo "$malas"
               exit 1
             fi
             echo "Todas las acciones externas están fijadas por SHA."
   ```
   Las acciones locales (`uses: ./...`) no se revisan. Una imagen `docker://` se marca siempre, incluso con digest `@sha256:`: si algún día hace falta una, ajusta el patrón en ese PR.
6. Tiempo: abre una corrida completa de CI en `main` y la de Seguridad del mismo commit (corren en paralelo y las dos son parte del pipeline de integración), y mide desde el inicio hasta que terminan todos sus jobs menos `despliegue-y-verificacion` (es despliegue y carga, y la rúbrica no lo cuenta). Si Seguridad termina después que CI, el tiempo es hasta el fin de Seguridad. Para ver los tiempos de cada job: `gh run view <id-de-la-corrida> --json jobs --jq '.jobs[] | [.name, .startedAt, .completedAt] | @tsv'`. Si pasa de 10 minutos, revisa la caché (`cache: maven` y `type=gha`) antes de quitar pasos. Guarda el enlace para `devops.md` (A12).
7. Gate, commit, push y PR. Pídele a Santi marcar `acciones-fijadas` como obligatorio.

**Cómo comprobarlo** (sin probar todavía en Actions).
- Rama desechable con un `uses: actions/checkout@v5` en cualquier workflow: `acciones-fijadas` queda en rojo y muestra la línea.
- Todos los workflows en verde con SHAs.
- Una corrida completa, sin despliegue ni carga, en menos de 10 minutos, con su enlace en `devops.md`.

**Trampas.**
- Dependabot (A5) sube el SHA y el comentario de versión en sus PR. Revísalos.
- `cd.yml` y `revertir.yml` son de Mateo: si hay que cambiarles algo, va en tu PR solo si él está de acuerdo, o se lo pasas.

**Pedido para tu agente:** "Haz los pasos 2 a 5 de la sección A8 sobre todos los workflows de `main` y dime qué acciones o permisos no cumplen. No cambies `cd.yml` ni `revertir.yml` sin preguntarme."

---

## A9 · Código de gestión: web

Tramo 2 · esfuerzo M · **sin parche.** Depende de cómo quede S9.

**Para qué.** DevSecOps (20), amenaza T1: hoy cualquiera cierra el caso de otra familia (verificado: 204 sin credenciales, incluso desde otra página). Es el pendiente 4 del Corte 2 (decisión D1: código de gestión más código de administrador).

**Depende de:** S9 (núcleo, Santi). **Desbloquea:** M10 (interfaz) y A10.

**Archivos:** `ReporteController.java`, `ManejadorErrores.java` y `dto/` en `src/main/java/petfinder/adaptadores/entrada/web/`; `VozController.java` si Santi no lo cubre en S9; `perf/scripts/corta.js`; `scripts/humo-flujo.sh`; pruebas nuevas en `src/test/java/petfinder/adaptadores/entrada/web/` y `src/test/java/petfinder/integracion/sistema/`.

**Cómo hacerlo.**
1. Cuando S9 esté en `main`: `git switch main && git pull && git switch -c corte3/A9`. Lee qué dejó S9: la firma nueva de resolver y cerrar en `GestionReportes`, la excepción de código inválido y la variable `PETFINDER_CODIGO_ADMIN`.
2. `ReporteController`: en `resolver` y `cerrar`, recibe `@RequestHeader(name = "X-Codigo-Gestion", required = false) String codigoGestion` y pásalo al puerto. Sin encabezado debe llegar al núcleo como ausente, para que el núcleo responda con su error.
3. `ManejadorErrores`: un `@ExceptionHandler` para la excepción de S9 que responda `HttpStatus.FORBIDDEN` con su mensaje en español. Spring elige el manejador más específico, así que convive con el de `DominioException`.
4. Respuesta de publicar: `crear` devuelve también `codigoGestion`, y ninguna otra respuesta lo trae. Agrégalo sin cambiar el resto de la forma del JSON (por ejemplo, un record de respuesta con los campos de `ReporteDTO` más `codigoGestion`), para no romper la interfaz ni las UIT antes de M10. Acuerda el nombre del campo con Mateo.
5. Voz: S9 dice que la respuesta de voz también devuelve el código. Acuerda con Santi quién toca `VozController`, que M1 también cambió (haz `git merge main` antes).
6. `perf/scripts/corta.js`: en la limpieza, manda el código que recibió al registrar. Hoy el id sale de `registro.json('reportes.0.id')`. Para ver dónde dejó S9 el código, con la app arriba:
   ```bash
   curl -s -X POST http://localhost:8080/api/voz -H 'Content-Type: application/json' -d '{"texto":"perdí un perro llamado Max en Chía","nombreContacto":"Prueba","medioContacto":"3001112233"}'
   ```
   y usa esa ruta (por ejemplo `reportes.0.<campo>`) en lugar del texto entre `< >`:
   ```js
   const codigo = registro.json('<ruta del código en la respuesta de voz>');
   const cierre = http.post(`${BASE}/api/reportes/${id}/cerrar`, null, {
     headers: { 'X-Codigo-Gestion': codigo }, tags: { name: 'limpieza' },
   });
   ```
7. `scripts/humo-flujo.sh`: guarda el código después del POST y úsalo al cerrar. El script ya usa `codigo` para el estado HTTP, así que la variable nueva se llama distinto:
   ```sh
   CODIGO_GESTION=$(grep -o '"codigoGestion":"[^"]*"' "$CUERPO" | head -n 1 | cut -d '"' -f 4)
   [ -n "$CODIGO_GESTION" ] || fallar "POST /api/reportes no devolvió codigoGestion"
   # ...
   codigo=$(pedir -X POST "$BASE/api/reportes/$ID/cerrar" -H "X-Codigo-Gestion: $CODIGO_GESTION")
   ```
8. Pruebas, con `// Arrange`, `// Act`, `// Assert` y `@DisplayName` en español:
   - Capa web, en una clase nueva (`CodigoGestionWebTest`) para no chocar con `ControladoresWebTest`, que M1 cambió: 403 sin encabezado, 403 con un código equivocado y 204 con el correcto.
   - Sistema, `CodigoGestionSistemaIT`: publica dos casos por HTTP (sin IDs fijos). Cerrar el primero sin código da 403, con el código del segundo da 403, con su propio código da 204, y el código de administrador cierra el segundo con 204. Ni `GET /api/reportes` ni `GET /api/reportes/{id}` contienen el código. Con `ANTHROPIC_API_KEY=` y un `PETFINDER_CODIGO_ADMIN` de prueba en `properties`.
9. Gate, commit, push y PR. Mateo revisa y luego hace M10 sobre esto.

**Cómo comprobarlo.**
```bash
./mvnw -q test -Dtest=CodigoGestionWebTest
./mvnw -q verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=CodigoGestionSistemaIT -Djacoco.skip=true
./mvnw -q clean verify
```
Con Compose arriba, `sh scripts/humo-flujo.sh http://localhost:8080` sale 0 y `BASE_URL=http://localhost:8080 k6 run perf/scripts/corta.js` sale 0. En el PR, `despliegue-y-verificacion` queda en verde.

**Trampas.**
- Si `corta.js` no manda el código, k6 cuenta los 403 como errores y el despliegue queda en rojo en todos los PR.
- El código solo se devuelve una vez. No lo escribas en logs ni en mensajes de error.

**Pedido para tu agente:** "Lee lo que dejó S9 en `main` y aplica la sección A9: encabezado, 403, código solo al publicar, `corta.js`, `humo-flujo.sh` y las dos pruebas. Pregúntame antes de tocar `VozController`."

---

## A10 · Menos datos personales en casos cerrados

Tramo 2 · esfuerzo S · **sin parche.**

**Para qué.** DevSecOps (20), amenaza T5: en Render, `GET /api/reportes/{id}` devuelve el contacto completo y coordenadas de 4 decimales a cualquiera, aunque el caso ya esté cerrado. El caso activo sigue con el contacto completo, porque quien encuentra la mascota necesita llamar.

**Depende de:** nada (mejor después de A9, porque la prueba cierra un caso). **Alimenta:** S11 (la mitigación de T5 en `seguridad.md`).

**Archivos:** `src/main/java/petfinder/adaptadores/entrada/web/dto/ReporteDTO.java` y una IT nueva en `src/test/java/petfinder/integracion/sistema/`.

**Cómo hacerlo.**
1. `git switch main && git pull && git switch -c corte3/A10`
2. En `ReporteDTO.construir`, separa "vista pública" de "datos protegidos". No reutilices `publico = true` para el detalle, porque también borra los avistamientos y el hallazgo:
   ```java
   // Un caso RESUELTO o CERRADO ya no necesita que lo llamen (amenaza T5): se enmascara el
   // contacto y se redondea la ubicación, igual que en la lista, pero se conservan los avistamientos.
   boolean protegido = publico || reporte.getEstado() != EstadoReporte.ACTIVO;
   Ubicacion ubicacion = protegido ? reporte.getUbicacion().aproximada() : reporte.getUbicacion();
   // ...
   protegido ? enmascarar(medio) : medio,
   ```
3. `HallazgoDTO.desde(Avistamiento avistamiento, boolean protegido)`: con `protegido`, `contacto` pasa por el mismo `enmascarar`.
4. `Ubicacion.aproximada()` ya redondea a 3 decimales. `AvistamientoPublicoDTO.desde` hoy copia `latitud` y `longitud` exactas del avistamiento: dale también el parámetro `protegido` y, con él, usa `avistamiento.ubicacion().aproximada()`.
5. IT `CasoCerradoSistemaIT`: publica un caso con `medioContacto` `3001234567` y una latitud de 7 decimales, ciérralo (con el código si A9 ya entró) y pide `GET /api/reportes/{id}`. El contacto trae `•` y la latitud y la longitud tienen 3 decimales o menos. Un segundo caso activo sigue con el contacto completo.
6. Gate, commit, push y PR. Avísale a Mateo: la página de un caso cerrado va a mostrar el contacto enmascarado.

**Cómo comprobarlo.**
```bash
./mvnw -q verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=CasoCerradoSistemaIT -Djacoco.skip=true
./mvnw -q clean verify
```

**Trampas.**
- Las pruebas de `ReporteDTO` que ya existen tienen que seguir en verde: el caso activo no cambia.

**Pedido para tu agente:** "Aplica la sección A10 en `ReporteDTO` sin cambiar la vista del caso activo, escribe `CasoCerradoSistemaIT` y corre el gate."

---

## A11 · Selenium para Cerca de ti y coincidencias (pendiente 6)

Tramo 2 · esfuerzo M · **sin parche.**

**Para qué.** Es el pendiente 6 del Corte 2 (sale en `evolucion.md`, S14) y suma a Integración continua (15), porque `pruebas-ui` pasa a check obligatorio.

**Depende de:** A2. **Alimenta:** S14 (`evolucion.md`, pendiente 6).

**Archivos:** `src/test/java/petfinder/ui/` (una clase `*UIT` nueva y sus Page Objects).

**Cómo hacerlo.**
1. `git switch main && git pull && git switch -c corte3/A11`
2. Lee `FlujosPrincipalesUIT`, `PaginaInicio` y `PaginaNuevoReporte`, y sigue el mismo estilo: Chrome sin ventana y Page Objects.
3. Clase nueva `FlujosCercaYCoincidenciasUIT` con dos flujos:
   - **Cerca de ti.** Arrange: publica por API dos casos, uno a unos cientos de metros de la posición simulada y otro lejos, fuera del radio de `app.js` (`RADIO_KM = 5`, línea 27; por ejemplo a 20 km). Act: toca el botón "Casos cerca de ti" del mapa. Assert: el título de la lista dice "Cerca de ti", el caso cercano aparece y el lejano no.
   - **Posibles coincidencias.** Lee `app.js` desde la línea 358 y `index.html` líneas 90 a 93 para armar el flujo de "La tengo yo" desde un hallazgo. Assert: el bloque `[data-rol=coincidencias]` se muestra y lista el caso publicado.
4. La ubicación se simula con el protocolo de Chrome (sin probar todavía), antes de abrir la página. Importa `java.util.List`, `java.util.Map` y `org.openqa.selenium.chrome.ChromeDriver`:
   ```java
   // Chrome sin ventana no tiene GPS: el protocolo de Chrome da el permiso y fija la posición.
   ChromeDriver chrome = (ChromeDriver) navegador;
   chrome.executeCdpCommand("Browser.grantPermissions", Map.of("permissions", List.of("geolocation")));
   chrome.executeCdpCommand("Emulation.setGeolocationOverride",
           Map.of("latitude", 4.6533, "longitude", -74.0836, "accuracy", 50));
   ```
5. Nombres únicos para los casos de prueba (por ejemplo, con la hora) y nunca un ID fijo.
6. Gate, `-Pui verify`, commit, push y PR.
7. Después de 3 corridas seguidas en verde de `pruebas-ui` en Actions, pídele a Santi que la marque como obligatoria.

**Cómo comprobarlo.**
```bash
./mvnw -q -Pui verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=FlujosCercaYCoincidenciasUIT -Djacoco.skip=true
./mvnw -q -Pui verify
```
En Actions, el reporte `reportes-pruebas-ui` muestra 4 flujos: los 2 que ya existían y los 2 nuevos.

**Trampas.**
- Si un flujo falla a veces en Actions y no en local, casi siempre es una espera: usa `WebDriverWait` sobre el elemento, nunca `Thread.sleep`.
- Después de M10, publicar desde la interfaz muestra el código de gestión: las UIT no deben depender de que no aparezca.

**Pedido para tu agente:** "Lee las UIT y Page Objects que existen y escribe los dos flujos de la sección A11 con la ubicación simulada por el protocolo de Chrome. Córrelos con el comando de una sola UIT."

---

## A12 · `docs/devops.md`

Tramo 3 · esfuerzo M · **sin parche.**

**Para qué.** Documentación (10): el enunciado pide `devops.md` con el diagrama del pipeline, qué hace cada etapa, qué umbral la detiene y por qué ese valor, y el procedimiento de reversión. Mateo aporta la parte de despliegue (`cd.yml` y `revertir.yml`).

**Depende de:** A6, M7 y M8. **Alimenta:** A13 (la guía puede enlazarlo).

**Archivos:** `docs/devops.md` (nuevo) y una fila en `docs/README.md` si M3 ya entró.

**Cómo hacerlo.**
1. `git switch main && git pull && git switch -c corte3/A12`
2. Diagrama en Mermaid. Ajústalo a los jobs reales de cada workflow:
   ```mermaid
   flowchart LR
     subgraph PR["Push y pull request: ci.yml y seguridad.yml"]
       C[construccion] --> U[pruebas-unitarias<br/>cobertura ≥ 70 %]
       C --> R[reglas-arquitectura]
       C --> I[pruebas-integracion]
       C --> UI[pruebas-ui]
       C --> IM[imagen-y-escaneo<br/>Trivy CRITICAL]
       U & R & I & IM --> D[despliegue-y-verificacion<br/>Compose, humo y k6]
       SEC[Secretos] ~~~ SAST[SAST] ~~~ SCA[SCA]
     end
     subgraph CD["main: cd.yml"]
       P[imagen en GHCR con el SHA] --> H[deploy hook de Render] --> V[humo y k6 contra la URL]
     end
     D -. merge a main .-> P
   ```
3. Una tabla con una fila por check: etapa del enunciado (tabla 2.2), check, workflow y job, herramienta, qué lo detiene, por qué ese umbral y qué artefacto publica. Umbrales: cobertura del dominio de 70 % (rúbrica; el dominio está en 97 %, medido en `BUNDLE` y no por paquete), Secretos cero hallazgos, SAST con severidad de 7,0 o más, SCA con HIGH o CRITICAL con corrección, imagen con CRITICAL con corrección (los HIGH quedan en el reporte; el porqué está en ADR-003), y despliegue con p95 menor a 500 ms, errores menores a 1 % y al menos 1800 peticiones (el SLO del Corte 2).
4. Secciones: caché y tiempo (con el enlace de A8), corrida diaria (`schedule`), versionado con tags `v*`, despliegue (Mateo) y reversión: cómo correr `revertir.yml` con un tag de imagen, qué comprueba y el enlace a la ejecución real (M8).
5. Gate, commit, push y PR.

**Cómo comprobarlo.**
```bash
sh scripts/contiene.sh docs/devops.md 'construccion' 'pruebas-unitarias' 'reglas-arquitectura' 'pruebas-integracion' 'pruebas-ui' 'Secretos' 'SAST' 'SCA' 'imagen-y-escaneo' 'despliegue-y-verificacion' 'acciones-fijadas' 'revertir.yml' 'actions/runs/' 'mermaid'
for j in $(awk '{sub(/\r$/,"")} /^jobs:/{f=1;next} /^[^ ]/{f=0} f && /^  [a-z0-9-]+:$/{gsub(/[ :]/,"");print}' .github/workflows/*.yml | sort -u); do grep -q -- "$j" docs/devops.md || echo "Falta en devops.md: $j"; done
```
El segundo comando no debe imprimir nada: cada job de cada workflow, también los de `cd.yml`, aparece en `devops.md`. El `sub(/\r$/,"")` es para Windows: con `* text=auto` los `.yml` pueden quedar con CRLF y, sin él, el comando no encuentra ningún job y no imprime nada aunque falten. Para ver que sí lee los jobs, corre solo la parte de `awk`: debe listar `construccion`, `sca` y los demás.

**Trampas.**
- Los umbrales del documento tienen que ser los mismos de los workflows. Si alguien cambia uno, cambia los dos.

**Pedido para tu agente:** "Escribe `docs/devops.md` según la sección A12, leyendo los workflows de `main` para nombres y umbrales, y deja marcados los huecos de despliegue que me tiene que pasar Mateo. Corre las dos comprobaciones."

---

## A13 · `docs/implementacion.md`: la guía

Tramo 3 · esfuerzo L · **sin parche.**

**Para qué.** Guía de implementación (10 pts, criterio propio): alguien que conoce Docker y Git, pero no el proyecto, llega al sistema funcionando sin preguntarle nada al grupo.

**Depende de:** S7, S9, S10, S11, A7, M7, M8 y M12. **Desbloquea:** la entrega del criterio Guía de implementación.

**Archivos:** `docs/implementacion.md` (nuevo) y la evidencia de la prueba en `docs/evidencias/`.

**Cómo hacerlo.**
1. `git switch main && git pull && git switch -c corte3/A13`
2. Las 9 partes, con estos títulos exactos:
   - `## 1. Qué se va a implementar`: el sistema en un párrafo, los componentes (app en Render con la imagen de GHCR y PostgreSQL) y el enlace a la vista de despliegue (M12).
   - `## 2. Requisitos previos`: cuentas (GitHub, Render y el registro GHCR), software con versión (Git, Docker, JDK 17 solo para desarrollar; el wrapper baja Maven 3.9.16), recursos mínimos, puerto 8080 y HTTPS (Render lo da).
   - `## 3. Costos estimados`: Render gratis con su PostgreSQL gratis (vence a los 30 días) y el plan recomendado (Starter, 7 USD al mes, 0,5 CPU, según D2), más una base PostgreSQL de pago, porque la gratis vence y el negocio no puede perder sus casos. Actions y GHCR gratis en un repo público. Anthropic opcional, con el tope de `PETFINDER_IA_TOPE_DIARIO`. CARTO gratis. Revisa los precios el día que escribas y anota la fecha.
   - `## 4. Configuración`: tabla con nombre, para qué sirve, valor de ejemplo, si es obligatoria, dónde se configura (`.env`, GitHub Secrets o el panel de Render) y cómo generar los valores sensibles (por ejemplo `openssl rand -hex 32`, que funciona en Git Bash). Incluye las de `.env.example`, `docker-compose.yml`, los secretos de los workflows (`RENDER_DEPLOY_HOOK_URL`) y las de S9 y S10.
   - `## 5. Instalación paso a paso`: desde una cuenta vacía hasta el sistema funcionando: fork, ajustes del repo, secretos, primera imagen, servicio en Render, base, primer despliegue, datos iniciales y primer administrador (`PETFINDER_CODIGO_ADMIN`). Cada paso con su comando o acción y el resultado esperado. Para los pasos en una consola web valen capturas.
   - `## 6. Verificación`: healthcheck, `sh scripts/humo-flujo.sh https://<url-de-render>` y un recorrido corto: publicar, ver en el mapa y cerrar con el código.
   - `## 7. Operación`: nueva versión (tag `v*`), reversión (`revertir.yml`), copia de seguridad y restauración, cambio de un secreto, logs y reinicio. Copia y restauración con Compose (sin probar todavía):
     ```bash
     docker compose exec -T db sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' > respaldo-$(date +%F).dump
     docker compose exec -T db sh -c 'pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --clean --if-exists' < respaldo-AAAA-MM-DD.dump
     ```
     Para Render, `pg_dump` con la "External Database URL" del panel y un cliente de PostgreSQL 18.
   - `## 8. Solución de problemas`: al menos 5, cada uno con síntoma, causa y solución. Candidatos: Compose dice "Defina POSTGRES_USER" (no se copió `.env`); el puerto 8080 está ocupado; Render gratis dormido hace fallar la prueba de humo (`HUMO_ESPERA` o plan Starter); Flyway no arranca con un volumen viejo; 403 al cerrar (falta el código); 429 en el asistente (S10); el mapa sin CARTO; la base gratis vencida; un pipeline en rojo por una vulnerabilidad nueva sin que nadie tocara el código.
   - `## 9. Recomendaciones para producción`: credenciales de ejemplo, HTTPS, copias programadas, tamaño de los recursos y los riesgos aceptados de `docs/seguridad.md` (S11).
3. Los comandos van completos, listos para copiar, con los valores que cambian marcados así: `<usuario-github>`.
4. La prueba una persona fuera del grupo, en un fork, con cuentas nuevas de Render y GHCR, anotando cada duda. Si no se consigue, la prueba Santi siguiendo solo la guía. Corrige cada duda y guarda el registro en `docs/evidencias/2026-11-14_antonio_prueba-guia.md`.
5. Gate, commit, push y PR.

**Cómo comprobarlo.**
```bash
sh scripts/contiene.sh docs/implementacion.md '## 1. Qué se va a implementar' '## 2. Requisitos previos' '## 3. Costos estimados' '## 4. Configuración' '## 5. Instalación paso a paso' '## 6. Verificación' '## 7. Operación' '## 8. Solución de problemas' '## 9. Recomendaciones para producción' 'pg_dump' 'pg_restore'
for v in $(grep -oE '^[A-Z_]+=' .env.example | tr -d '='); do grep -q "$v" docs/implementacion.md || echo "Falta en la guía: $v"; done
for s in $(grep -ohE 'secrets\.[A-Z_]+' .github/workflows/*.yml | cut -d. -f2 | sort -u); do grep -q "$s" docs/implementacion.md || echo "Falta en la guía: $s"; done
for v in $(grep -oE '\$\{[A-Z_]+' docker-compose.yml | cut -c3- | sort -u); do grep -q "$v" docs/implementacion.md || echo "Falta en la guía: $v"; done
```
Los tres `for` no deben imprimir nada (`GITHUB_TOKEN` es la única excepción válida del segundo: GitHub lo crea solo). Y lo principal: la persona de afuera llegó al sistema funcionando siguiendo solo la guía.

**Trampas.**
- Spring lee `petfinder.datos-ejemplo` desde la variable `PETFINDER_DATOSEJEMPLO` (sin el guion). Confírmalo antes de escribirlo en la tabla.
- La guía puede enlazar a `devops.md` o al README, pero los pasos tienen que estar en la guía.

**Pedido para tu agente:** "Escribe el borrador de `docs/implementacion.md` con las 9 partes de la sección A13, sacando nombres de `.env.example`, `docker-compose.yml` y los workflows. Marca lo que yo tengo que comprobar en Render y corre las comprobaciones de la sección."

---

## A14 · Comprobación de cierre

Tramo 3 · esfuerzo S · domingo 15 · **sin parche.** Estás de turno el sábado 14 y Mateo el 15, por si una vulnerabilidad nueva pone `main` en rojo.

**Para qué.** Integración continua (15) y Despliegue continuo (15): la calificación mira el último commit de `main`. Si está en rojo, los criterios 2, 3 y 5 (45 puntos) quedan como máximo en Básico.

**Depende de:** S2, M7 y M11.

**Cómo hacerlo** (contra la rúbrica).
1. Cada etapa de la tabla 2.2 es un check obligatorio en `main`:
   ```bash
   gh api repos/DrearSanti/pet-finder-dyas/rules/branches/main --jq '.[] | select(.type=="required_status_checks") | .parameters.required_status_checks[].context'
   ```
   Deben salir `construccion`, `pruebas-unitarias`, `reglas-arquitectura`, `pruebas-integracion`, `pruebas-ui`, `Secretos`, `SAST`, `SCA`, `imagen-y-escaneo`, `despliegue-y-verificacion` y `acciones-fijadas`.
2. El repo es público y Actions se ve: `gh repo view DrearSanti/pet-finder-dyas --json visibility --jq .visibility` dice `PUBLIC`.
3. CI y CD en verde sobre el último commit de `main`:
   ```bash
   git switch main && git pull
   gh run list --commit "$(git rev-parse HEAD)"
   ```
4. La URL y la base están vivas: `curl -fsS https://pet-finder-akac.onrender.com/actuator/health/readiness` (o la URL nueva si M6 la cambió) y `sh scripts/humo-flujo.sh <url>`, que crea y cierra su propio caso. La fecha de vencimiento de la base gratis es posterior a la calificación.
5. Cada quien entregó sus coevaluaciones.
6. Turno del sábado 14: revisa la corrida diaria (6:00 en Bogotá). Si sale roja, abre un PR con el arreglo, con revisión y pipeline completo, y avísales a los dos.

**Cómo comprobarlo.** El comando del paso 1 lista todos los checks, y CI y CD están en verde sobre el último commit de `main`.

**Pedido para tu agente:** "Corre los comandos de la sección A14 y dame una lista de lo que está en verde y lo que falta. No cambies nada."

---

## Qué entregas, a quién y cuándo

| Cuándo | Qué | A quién |
|---|---|---|
| Sáb 7 | PR de A1, A3, A7 y A5 | Mateo los revisa |
| Primera corrida de `ci.yml` (A2) | Los nombres exactos de los checks | Santi, para S2 |
| Al entrar A2 | Aviso de que CI ya corre | Mateo (M5, M7, M13) y Santi (S4, S15) |
| Al entrar A7 | La imagen ya no lleva la clave; crear `PETFINDER_CLAVE_CARTO` en Render | Mateo (M5, M6) |
| Al entrar A4 | Cómo se usa `humo-flujo.sh` contra una URL | Mateo (M7) |
| Al entrar A5 y A6 | Enlaces de las corridas en rojo (commons-text y `config-local.js`) y los reportes | Santi (S11, S12, S13) |
| Dom 8 | A6 en `main`; marcar `imagen-y-escaneo` y `despliegue-y-verificacion` como obligatorios | Santi |
| Al entrar A9 | Nombre del campo `codigoGestion` y el 403 | Mateo (M10) |
| Tras 3 corridas en verde de `pruebas-ui` | Pedir que sea obligatorio | Santi |
| Al terminar A8 | Enlace de la corrida de menos de 10 minutos | `devops.md` y Santi (S15) |
| Vie 13 | `docs/devops.md` | Mateo lo revisa |
| Sáb 14 | `docs/implementacion.md` probada por alguien de afuera, con su registro de dudas | Mateo lo revisa; Santi lo cita en `evolucion.md` |
| Sáb 14 | Turno: corrida diaria en verde | El equipo |
| Dom 15 | Resultado de A14 | Los tres |
| Siempre | Revisar los PR de Santi en menos de 24 h | Santi |
