# Corte 3 · Guía de tareas de Santi (S1 a S16)

**Para:** Santi (`DrearSanti`), dueño y administrador de `github.com/DrearSanti/pet-finder-dyas`, y su agente de Claude Code.
**Tu foco:** seguridad, núcleo y lo que solo puede hacer el dueño del repositorio.
**Página del reparto (marca ahí cada tarea cuando se integre):** https://claude.ai/artifact/VsxyLQaD2CfTw7srJPbFwn

| Tramo | Fechas | Hito |
|---|---|---|
| 1 · Base | hasta el dom 8 nov | Un merge a main se despliega solo, y los cuatro controles de seguridad pueden bloquear. |
| 2 · Endurecer | lun 9 a mié 11 nov | Funcionalidad congelada, reversión ejecutada y PR bloqueado cerrado. |
| 3 · Documentar y ensayar | jue 12 a dom 15 nov (entrega) | v3.0.0 publicada y pipeline en verde en el último commit de main. Desde el vie 13 solo entran documentos. |

**Revisiones (D6):** Antonio (`antobeni07`) revisa tus PR; tú revisas los de Mateo (`technologybyteo`). Respuesta en menos de 24 horas. Mateo ya tiene listas M1, M2, M3 y M4 sin integrar: revisarlas pronto te desbloquea a ti (S6 espera a M1; S7 y S10 tocan lo mismo que M2).

## Reglas

1. Una rama por tarea, creada desde `main` actualizado: `git switch main && git pull && git switch -c corte3/S4`.
2. Un PR por tarea, con Antonio como revisor. Se integra con **merge commit**, nunca squash ni rebase.
3. Antes de abrir el PR: `./mvnw -q clean verify` en verde, con JDK 17 (`./mvnw -v` debe decir `Java version: 17`).
4. Al integrar, marca la tarea en la página del reparto.
5. El agente nunca hace `git commit`, `git tag`, `git push` ni `git reset --hard`. Los commits son tuyos.
6. No toques `pom.xml` salvo lo que pida la tarea (S7 agrega Flyway; S12 agrega commons-text 1.9 solo en su rama de evidencia).
7. Nadie lee ni imprime `.env`. Revisa `git diff --cached` antes de cada commit: un secreto real en el historial pone DevSecOps en Insuficiente.
8. Si un check falla, se arregla la causa. Bajar un umbral o apagar una regla sin justificarlo cuenta como control no implementado.

**Windows:** todos los comandos son para Git Bash. En PowerShell, `./mvnw` es `.\mvnw.cmd`. Con `gh api` en Git Bash, escribe las rutas sin `/` inicial (`repos/DrearSanti/...`): Git Bash convierte `/repos/...` en una ruta de Windows y la llamada falla.

**Dos trampas de Maven en este repo:**
- `-Dtest` con solo un patrón negativo (por ejemplo `-Dtest='!ReglasArquitecturaTest'`) corre también las IT y las UIT, y Selenium descarga unos 384 MB. Usa siempre un patrón positivo: `-Dtest='*Test,!ReglasArquitecturaTest'`.
- Cuando entre A1 (umbral de JaCoCo), un comando que corre pocas IT falla por cobertura. Agrega `-Djacoco.skip=true` a esos comandos.

Una sola IT:

```bash
./mvnw -q verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=ReinicioPersistenciaIT -Djacoco.skip=true
```

## Tu semana en orden

| Tarea | Cuándo | ¿Empieza ya? | Desbloquea a |
|---|---|---|---|
| Revisar M1, M2, M3, M4 (Mateo) | sáb 7 | Sí, apenas Mateo abra los PR | S6 (M1), S7 y S10 (M2), A6 (M4) |
| S1 Ajustes del repositorio | sáb 7 | Sí | A2 (Actions habilitado), S2, S3, S6 |
| S2 Proteger main (base) | sáb 7 | Sí, sin checks | Todos: desde aquí nada entra sin PR aprobado |
| S4 Reglas de arquitectura | sáb 7 | Sí, el cambio está listo | S15; el check reglas-arquitectura de A2 |
| S7 PostgreSQL y Flyway | sáb 7 a dom 8 | Sí, el cambio está listo; aplícalo después de que M2 entre a main | S9, M6, A13 |
| S5 Gitleaks | sáb 7 a dom 8 | Sí; coordina `seguridad.yml` con A5 (Antonio) | S11, S12, S13, A8 |
| S6 CodeQL y umbral | dom 8 | Espera a M1 en main | S11, S12, S13, A8 |
| S2 Checks obligatorios | dom 8 | Espera a que corran A2, A5, S5, S6 (y luego A6) | S12, A14 |
| S3 Secretos de despliegue | dom 8 | Espera a M5 (paquete) y M6 (hook de Render) | M7 |
| S8 N+1 y tope de la lista | lun 9 a mar 10 | Sí, desde el lun 9 | Ayuda al SLO de M7 |
| S9 Código de gestión: núcleo | lun 9 a mar 10 | Espera a S7 en main; se hace con A9 y M10 | A9, M10, S16, A13 |
| S10 Límite de peticiones | mar 10 a mié 11 | Espera a M2 en main | A13 |
| S12 PR bloqueado de evidencia | mié 11 | Espera a S2, S5, S6, A5, A6 | S11 |
| S11 docs/seguridad.md | jue 12 a vie 13 | Espera a S5, S6, A5, A6, S12 | A13 (riesgos aceptados, parte 9) |
| S13 ADR-003 | jue 12 | Espera a S5, S6, A5, A6 | — |
| S16 Auditoría (opcional) | jue 12, solo si sobra tiempo | Espera a S9 | — |
| S15 Trazabilidad en el README | vie 13 | Espera a A2, A6, M7 | — |
| S14 docs/evolucion.md | vie 13 a sáb 14 | Espera a casi todo y a la respuesta del profesor (M0) | — |

S1, S2 y S3 solo las puedes hacer tú: el repositorio es de cuenta personal. Agenda 30 minutos con Antonio y Mateo justo después de la primera corrida de `ci.yml`.

---

# Tramo 1 · Base (hasta el dom 8 nov)

## S1 · Ajustes del repositorio

**Para qué:** Integración continua (15 puntos) y apoyo a DevSecOps (20). Hoy `allow_squash_merge` y `allow_rebase_merge` están en `true`, `vulnerability-alerts` responde 404 y no hay ningún environment.
**Depende de:** nada. **Desbloquea:** A2 (necesita Actions habilitado), S2, S3 y S6.
**Hecho cuando (del plan):** gh api repos/DrearSanti/pet-finder-dyas muestra allow_squash_merge y allow_rebase_merge en false, y /vulnerability-alerts responde 204.
**Archivos:** ninguno; todo es configuración de GitHub.

**Cómo hacerlo** (en la web o con `gh`; los comandos de `gh` están **sin probar todavía**):

1. Settings > General > Pull Requests: deja marcado solo **Allow merge commits**; desmarca squash y rebase.
   ```bash
   gh api -X PATCH repos/DrearSanti/pet-finder-dyas -F allow_merge_commit=true -F allow_squash_merge=false -F allow_rebase_merge=false
   ```
2. Settings > Actions > General: Actions habilitado; en **Workflow permissions**, "Read repository contents and packages permissions" (solo lectura), y sin permitir que Actions apruebe PR. Cada job pide lo suyo con `permissions:`.
   ```bash
   gh api -X PUT repos/DrearSanti/pet-finder-dyas/actions/permissions -F enabled=true -f allowed_actions=all
   gh api -X PUT repos/DrearSanti/pet-finder-dyas/actions/permissions/workflow -f default_workflow_permissions=read -F can_approve_pull_request_reviews=false
   ```
3. Settings > Code security (en algunas cuentas se llama Advanced Security): activa **Dependabot alerts** y **Secret Protection** (secret scanning y push protection). En **Code scanning no elijas "Default setup"**: el workflow de S6 es la configuración avanzada, y GitHub rechaza los SARIF de un workflow mientras el default setup esté activo.
   ```bash
   gh api -X PUT repos/DrearSanti/pet-finder-dyas/vulnerability-alerts
   gh api -X PATCH repos/DrearSanti/pet-finder-dyas -f 'security_and_analysis[secret_scanning][status]=enabled' -f 'security_and_analysis[secret_scanning_push_protection][status]=enabled'
   ```
4. Crea el environment `produccion` (Settings > Environments > New environment):
   ```bash
   gh api -X PUT repos/DrearSanti/pet-finder-dyas/environments/produccion
   ```

**Cómo comprobarlo:**

```bash
gh api repos/DrearSanti/pet-finder-dyas --jq '{merge: .allow_merge_commit, squash: .allow_squash_merge, rebase: .allow_rebase_merge}'
# esperado: {"merge":true,"rebase":false,"squash":false}
gh api -i repos/DrearSanti/pet-finder-dyas/vulnerability-alerts | head -n 1
# esperado: HTTP/2.0 204 No Content
gh api repos/DrearSanti/pet-finder-dyas/actions/permissions --jq .enabled
# esperado: true
gh api repos/DrearSanti/pet-finder-dyas/actions/permissions/workflow --jq .default_workflow_permissions
# esperado: read
gh api repos/DrearSanti/pet-finder-dyas/environments --jq '.environments[].name'
# esperado: produccion
```

**Trampas:**
- Con la cuenta de Mateo o de Antonio `vulnerability-alerts` responde 403: compruébalo tú.
- "Default setup" de CodeQL choca con S6 (ver paso 3).

**Pedido para tu agente:** "Lee la sección S1 de esta guía y dame los comandos de comprobación; los ajustes los hago yo en GitHub."

## S2 · Proteger main

**Para qué:** Integración continua (15): main protegida de verdad y PR aprobados por otro integrante. Hoy hay 0 de 32 PR con revisión, `branches/main/protection` da 404 y `rulesets` da `[]`.
**Depende de:** A2 para los checks (GitHub solo ofrece en la lista los nombres de checks que ya corrieron). **Desbloquea:** S12 y A14; desde aquí todo entra por PR aprobado.
**Hecho cuando (del plan):** Un git push directo a main se rechaza y un PR sin aprobación o con un check en rojo no se puede fusionar.
**Archivos:** ninguno.

**Cómo hacerlo, en dos pasos:**

1. **Hoy, sin checks.** Settings > Rules > Rulesets > New branch ruleset:
   - Nombre `proteger-main`, Enforcement **Active**, **Bypass list vacía** (ni el dueño la salta).
   - Target: Include default branch.
   - Restrict deletions; Block force pushes.
   - Require a pull request before merging: 1 aprobación; **Dismiss stale pull request approvals when new commits are pushed**; métodos permitidos: solo Merge.
   - **No** actives "Require linear history": prohíbe los merge commits que usa el equipo.

   Equivalente con `gh` (**sin probar todavía**). Guarda esto como `ruleset-main.json` fuera del repo:
   ```json
   {
     "name": "proteger-main",
     "target": "branch",
     "enforcement": "active",
     "bypass_actors": [],
     "conditions": { "ref_name": { "include": ["~DEFAULT_BRANCH"], "exclude": [] } },
     "rules": [
       { "type": "deletion" },
       { "type": "non_fast_forward" },
       { "type": "pull_request", "parameters": {
           "required_approving_review_count": 1,
           "dismiss_stale_reviews_on_push": true,
           "require_code_owner_review": false,
           "require_last_push_approval": false,
           "required_review_thread_resolution": false,
           "allowed_merge_methods": ["merge"] } }
     ]
   }
   ```
   ```bash
   gh api -X POST repos/DrearSanti/pet-finder-dyas/rulesets --input ruleset-main.json
   ```
2. **A medida que cada check corra una vez**, edita el ruleset: Require status checks to pass, con **Require branches to be up to date before merging**, y agrega los checks con estos nombres exactos:

   | Workflow | Checks | Aparecen con |
   |---|---|---|
   | CI (`ci.yml`) | `construccion`, `pruebas-unitarias`, `reglas-arquitectura`, `pruebas-integracion` | A2 |
   | CI (`ci.yml`) | `imagen-y-escaneo`, `despliegue-y-verificacion` | A6 |
   | Seguridad (`seguridad.yml`) | `SCA` | A5 |
   | Seguridad (`seguridad.yml`) | `Secretos` | S5 |
   | Seguridad (`seguridad.yml`) | `SAST (java-kotlin)`, `SAST (javascript-typescript)` | S6 |
   | CI (`ci.yml`) | `pruebas-ui` | A11, después de 3 corridas seguidas en verde |

   Los nombres de `ci.yml` salen del cambio de A2 que ya pasó el gate en local; ese workflow **todavía no ha corrido en GitHub Actions**, así que confirma cada nombre en la pestaña Checks del primer PR antes de agregarlo.

**Cómo comprobarlo:**

```bash
gh api repos/DrearSanti/pet-finder-dyas/rules/branches/main --jq '.[].type'
# esperado (en cualquier orden): deletion, non_fast_forward, pull_request y, tras el paso 2, required_status_checks

# Un push directo a main se rechaza (lo haces tú, no el agente):
git switch main && git pull
git switch -c prueba/push-directo
git commit --allow-empty -m "Prueba: push directo a main (debe rechazarse)"
git push origin HEAD:main        # esperado: rechazo con "Repository rule violations"
git switch main && git branch -D prueba/push-directo
```

Y en el primer PR de Antonio: sin aprobación, el botón de merge queda bloqueado; con un check en rojo, también.

**Trampas:**
- Un check obligatorio que nunca corre deja todos los PR esperando para siempre. Agrega solo nombres que ya viste correr; por eso los workflows no usan `paths-ignore`.
- Con "up to date" activo, cada merge obliga a los demás PR a actualizarse con "Update branch" y a correr CI otra vez. Es lo esperado.
- Si alguien renombra un job, el check viejo queda exigido y nadie puede fusionar: corrige el ruleset en el mismo momento.

**Pedido para tu agente:** "Con la sección S2 de esta guía, prepárame `ruleset-main.json` fuera del repo y dime qué checks ya aparecen en el último PR (`gh pr checks`)."

## S3 · Secretos de despliegue

**Para qué:** Despliegue continuo (15) y amenaza T11: los secretos viven solo en el environment `produccion` y solo los usa el job de despliegue. Hoy hay 0 secretos y 0 environments.
**Depende de:** M5 (primera imagen publicada en GHCR) y M6 (deploy hook de Render). **Desbloquea:** M7.
**Hecho cuando (del plan):** gh api .../environments/produccion/secrets lista los nombres y cd.yml los usa sin que salgan en los logs.
**Archivos:** ninguno.

**Cómo hacerlo** (todo **sin probar todavía**):

1. Pídele a Mateo los nombres que usa `cd.yml` (al menos `RENDER_DEPLOY_HOOK_URL`) y los valores **por privado**, nunca en el repo, en un PR ni en el chat del grupo.
2. Crea cada secreto en el environment:
   ```bash
   gh secret set RENDER_DEPLOY_HOOK_URL --env produccion --repo DrearSanti/pet-finder-dyas
   ```
   `gh` pide el valor sin mostrarlo. En Git Bash (mintty) ese aviso a veces no funciona: corre el comando en PowerShell o Windows Terminal, o crea el secreto en Settings > Environments > produccion > Add environment secret.
3. En Settings > Environments > produccion > Deployment branches and tags: **Selected branches and tags**, con la rama `main` y el patrón de tag `v*`. Confírmalo con Mateo según cómo dispare `cd.yml`.
4. Después de la primera publicación de M5: github.com/DrearSanti?tab=packages > `pet-finder` > Package settings > Change visibility > **Public**, para que Render baje la imagen sin credenciales. Solo tú puedes hacerlo.

**Cómo comprobarlo:**

```bash
gh api repos/DrearSanti/pet-finder-dyas/environments/produccion/secrets --jq '.secrets[].name'
# esperado: RENDER_DEPLOY_HOOK_URL y lo demás que pida cd.yml (solo nombres; los valores nunca salen)
gh auth refresh -s read:packages
gh api users/DrearSanti/packages/container/pet-finder --jq .visibility
# esperado: public
```

En la primera corrida de `cd.yml`, el log del paso del hook muestra `***` en lugar de la URL.

**Trampas:**
- La URL del deploy hook es un secreto: quien la tenga despliega lo que quiera. Si se filtra, Mateo la regenera en Render y tú la actualizas.
- Una vez público el paquete, cualquiera puede bajar la imagen. Por eso A7 saca la clave de CARTO de la imagen antes de la primera publicación.

**Pedido para tu agente:** "Lee S3 y comprueba con `gh` que el environment `produccion` tiene los secretos que usa `.github/workflows/cd.yml`, sin imprimir valores."

## S4 · Endurecer las reglas de arquitectura

**Para qué:** Arquitectura verificada (15). La regla actual solo vigila la web; la consola usaba `ServicioReportes` y `ServicioAvistamientos` directamente (24 violaciones medidas en `MenuConsola` y `EscenarioDemostracion`). La rúbrica pide reglas que salgan del ADR del Corte 2 y protejan una frontera real.
**Depende de:** A2, solo para ver el check `reglas-arquitectura` en rojo en GitHub. **Desbloquea:** ninguna tarea del plan; da sentido al check de A2 y S15 cita sus reglas.
**Hecho cuando (del plan):** ReglasArquitecturaTest pasa con las reglas nuevas, y una violación a propósito pone en rojo el check reglas-arquitectura.
**Archivos:** `src/test/java/petfinder/arquitectura/ReglasArquitecturaTest.java`, `src/main/java/petfinder/Main.java`, `src/main/java/petfinder/adaptadores/entrada/consola/MenuConsola.java`, `src/main/java/petfinder/adaptadores/entrada/consola/EscenarioDemostracion.java`, `docs/adr/ADR-001-estilo-arquitectonico.md`.

**Estado:** el cambio de código ya pasó `./mvnw -q clean verify` en local (235 unitarias y 26 IT en verde, corrido junto con S7). Falta la cita en ADR-001.

**Qué cambia:**
- La consola recibe los puertos `GestionReportes` y `RegistroAvistamientos` en lugar de los servicios. Los nombres de las variables no cambian.
- `laWebSoloHablaConPuertos` pasa a `lasEntradasSoloHablanConPuertos` y cubre todo `adaptadores.entrada`.
- Cuatro reglas nuevas: `laPersistenciaNoConoceOtrosAdaptadores`, `laIaNoConoceOtrosAdaptadores`, `soloConfigConoceLosAdaptadoresDeSalida` y `elAsistenteNuncaPublica` (amenaza T8). Quedan 9 reglas.
- Se quita `allowEmptyShould(true)`: si un paquete cambia de nombre, la regla falla en vez de pasar sin revisar nada.

**Cómo hacerlo:**

1. `git switch main && git pull && git switch -c corte3/S4`
2. Aplica el cambio. El bloque de abajo también está como archivo en `docs/tareas-corte-3/parches/S4.patch`: usa el archivo, porque copiar desde el Markdown en Windows puede dañar los finales de línea.
   ```bash
   git apply --check docs/tareas-corte-3/parches/S4.patch && git apply docs/tareas-corte-3/parches/S4.patch
   ```
   Si `--check` falla porque alguien ya tocó esos archivos, **no uses `--3way`**: el parche no trae las líneas `index` con los hashes de los archivos y Git responde "repository lacks the necessary blob". Usa `git apply --reject docs/tareas-corte-3/parches/S4.patch`: aplica lo que encaja y deja cada parte que no encajó en un archivo `.rej` junto al original. Pasa esas líneas a mano, borra los `.rej` (`find . -name '*.rej' -delete`) y revisa `git status` antes del commit.

```diff
diff --git a/src/main/java/petfinder/Main.java b/src/main/java/petfinder/Main.java
--- a/src/main/java/petfinder/Main.java
+++ b/src/main/java/petfinder/Main.java
@@ -4,8 +4,8 @@
 import org.springframework.boot.builder.SpringApplicationBuilder;
 import org.springframework.context.ConfigurableApplicationContext;
 
-import petfinder.application.service.ServicioAvistamientos;
-import petfinder.application.service.ServicioReportes;
+import petfinder.application.port.entrada.RegistroAvistamientos;
+import petfinder.application.port.entrada.GestionReportes;
 import petfinder.domain.observer.AlertaPropietarioObserver;
 import petfinder.domain.observer.AuditoriaObserver;
 import petfinder.domain.observer.PublicadorAvistamientos;
@@ -30,8 +30,8 @@
                 .properties("spring.main.banner-mode=off", "logging.level.root=WARN")
                 .run(args)) {
 
-            ServicioReportes servicioReportes = contexto.getBean(ServicioReportes.class);
-            ServicioAvistamientos servicioAvistamientos = contexto.getBean(ServicioAvistamientos.class);
+            GestionReportes servicioReportes = contexto.getBean(GestionReportes.class);
+            RegistroAvistamientos servicioAvistamientos = contexto.getBean(RegistroAvistamientos.class);
 
             if (tieneBandera(args, "--menu")) {
                 new MenuConsola(servicioReportes, servicioAvistamientos).ejecutar();
diff --git a/src/main/java/petfinder/adaptadores/entrada/consola/EscenarioDemostracion.java b/src/main/java/petfinder/adaptadores/entrada/consola/EscenarioDemostracion.java
--- a/src/main/java/petfinder/adaptadores/entrada/consola/EscenarioDemostracion.java
+++ b/src/main/java/petfinder/adaptadores/entrada/consola/EscenarioDemostracion.java
@@ -2,8 +2,8 @@
 
 import java.time.LocalDateTime;
 
-import petfinder.application.service.ServicioAvistamientos;
-import petfinder.application.service.ServicioReportes;
+import petfinder.application.port.entrada.RegistroAvistamientos;
+import petfinder.application.port.entrada.GestionReportes;
 import petfinder.domain.exception.DominioException;
 import petfinder.domain.model.Avistamiento;
 import petfinder.domain.model.Contacto;
@@ -26,14 +26,14 @@
  */
 public class EscenarioDemostracion {
 
-    private final ServicioReportes servicioReportes;
-    private final ServicioAvistamientos servicioAvistamientos;
+    private final GestionReportes servicioReportes;
+    private final RegistroAvistamientos servicioAvistamientos;
     private final PublicadorAvistamientos publicador;
     private final AlertaPropietarioObserver alerta;
     private final AuditoriaObserver auditoria;
 
-    public EscenarioDemostracion(ServicioReportes servicioReportes,
-                                 ServicioAvistamientos servicioAvistamientos,
+    public EscenarioDemostracion(GestionReportes servicioReportes,
+                                 RegistroAvistamientos servicioAvistamientos,
                                  PublicadorAvistamientos publicador,
                                  AlertaPropietarioObserver alerta,
                                  AuditoriaObserver auditoria) {
diff --git a/src/main/java/petfinder/adaptadores/entrada/consola/MenuConsola.java b/src/main/java/petfinder/adaptadores/entrada/consola/MenuConsola.java
--- a/src/main/java/petfinder/adaptadores/entrada/consola/MenuConsola.java
+++ b/src/main/java/petfinder/adaptadores/entrada/consola/MenuConsola.java
@@ -4,8 +4,8 @@
 import java.util.List;
 import java.util.Scanner;
 
-import petfinder.application.service.ServicioAvistamientos;
-import petfinder.application.service.ServicioReportes;
+import petfinder.application.port.entrada.RegistroAvistamientos;
+import petfinder.application.port.entrada.GestionReportes;
 import petfinder.domain.exception.DominioException;
 import petfinder.domain.model.Avistamiento;
 import petfinder.domain.model.Contacto;
@@ -26,15 +26,15 @@
  */
 public class MenuConsola {
 
-    private final ServicioReportes servicioReportes;
-    private final ServicioAvistamientos servicioAvistamientos;
+    private final GestionReportes servicioReportes;
+    private final RegistroAvistamientos servicioAvistamientos;
     private final Scanner entrada = new Scanner(System.in);
 
     /** Consecutivo local de avistamientos; los reportes los numera el dominio. */
     private int consecutivoAvistamiento = 0;
 
-    public MenuConsola(ServicioReportes servicioReportes,
-                       ServicioAvistamientos servicioAvistamientos) {
+    public MenuConsola(GestionReportes servicioReportes,
+                       RegistroAvistamientos servicioAvistamientos) {
         this.servicioReportes = servicioReportes;
         this.servicioAvistamientos = servicioAvistamientos;
     }
diff --git a/src/test/java/petfinder/arquitectura/ReglasArquitecturaTest.java b/src/test/java/petfinder/arquitectura/ReglasArquitecturaTest.java
--- a/src/test/java/petfinder/arquitectura/ReglasArquitecturaTest.java
+++ b/src/test/java/petfinder/arquitectura/ReglasArquitecturaTest.java
@@ -7,12 +7,18 @@
 import com.tngtech.archunit.junit.ArchTest;
 import com.tngtech.archunit.lang.ArchRule;
 
+import petfinder.application.port.entrada.AsistenteReportes;
+import petfinder.application.port.entrada.GestionReportes;
+import petfinder.application.port.salida.RepositorioReportes;
+
 /**
  * Reglas de la arquitectura hexagonal convertidas en pruebas: si alguien
- * rompe la dirección de las dependencias, mvnw test se pone en rojo.
+ * rompe la dirección de las dependencias, mvnw test se pone en rojo, y en el
+ * pipeline se pone en rojo el check reglas-arquitectura.
  *
- * Las reglas sobre paquetes que todavía no existen usan allowEmptyShould,
- * porque ArchUnit falla por defecto cuando una regla no encuentra clases.
+ * Cada regla protege una frontera que nombra ADR-001. Ninguna usa
+ * allowEmptyShould: si un paquete cambia de nombre y la regla deja de
+ * encontrar clases, ArchUnit falla en vez de pasar sin revisar nada.
  */
 @AnalyzeClasses(packages = "petfinder", importOptions = ImportOption.DoNotIncludeTests.class)
 class ReglasArquitecturaTest {
@@ -31,11 +37,51 @@
                     "org.springframework..", "jakarta.persistence..", "com.anthropic..",
                     "petfinder.adaptadores..", "petfinder.config..");
 
+    /**
+     * Antes solo miraba la web, y la consola usaba ServicioReportes y
+     * ServicioAvistamientos directamente. Ahora cubre todo adaptadores.entrada:
+     * una forma nueva de entrar al sistema (consola, web, voz, un chat) solo
+     * puede hablar con los puertos de entrada.
+     */
     @ArchTest
-    static final ArchRule laWebSoloHablaConPuertos = noClasses()
-            .that().resideInAPackage("petfinder.adaptadores.entrada.web..")
-            .should().dependOnClassesThat().resideInAPackage("petfinder.application.service..")
-            .allowEmptyShould(true);
+    static final ArchRule lasEntradasSoloHablanConPuertos = noClasses()
+            .that().resideInAPackage("petfinder.adaptadores.entrada..")
+            .should().dependOnClassesThat().resideInAPackage("petfinder.application.service..");
+
+    /** Cambiar la base de datos no puede obligar a tocar el extractor con Claude, ni al revés. */
+    @ArchTest
+    static final ArchRule laPersistenciaNoConoceOtrosAdaptadores = noClasses()
+            .that().resideInAPackage("petfinder.adaptadores.salida.persistencia..")
+            .should().dependOnClassesThat().resideInAnyPackage(
+                    "petfinder.adaptadores.entrada..", "petfinder.adaptadores.salida.ia..");
+
+    @ArchTest
+    static final ArchRule laIaNoConoceOtrosAdaptadores = noClasses()
+            .that().resideInAPackage("petfinder.adaptadores.salida.ia..")
+            .should().dependOnClassesThat().resideInAnyPackage(
+                    "petfinder.adaptadores.entrada..", "petfinder.adaptadores.salida.persistencia..");
+
+    /**
+     * config es el composition root: el único lugar que nombra RepositorioReportesH2,
+     * ExtractorClaude y compañía. Si otra clase los usara, cambiar de adaptador
+     * dejaría de ser "cambiar una línea de config".
+     */
+    @ArchTest
+    static final ArchRule soloConfigConoceLosAdaptadoresDeSalida = noClasses()
+            .that().resideOutsideOfPackages("petfinder.config..", "petfinder.adaptadores.salida..")
+            .should().dependOnClassesThat().resideInAPackage("petfinder.adaptadores.salida..");
+
+    /**
+     * El asistente nunca publica (amenaza T8 de docs/seguridad.md): aunque una
+     * frase manipule al modelo, quien implementa AsistenteReportes no tiene cómo
+     * llegar a GestionReportes ni al repositorio. Publicar siempre es un
+     * POST /api/reportes que dispara la persona.
+     */
+    @ArchTest
+    static final ArchRule elAsistenteNuncaPublica = noClasses()
+            .that().implement(AsistenteReportes.class)
+            .should().dependOnClassesThat().areAssignableTo(GestionReportes.class)
+            .orShould().dependOnClassesThat().areAssignableTo(RepositorioReportes.class);
 
     @ArchTest
     static final ArchRule soloElAdaptadorDeIaUsaElSdkDeAnthropic = noClasses()
```

3. En `docs/adr/ADR-001-estilo-arquitectonico.md`, agrega al final de la sección "Decisión" una lista de las reglas que la hacen cumplir, una línea por regla con la frontera que protege. Por ejemplo:
   ```markdown
   ### Reglas que lo verifican (Corte 3)

   `ReglasArquitecturaTest` corre en el check `reglas-arquitectura` de cada PR. Cada regla protege una frontera de esta decisión:

   | Regla | Frontera |
   |---|---|
   | `elDominioEsJavaPuro` | El dominio no conoce Spring, JPA, Anthropic ni otros paquetes del proyecto |
   | `laAplicacionNoConoceAdaptadoresNiFrameworks` | Los servicios dependen solo del dominio y de sus puertos |
   | `lasEntradasSoloHablanConPuertos` | Web, voz y consola entran por los puertos de entrada, nunca por los servicios |
   | `laPersistenciaNoConoceOtrosAdaptadores` | Cambiar la base no obliga a tocar la web ni el extractor con Claude |
   | `laIaNoConoceOtrosAdaptadores` | Cambiar el modelo no obliga a tocar la web ni la persistencia |
   | `soloConfigConoceLosAdaptadoresDeSalida` | config es el único lugar que nombra los adaptadores de salida concretos |
   | `elAsistenteNuncaPublica` | Quien implementa `AsistenteReportes` no llega a `GestionReportes` ni al repositorio (T8) |
   | `soloElAdaptadorDeIaUsaElSdkDeAnthropic` | El SDK de Anthropic queda dentro de `adaptadores.salida.ia` |
   | `soloLaPersistenciaUsaJpa` | JPA queda dentro de `adaptadores.salida.persistencia` |
   ```
   Y en "Consecuencias positivas", cambia "Las 5 reglas de ArchUnit" por "Las 9 reglas de ArchUnit (5 del Corte 2 y 4 del Corte 3)". Confirma los nombres contra el archivo de pruebas.
4. `./mvnw -q clean verify`, commit, push y PR con Antonio de revisor.

**Cómo comprobarlo:**

```bash
./mvnw -q test -Dtest=ReglasArquitecturaTest     # en verde
```

Violación a propósito, en una rama desechable y después de que A2 esté en main:

1. `git switch -c prueba/violacion-arquitectura`
2. En `MenuConsola.java` agrega un **campo** que use el servicio, por ejemplo `private final petfinder.application.service.ServicioReportes violacion = null;`
3. `./mvnw -q test -Dtest=ReglasArquitecturaTest` sale en rojo nombrando `lasEntradasSoloHablanConPuertos`.
4. Commit y push de esa rama: el check `reglas-arquitectura` queda en rojo. Guarda el enlace de la corrida para S15 y docs/devops.md.
5. Borra la rama: `git push origin --delete prueba/violacion-arquitectura` y `git branch -D prueba/violacion-arquitectura`.

**Trampas:**
- Un `import` sin usar no sirve para la prueba: el compilador lo borra del bytecode y ArchUnit no lo ve. Usa un campo o una llamada.
- El gate se corrió con S4 y S7 juntos; por separado no se ha corrido. Por eso el `clean verify` del paso 4 es obligatorio.

**Pedido para tu agente:** "Aplica el parche de la sección S4 de esta guía en la rama `corte3/S4`, agrega la tabla de reglas a ADR-001 como dice el paso 3 y corre `./mvnw -q clean verify`. No hagas commit."

## S5 · Control de secretos con Gitleaks

**Para qué:** DevSecOps (20): control de secretos con umbral **cero secretos, incluido el historial**. La auditoría no encontró secretos con las reglas de Gitleaks replicadas; la primera corrida real es la confirmación.
**Depende de:** nada, pero `seguridad.yml` lo crea A5 (Antonio). **Desbloquea:** S11, S12, S13 y A8.
**Hecho cuando (del plan):** El check Secretos corre en verde sobre todo el historial, publica su reporte como artefacto, y se pone rojo en una rama desechable con una llave falsa.
**Archivos:** `.github/workflows/seguridad.yml`, `.gitleaks.toml` (solo si hace falta).

**Cómo hacerlo** (el job está **sin probar todavía** en GitHub Actions):

1. Coordina con Antonio:
   - Si A5 ya está en main, `seguridad.yml` existe con el job `sca`. Agrega el tuyo debajo, dentro de `jobs:`.
   - Si tú vas primero, crea el archivo con la misma cabecera que trae A5 y solo tu job; Antonio agrega `sca` después:
     ```yaml
     # Controles de seguridad de Pet Finder: Secretos (S5), SAST (S6) y SCA (A5).
     name: Seguridad

     on:
       push:
         branches: ['**']
         tags: ['v*']
       pull_request:
         branches: [main]
       # Corrida diaria: la base de vulnerabilidades cambia sin que nadie toque el código.
       schedule:
         - cron: '0 11 * * *'
       workflow_dispatch:

     permissions:
       contents: read

     defaults:
       run:
         shell: bash

     jobs:
     ```
   La corrida diaria que pide la tarea ya viene en esa cabecera (`schedule`, 11:00 UTC = 6:00 en Bogotá).
2. Saca el SHA-256 del binario de una vez y fíjalo en el workflow:
   ```bash
   curl -sSfL https://github.com/gitleaks/gitleaks/releases/download/v8.30.1/gitleaks_8.30.1_checksums.txt | grep linux_x64.tar.gz
   ```
   Copia los 64 caracteres hexadecimales en `GITLEAKS_SHA256`. Fijarlo en el repo protege más que bajar `checksums.txt` en cada corrida: si alguien cambia la release, el hash ya no coincide.
3. Agrega el job:
   ```yaml
     secretos:
       name: Secretos
       runs-on: ubuntu-24.04
       timeout-minutes: 10
       env:
         GITLEAKS_VERSION: 8.30.1
         # SHA-256 de gitleaks_8.30.1_linux_x64.tar.gz, copiado de gitleaks_8.30.1_checksums.txt de la release oficial.
         GITLEAKS_SHA256: PEGA_AQUI_LOS_64_CARACTERES
       steps:
         - uses: actions/checkout@fbc6f3992d24b796d5a048ff273f7fcc4a7b6c09 # v5.1.0
           with:
             # Todo el historial: el umbral es cero secretos, incluido lo que ya se borró en commits viejos.
             fetch-depth: 0
         - name: Instalar Gitleaks con checksum validado
           run: |
             archivo="gitleaks_${GITLEAKS_VERSION}_linux_x64.tar.gz"
             curl -sSfL -o "$archivo" "https://github.com/gitleaks/gitleaks/releases/download/v${GITLEAKS_VERSION}/${archivo}"
             echo "${GITLEAKS_SHA256}  ${archivo}" | sha256sum -c -
             tar -xzf "$archivo" gitleaks
             ./gitleaks version
         # --redact porque los logs de Actions son públicos. --log-opts=HEAD recorre todo el historial que llega
         # a este commit (en un PR, el de main más el de la rama); sin él, Gitleaks usa --all y una rama de
         # prueba con una llave falsa pondría en rojo el check de todos los demás.
         - name: Buscar secretos en el historial (cero secretos)
           run: ./gitleaks git . --log-opts="HEAD" --redact --verbose --exit-code 1 --report-format sarif --report-path gitleaks.sarif
         - name: Publicar reporte de secretos
           if: always()
           uses: actions/upload-artifact@330a01c490aca151604b8cf639adc76d48f6c5d4 # v5.0.0
           with:
             name: reporte-secretos
             path: gitleaks.sarif
             if-no-files-found: warn
   ```
4. Si dispara en un valor de ejemplo (por ejemplo en `.env.example`), cambia el valor por uno que no parezca secreto (`cambia-esto`). Ojo: eso solo arregla los commits nuevos; el escaneo recorre el historial y el commit viejo sigue disparando. Para ese commit viejo, la excepción más acotada es `.gitleaksignore` en la raíz, con una línea por hallazgo: el valor `Fingerprint:` que imprime Gitleaks en el log (`commit:archivo:regla:línea`). Si el mismo valor de ejemplo aparece en muchos commits, usa en su lugar `.gitleaks.toml` con una excepción por ruta. En los dos casos, registra el falso positivo en docs/seguridad.md (S11):
   ```toml
   [extend]
   useDefault = true

   [allowlist]
   description = "Valores de ejemplo de .env.example: falso positivo registrado en docs/seguridad.md"
   paths = ['''^\.env\.example$''']
   ```
   Si tu versión de Gitleaks avisa que `[allowlist]` es el formato viejo, cámbialo por `[[allowlists]]`.
5. `./mvnw -q clean verify` (no cambia, pero es la regla), commit, push y PR.

**Cómo comprobarlo:**
- En el PR aparece el check **Secretos** en verde, y en la corrida hay un artefacto `reporte-secretos`.
- Llave falsa en una rama desechable:
  ```bash
  git switch main && git pull && git switch -c prueba/gitleaks
  echo "petfinder.pago.api-secret=$(LC_ALL=C tr -dc 'A-Za-z0-9' < /dev/urandom | head -c 40)" > src/main/resources/prueba-gitleaks.properties
  git add src/main/resources/prueba-gitleaks.properties
  git commit -m "Prueba: llave falsa para ver Secretos en rojo"
  git push -u origin prueba/gitleaks     # el check Secretos queda en rojo; guarda el enlace de la corrida
  git switch main
  git push origin --delete prueba/gitleaks && git branch -D prueba/gitleaks
  ```

**Trampas:**
- No uses llaves con forma `sk-ant-...`, `ghp_...` ni `AKIA...EXAMPLE`: el push protection de GitHub rechaza las dos primeras antes de que llegue el pipeline, y Gitleaks deja pasar la tercera.
- Sin `fetch-depth: 0` solo se revisa el último commit, y el umbral pide el historial.
- Un secreto **real** en el historial no se arregla borrando el archivo: se rota (se revoca y se crea otro) y se registra en docs/seguridad.md.

**Pedido para tu agente:** "Agrega el job `secretos` de la sección S5 de esta guía a `.github/workflows/seguridad.yml` (o crea el archivo con la cabecera si no existe) y deja `GITLEAKS_SHA256` para que yo pegue el hash."

## S6 · SAST con CodeQL y un paso que aplique el umbral

**Para qué:** DevSecOps (20): SAST con umbral **ningún hallazgo alto o crítico sin tratar**. CodeQL solo reporta; el enunciado pide un paso que lea el SARIF y falle.
**Depende de:** M1 en main. Sin M1, la regex cuadrática de `ExtractorRegex` (100 KB = 17 s) sale como `java/polynomial-redos` alta y bloquea a todos desde el primer día. **Desbloquea:** S11, S12, S13 y A8.
**Hecho cuando (del plan):** El check SAST corre en PR y push, los resultados salen en la pestaña Security, el SARIF queda como artefacto y el paso falla en una rama desechable con un controlador de prueba.
**Archivos:** `.github/workflows/seguridad.yml`, `scripts/umbral-sarif.py`, `.github/codeql/codeql-config.yml`, `.github/codeql/excepciones.txt`, fila nueva en `scripts/README.md` (lo crea M3).

**Estado:** `scripts/umbral-sarif.py` se probó en local con SARIF de ejemplo: sale 1 con un hallazgo de 9,8 sin tratar, 0 cuando el único hallazgo alto está en `excepciones.txt` y 2 cuando no encuentra SARIF. Contra un SARIF real de CodeQL y en GitHub Actions está **sin probar todavía**.

**Cómo hacerlo:**

1. Espera a que M1 entre a main; luego `git switch main && git pull && git switch -c corte3/S6`.
2. Crea `scripts/umbral-sarif.py`:
   ```python
   #!/usr/bin/env python3
   """Umbral de SAST (S6): CodeQL solo reporta, así que este paso lee sus SARIF y falla
   si queda un hallazgo con security-severity igual o mayor al umbral sin tratar.

   Uso: python3 scripts/umbral-sarif.py CARPETA_O_ARCHIVO [UMBRAL] [EXCEPCIONES]
     UMBRAL       7.0 por defecto (7.0 a 8.9 es alto; 9.0 o más, crítico).
     EXCEPCIONES  .github/codeql/excepciones.txt por defecto. Una por línea: "regla ruta",
                  por ejemplo "java/ssrf src/main/java/petfinder/Foo.java". Cada línea
                  tiene su fila en docs/seguridad.md. "#" empieza un comentario.

   Sale 0 si no hay hallazgos por encima del umbral, 1 si los hay y 2 si no encontró SARIF:
   una ruta mal escrita no puede dejar pasar el pipeline en verde.
   """
   import json
   import pathlib
   import sys


   def leer_excepciones(ruta):
       excepciones = set()
       archivo = pathlib.Path(ruta)
       if not archivo.is_file():
           return excepciones
       for linea in archivo.read_text(encoding="utf-8").splitlines():
           linea = linea.split("#", 1)[0].strip()
           if linea:
               partes = linea.split()
               if len(partes) != 2:
                   sys.exit(f"Excepción mal escrita (se espera 'regla ruta'): {linea}")
               excepciones.add((partes[0], partes[1]))
       return excepciones


   def severidades(run):
       # Las reglas de los paquetes de consultas quedan en extensions, no en driver.
       reglas = list(run.get("tool", {}).get("driver", {}).get("rules", []))
       for extension in run.get("tool", {}).get("extensions", []):
           reglas.extend(extension.get("rules", []))
       mapa = {}
       for regla in reglas:
           valor = regla.get("properties", {}).get("security-severity")
           if valor is not None:
               mapa[regla.get("id")] = float(valor)
       return mapa


   def ruta_de(resultado):
       for ubicacion in resultado.get("locations", []):
           uri = ubicacion.get("physicalLocation", {}).get("artifactLocation", {}).get("uri")
           if uri:
               return uri
       return "?"


   def main():
       if len(sys.argv) < 2:
           sys.exit(__doc__)
       origen = pathlib.Path(sys.argv[1])
       umbral = float(sys.argv[2]) if len(sys.argv) > 2 else 7.0
       excepciones = leer_excepciones(sys.argv[3] if len(sys.argv) > 3 else ".github/codeql/excepciones.txt")

       archivos = sorted(origen.rglob("*.sarif")) if origen.is_dir() else [origen]
       archivos = [a for a in archivos if a.is_file()]
       if not archivos:
           print(f"No se encontró ningún SARIF en {origen}")
           return 2

       bloqueantes = 0
       for archivo in archivos:
           sarif = json.loads(archivo.read_text(encoding="utf-8"))
           for run in sarif.get("runs", []):
               mapa = severidades(run)
               for resultado in run.get("results", []):
                   regla = resultado.get("ruleId") or resultado.get("rule", {}).get("id", "?")
                   severidad = mapa.get(regla, 0.0)
                   ruta = ruta_de(resultado)
                   if severidad < umbral:
                       continue
                   if resultado.get("suppressions"):
                       print(f"Suprimido en el código: {regla} {ruta} ({severidad})")
                       continue
                   if (regla, ruta) in excepciones:
                       print(f"Excepción registrada en docs/seguridad.md: {regla} {ruta} ({severidad})")
                       continue
                   bloqueantes += 1
                   linea = (resultado.get("locations") or [{}])[0].get("physicalLocation", {}).get("region", {}).get("startLine", "?")
                   print(f"BLOQUEA: {regla} en {ruta}:{linea}, security-severity {severidad}")

       print(f"{bloqueantes} hallazgo(s) con security-severity >= {umbral} sin tratar en {len(archivos)} SARIF.")
       return 1 if bloqueantes else 0


   if __name__ == "__main__":
       sys.exit(main())
   ```
3. Crea `.github/codeql/codeql-config.yml`:
   ```yaml
   name: Configuración de CodeQL de Pet Finder
   # Lo que no corre en producción no se analiza: el plan del Corte 2 y las pruebas.
   paths-ignore:
     - blueprints
     - src/test
   ```
   Y `.github/codeql/excepciones.txt`:
   ```text
   # Excepciones del umbral de SAST (scripts/umbral-sarif.py). Una por línea: "regla ruta".
   # Cada línea necesita su fila en docs/seguridad.md: falso positivo o riesgo aceptado, con la justificación.
   # Descartar una alerta en la pestaña Security no cambia el SARIF; por eso existe este archivo.
   ```
4. Agrega el job a `seguridad.yml` (**sin probar todavía**):
   ```yaml
     sast:
       name: SAST (${{ matrix.lenguaje }})
       runs-on: ubuntu-24.04
       timeout-minutes: 20
       permissions:
         contents: read
         security-events: write   # subir los resultados a la pestaña Security
         actions: read
       strategy:
         fail-fast: false
         matrix:
           lenguaje: [java-kotlin, javascript-typescript]
       steps:
         - uses: actions/checkout@fbc6f3992d24b796d5a048ff273f7fcc4a7b6c09 # v5.1.0
         - name: Iniciar CodeQL
           uses: github/codeql-action/init@1fc58d04a4d4374f921bdac573c3811c240f9c26 # v4.39.2
           with:
             languages: ${{ matrix.lenguaje }}
             build-mode: none
             queries: security-extended
             config-file: ./.github/codeql/codeql-config.yml
         - name: Analizar
           uses: github/codeql-action/analyze@1fc58d04a4d4374f921bdac573c3811c240f9c26 # v4.39.2
           with:
             category: /language:${{ matrix.lenguaje }}
             output: sarif-results
             upload: always
         # CodeQL solo reporta. Umbral del enunciado: ningún hallazgo alto o crítico sin tratar (7.0 o más).
         - name: Umbral de SAST (security-severity 7.0 o más sin tratar detiene el pipeline)
           run: python3 scripts/umbral-sarif.py sarif-results 7.0 .github/codeql/excepciones.txt
         - name: Publicar SARIF
           if: always()
           uses: actions/upload-artifact@330a01c490aca151604b8cf639adc76d48f6c5d4 # v5.0.0
           with:
             name: sarif-${{ matrix.lenguaje }}
             path: sarif-results/
             if-no-files-found: warn
   ```
5. Agrega la fila de `umbral-sarif.py` en `scripts/README.md` (Script, Qué comprueba, Uso).
6. Abre el PR y mira los hallazgos reales **antes** de pedir que SAST sea obligatorio. Cada hallazgo de 7,0 o más se corrige o va a `excepciones.txt` con su fila en docs/seguridad.md.

**Cómo comprobarlo:**
- En el PR aparecen `SAST (java-kotlin)` y `SAST (javascript-typescript)`, cada uno con su artefacto `sarif-...`, y los resultados se ven en Security > Code scanning.
- Prueba local del umbral con un SARIF descargado del artefacto: `python3 scripts/umbral-sarif.py ruta/al/sarif 7.0` (en Windows, si `python3` no existe, prueba con `py -3`).
- Controlador de prueba en una rama desechable `prueba/sast`: `src/main/java/petfinder/adaptadores/entrada/web/DiagnosticoController.java` con un `GET /api/diagnostico` que pase un `@RequestParam String comando` a `new ProcessBuilder("sh", "-c", comando).start()`. El paso del umbral sale en rojo con `java/command-line-injection`. Guarda el enlace y borra la rama.

**Trampas:**
- Con matriz, los checks se llaman `SAST (java-kotlin)` y `SAST (javascript-typescript)`, no `SAST`: esos son los nombres que van al ruleset.
- Si en S1 quedó activo "Default setup" de CodeQL, la subida del SARIF falla. Apágalo.
- CodeQL es el job más lento (se estiman 4 a 6 minutos). Corre en paralelo con CI, así que no suma al total.
- Si `paths-ignore` no excluye `src/test` en Java con `build-mode: none`, verás en el SARIF hallazgos con ruta `src/test/...`. No bajes el umbral: si alguno es de 7,0 o más, agrega su línea `regla ruta` a `excepciones.txt` y su fila en docs/seguridad.md como falso positivo ("código de pruebas, no va en el jar ni corre en producción").

**Pedido para tu agente:** "Crea los archivos de la sección S6 de esta guía (`scripts/umbral-sarif.py`, `.github/codeql/codeql-config.yml`, `.github/codeql/excepciones.txt`, el job `sast` en `seguridad.yml` y la fila en `scripts/README.md`), prueba el script con un SARIF de ejemplo y dime qué falta para abrir el PR."

## S7 · Persistencia real con PostgreSQL y Flyway (pendiente 2)

**Para qué:** Contenerización (5), pendiente 2 del Corte 2 (va a evolucion.md) y amenaza T10. Hoy Compose usa `ddl-auto: create` y cada reinicio borra los casos. Con `update` tampoco basta: medido, el siguiente POST devuelve 201 y pisa un reporte, porque el generador de IDs arranca en 0.
**Depende de:** D3 (Flyway, confirmada). Aplícalo después de que M2 entre a main. **Desbloquea:** S9, M6 (Render con PostgreSQL) y A13.
**Hecho cuando (del plan):** Con compose: POST, docker compose restart app, GET devuelve la misma mascota; el siguiente POST da un ID nuevo y la lista crece en 1.
**Archivos:** `pom.xml`, `docker-compose.yml`, `src/main/resources/application.properties`, `src/main/resources/db/migration/V1__esquema_inicial.sql` (nuevo), `RepositorioReportes.java` (puerto), `ReporteJpaRepository.java`, `RepositorioReportesH2.java`, `RepositorioReportesEnMemoria.java`, `GeneradorIdReportes.java`, `ConfiguracionPetFinder.java`, `DatosDeEjemplo.java`, `GeneradorIdReportesTest.java`, `RepositorioReportesH2IT.java`, `ReinicioPersistenciaIT.java` (nueva) y `src/test/resources/config/application.properties` (nuevo).

**Estado:** el cambio ya pasó `./mvnw -q clean verify` en local (235 unitarias y 26 IT en verde, junto con S4), **con H2**. Flyway contra PostgreSQL está **sin probar todavía**: lo prueban el check `despliegue-y-verificacion` de A6 o tu prueba con Compose.

**Qué cambia:**
- Flyway crea el esquema (`V1__esquema_inicial.sql`, SQL común a H2 y PostgreSQL) y Hibernate solo lo valida (`ddl-auto=validate`).
- El puerto `RepositorioReportes` gana `ultimoNumeroDeId()`. `GeneradorIdReportes` arranca después de ese número, comparado como número: como texto, `PF-1000` queda antes que `PF-999`.
- `DatosDeEjemplo` no carga nada si la base ya tiene reportes, incluidos los cerrados.
- Las pruebas reciben una base H2 propia por contexto (`src/test/resources/config/application.properties`): desde Flyway el esquema ya no se borra entre contextos.
- `ReinicioPersistenciaIT` arranca la app dos veces contra la misma base y comprueba que el caso sobrevive, que el ID nuevo es mayor y que la lista crece en 1.

**Cómo hacerlo:**

1. Después de que M2 entre: `git switch main && git pull && git switch -c corte3/S7`
2. Aplica el cambio desde `docs/tareas-corte-3/parches/S7.patch` (es el mismo bloque de abajo; usa el archivo y no una copia del Markdown):
   ```bash
   git apply --check docs/tareas-corte-3/parches/S7.patch && git apply docs/tareas-corte-3/parches/S7.patch
   # si --check falla por los cambios de M2 o A3 (con M2 en main falla seguro en application.properties):
   git apply --reject docs/tareas-corte-3/parches/S7.patch
   ```
   No uses `--3way`: el parche no trae las líneas `index` y Git responde "repository lacks the necessary blob to perform 3-way merge". Con `--reject`, Git aplica todo lo que encaja y deja lo que no en `src/main/resources/application.properties.rej` (y en `docker-compose.yml.rej` si A3 ya cambió esa zona). Comprobado contra la carpeta de M2: solo se rechaza el bloque de `application.properties`. Resuelve cada `.rej` con el paso 3, bórralos (`find . -name '*.rej' -delete`) y revisa `git status` antes del commit.
3. **Conflicto esperado con M2** en `application.properties`: conserva la consola apagada. La zona debe quedar así:
   ```properties
   # Sin SPRING_DATASOURCE_URL la app usa H2 en memoria (local y pruebas): los datos se pierden al reiniciar.
   # Con PostgreSQL (Compose, Render) los datos quedan en la base.
   spring.datasource.url=jdbc:h2:mem:petfinder;DB_CLOSE_DELAY=-1
   spring.datasource.username=sa
   spring.datasource.password=
   spring.jpa.open-in-view=false
   # El esquema lo crea Flyway (src/main/resources/db/migration) y Hibernate solo lo compara con las
   # entidades: si no coinciden, la app no arranca, en vez de crear o borrar tablas por su cuenta.
   spring.jpa.hibernate.ddl-auto=validate
   # Consola de H2 apagada: en Render respondía 200 a cualquiera. Solo se prende con el
   # perfil local (application-local.properties), en el computador de quien desarrolla.
   spring.h2.console.enabled=false

   # Casos de ejemplo al arrancar, solo si la base no tiene ningún reporte
   petfinder.datos-ejemplo=true
   ```
   Si A3 ya cambió `docker-compose.yml`, lo único tuyo ahí es quitar `SPRING_JPA_HIBERNATE_DDL_AUTO: create` y su comentario, y dejar el comentario nuevo. Conserva `SPRING_H2_CONSOLE_ENABLED: "false"`.

```diff
diff --git a/docker-compose.yml b/docker-compose.yml
--- a/docker-compose.yml
+++ b/docker-compose.yml
@@ -14,9 +14,8 @@
       SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/${POSTGRES_DB:-petfinder}
       SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER:?Defina POSTGRES_USER (ver .env.example)}
       SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:?Defina POSTGRES_PASSWORD (ver .env.example)}
-      # Las tablas se recrean en cada arranque de la app. El generador de IDs (PF-001...) vive en
-      # memoria y vuelve a empezar al reiniciar; con "update" pisaría reportes ya guardados.
-      SPRING_JPA_HIBERNATE_DDL_AUTO: create
+      # Sin SPRING_JPA_HIBERNATE_DDL_AUTO: el esquema lo crea Flyway al arrancar (db/migration) y Hibernate
+      # solo lo valida. Los casos sobreviven a docker compose restart; down -v sí borra la base.
       # La consola de H2 no aplica con PostgreSQL
       SPRING_H2_CONSOLE_ENABLED: "false"
       # Asistente con Claude (opcional: sin clave funciona con reglas)
diff --git a/pom.xml b/pom.xml
--- a/pom.xml
+++ b/pom.xml
@@ -61,6 +61,16 @@
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-h2console</artifactId>
         </dependency>
+        <!-- Migraciones del esquema (S7, decisión D3): Flyway crea las tablas y Hibernate solo las valida.
+             El segundo módulo es el soporte de PostgreSQL, que desde Flyway 10 va aparte. Versiones: las de Spring Boot. -->
+        <dependency>
+            <groupId>org.springframework.boot</groupId>
+            <artifactId>spring-boot-starter-flyway</artifactId>
+        </dependency>
+        <dependency>
+            <groupId>org.flywaydb</groupId>
+            <artifactId>flyway-database-postgresql</artifactId>
+        </dependency>
 
         <!-- Adaptador de salida: extractor con Claude (SDK oficial de Java) -->
         <dependency>
diff --git a/src/main/java/petfinder/adaptadores/salida/persistencia/h2/ReporteJpaRepository.java b/src/main/java/petfinder/adaptadores/salida/persistencia/h2/ReporteJpaRepository.java
--- a/src/main/java/petfinder/adaptadores/salida/persistencia/h2/ReporteJpaRepository.java
+++ b/src/main/java/petfinder/adaptadores/salida/persistencia/h2/ReporteJpaRepository.java
@@ -3,6 +3,7 @@
 import java.util.List;
 
 import org.springframework.data.jpa.repository.JpaRepository;
+import org.springframework.data.jpa.repository.Query;
 
 import petfinder.domain.model.EstadoReporte;
 
@@ -10,4 +11,8 @@
 public interface ReporteJpaRepository extends JpaRepository<ReporteEntity, String> {
 
     List<ReporteEntity> findByEstadoOrderByFechaCreacionDesc(EstadoReporte estado);
+
+    /** Solo los identificadores: al arrancar no hace falta cargar los reportes completos. */
+    @Query("select r.id from ReporteEntity r")
+    List<String> listarIds();
 }
diff --git a/src/main/java/petfinder/adaptadores/salida/persistencia/h2/RepositorioReportesH2.java b/src/main/java/petfinder/adaptadores/salida/persistencia/h2/RepositorioReportesH2.java
--- a/src/main/java/petfinder/adaptadores/salida/persistencia/h2/RepositorioReportesH2.java
+++ b/src/main/java/petfinder/adaptadores/salida/persistencia/h2/RepositorioReportesH2.java
@@ -9,6 +9,7 @@
 import petfinder.domain.model.Avistamiento;
 import petfinder.domain.model.Contacto;
 import petfinder.domain.model.EstadoReporte;
+import petfinder.domain.model.GeneradorIdReportes;
 import petfinder.domain.model.Mascota;
 import petfinder.domain.model.ReporteEncontrada;
 import petfinder.domain.model.ReporteMascota;
@@ -58,6 +59,15 @@
         return jpa.findByEstadoOrderByFechaCreacionDesc(EstadoReporte.ACTIVO).stream()
                 .map(RepositorioReportesH2::aDominio)
                 .toList();
+    }
+
+    @Override
+    @Transactional(readOnly = true)
+    public int ultimoNumeroDeId() {
+        return jpa.listarIds().stream()
+                .mapToInt(GeneradorIdReportes::numeroDe)
+                .max()
+                .orElse(0);
     }
 
     private static void copiar(ReporteMascota reporte, ReporteEntity fila) {
diff --git a/src/main/java/petfinder/adaptadores/salida/persistencia/memoria/RepositorioReportesEnMemoria.java b/src/main/java/petfinder/adaptadores/salida/persistencia/memoria/RepositorioReportesEnMemoria.java
--- a/src/main/java/petfinder/adaptadores/salida/persistencia/memoria/RepositorioReportesEnMemoria.java
+++ b/src/main/java/petfinder/adaptadores/salida/persistencia/memoria/RepositorioReportesEnMemoria.java
@@ -5,6 +5,7 @@
 import java.util.Map;
 import java.util.Optional;
 
+import petfinder.domain.model.GeneradorIdReportes;
 import petfinder.domain.model.ReporteMascota;
 import petfinder.application.port.salida.RepositorioReportes;
 /**
@@ -43,5 +44,13 @@
         return reportes.values().stream()
                 .filter(ReporteMascota::estaActivo)
                 .toList();
+    }
+
+    @Override
+    public int ultimoNumeroDeId() {
+        return reportes.keySet().stream()
+                .mapToInt(GeneradorIdReportes::numeroDe)
+                .max()
+                .orElse(0);
     }
 }
\ No newline at end of file
diff --git a/src/main/java/petfinder/application/port/salida/RepositorioReportes.java b/src/main/java/petfinder/application/port/salida/RepositorioReportes.java
--- a/src/main/java/petfinder/application/port/salida/RepositorioReportes.java
+++ b/src/main/java/petfinder/application/port/salida/RepositorioReportes.java
@@ -35,4 +35,15 @@
      * @return lista de reportes activos
      */
     List<ReporteMascota> listarActivos();
+
+    /**
+     * Mayor número usado en un identificador PF-, o 0 si no hay reportes.
+     *
+     * Lo pide config al arrancar: el generador sigue desde ahí en vez de
+     * empezar en PF-001 y pisar casos guardados, y DatosDeEjemplo no carga
+     * nada sobre una base que ya tiene casos, aunque estén cerrados.
+     *
+     * @return el mayor número, comparado como número y no como texto
+     */
+    int ultimoNumeroDeId();
 }
\ No newline at end of file
diff --git a/src/main/java/petfinder/config/ConfiguracionPetFinder.java b/src/main/java/petfinder/config/ConfiguracionPetFinder.java
--- a/src/main/java/petfinder/config/ConfiguracionPetFinder.java
+++ b/src/main/java/petfinder/config/ConfiguracionPetFinder.java
@@ -34,9 +34,13 @@
 @Configuration
 public class ConfiguracionPetFinder {
 
+    /**
+     * Arranca después del último PF- guardado: con PostgreSQL la base sobrevive
+     * a los reinicios y el contador en memoria no.
+     */
     @Bean
-    public GeneradorIdReportes generadorIdReportes() {
-        return new GeneradorIdReportes();
+    public GeneradorIdReportes generadorIdReportes(RepositorioReportes repositorio) {
+        return new GeneradorIdReportes(repositorio.ultimoNumeroDeId());
     }
 
     @Bean
diff --git a/src/main/java/petfinder/config/DatosDeEjemplo.java b/src/main/java/petfinder/config/DatosDeEjemplo.java
--- a/src/main/java/petfinder/config/DatosDeEjemplo.java
+++ b/src/main/java/petfinder/config/DatosDeEjemplo.java
@@ -7,6 +7,7 @@
 import org.springframework.stereotype.Component;
 
 import petfinder.application.port.entrada.GestionReportes;
+import petfinder.application.port.salida.RepositorioReportes;
 import petfinder.domain.model.Contacto;
 import petfinder.domain.model.Mascota;
 import petfinder.domain.model.SolicitudReporte;
@@ -14,8 +15,9 @@
 import petfinder.domain.model.Ubicacion;
 
 /**
- * Carga casos de ejemplo al arrancar, porque H2 en memoria empieza vacío en
- * cada reinicio (también en Render). Entra por el puerto GestionReportes, como
+ * Carga casos de ejemplo al arrancar cuando la base está vacía: H2 en memoria
+ * empieza vacío en cada reinicio, pero PostgreSQL no, y cargarlos otra vez
+ * duplicaría a Luna, Max, la gata y Copito en cada reinicio. Entra por el puerto GestionReportes, como
  * cualquier otro adaptador: los datos pasan por las mismas validaciones.
  * Solo corre en la aplicación web (la demo de consola conserva sus PF-001 y
  * PF-002 del Corte 1) y se apaga con petfinder.datos-ejemplo=false.
@@ -26,13 +28,19 @@
 public class DatosDeEjemplo implements ApplicationRunner {
 
     private final GestionReportes gestion;
+    private final RepositorioReportes repositorio;
 
-    public DatosDeEjemplo(GestionReportes gestion) {
+    public DatosDeEjemplo(GestionReportes gestion, RepositorioReportes repositorio) {
         this.gestion = gestion;
+        this.repositorio = repositorio;
     }
 
     @Override
     public void run(ApplicationArguments args) {
+        // Cualquier reporte cuenta, también los cerrados: listarActivos() no basta.
+        if (repositorio.ultimoNumeroDeId() > 0) {
+            return;
+        }
         gestion.registrar(TipoReporte.PERDIDA, SolicitudReporte.paraPerdida(
                 new Ubicacion("Cedritos", "Parque de la 147", 4.7235, -74.0417),
                 "Se escapó del conjunto esta tarde. Es tímida: si la ves, no la persigas.",
diff --git a/src/main/java/petfinder/domain/model/GeneradorIdReportes.java b/src/main/java/petfinder/domain/model/GeneradorIdReportes.java
--- a/src/main/java/petfinder/domain/model/GeneradorIdReportes.java
+++ b/src/main/java/petfinder/domain/model/GeneradorIdReportes.java
@@ -1,6 +1,8 @@
 package petfinder.domain.model;
 
 import java.util.concurrent.atomic.AtomicInteger;
+import java.util.regex.Matcher;
+import java.util.regex.Pattern;
 
 /**
  * Entrega identificadores consecutivos con el formato PF-001.
@@ -14,17 +16,52 @@
  * generar identificadores en este alcance. Crear la abstraccion por si acaso
  * seria generalidad especulativa; la inversion de dependencias se aplica
  * donde si hay una frontera real de sustitucion, que es el almacenamiento.
+ *
+ * Límite conocido: el contador vive en el proceso. Con una sola instancia de
+ * la app basta; dos instancias contra la misma base repetirían números.
  */
 public class GeneradorIdReportes {
 
+    private static final Pattern FORMATO = Pattern.compile("PF-(\\d{1,9})");
+
     /**
      * AtomicInteger y no int: con la API, dos peticiones simultáneas podían
      * leer el mismo valor con secuencia++ y recibir el mismo PF-00X, y la
      * segunda sobrescribía a la primera en el repositorio.
      */
-    private final AtomicInteger secuencia = new AtomicInteger();
+    private final AtomicInteger secuencia;
 
+    /** Base vacía: el primer identificador es PF-001. */
+    public GeneradorIdReportes() {
+        this(0);
+    }
+
+    /**
+     * Arranca después del último número que ya está en la base. Con la base
+     * persistente (PostgreSQL), empezar siempre en 0 hacía que el primer POST
+     * tras un reinicio devolviera 201 con PF-001 y pisara el reporte guardado.
+     */
+    public GeneradorIdReportes(int ultimoNumeroUsado) {
+        if (ultimoNumeroUsado < 0) {
+            throw new IllegalArgumentException("El último número usado no puede ser negativo: " + ultimoNumeroUsado);
+        }
+        this.secuencia = new AtomicInteger(ultimoNumeroUsado);
+    }
+
     public String siguiente() {
         return String.format("PF-%03d", secuencia.incrementAndGet());
+    }
+
+    /**
+     * Número de un identificador PF-, o 0 si no tiene ese formato. Existe para
+     * comparar como número: como texto, "PF-1000" queda antes que "PF-999" y
+     * el máximo saldría mal.
+     */
+    public static int numeroDe(String id) {
+        if (id == null) {
+            return 0;
+        }
+        Matcher m = FORMATO.matcher(id);
+        return m.matches() ? Integer.parseInt(m.group(1)) : 0;
     }
 }
diff --git a/src/main/resources/application.properties b/src/main/resources/application.properties
--- a/src/main/resources/application.properties
+++ b/src/main/resources/application.properties
@@ -9,12 +9,16 @@
 # Activa /actuator/health/readiness, que usa el HEALTHCHECK del Dockerfile
 management.endpoint.health.probes.enabled=true
 
-# H2 en memoria: los datos se pierden al reiniciar (límite declarado)
+# Sin SPRING_DATASOURCE_URL la app usa H2 en memoria (local y pruebas): los datos se pierden al reiniciar.
+# Con PostgreSQL (Compose, Render) los datos quedan en la base.
 spring.datasource.url=jdbc:h2:mem:petfinder;DB_CLOSE_DELAY=-1
 spring.datasource.username=sa
 spring.datasource.password=
 spring.jpa.open-in-view=false
+# El esquema lo crea Flyway (src/main/resources/db/migration) y Hibernate solo lo compara con las
+# entidades: si no coinciden, la app no arranca, en vez de crear o borrar tablas por su cuenta.
+spring.jpa.hibernate.ddl-auto=validate
 spring.h2.console.enabled=true
 
-# Casos de ejemplo al arrancar (H2 empieza vacío en cada reinicio)
+# Casos de ejemplo al arrancar, solo si la base no tiene ningún reporte
 petfinder.datos-ejemplo=true
diff --git a/src/main/resources/db/migration/V1__esquema_inicial.sql b/src/main/resources/db/migration/V1__esquema_inicial.sql
new file mode 100644
--- /dev/null
+++ b/src/main/resources/db/migration/V1__esquema_inicial.sql
@@ -0,0 +1,48 @@
+-- Esquema inicial de Pet Finder (Corte 3, tarea S7). Desde aquí las tablas las crea Flyway y Hibernate
+-- solo las valida (spring.jpa.hibernate.ddl-auto=validate). Sale de ReporteEntity y AvistamientoEntity.
+-- SQL común: el mismo archivo corre en H2 (local y pruebas) y en PostgreSQL (Compose y Render).
+-- Los enum van como texto (EnumType.STRING) y sin CHECK: en H2 2.4 un CHECK con IN (...) falla con
+-- "Check constraint invalid ... The database has been closed" cuando se cierra la conexión que creó la tabla.
+-- Nunca se edita un archivo ya aplicado: Flyway guarda su checksum y la app no arranca si cambia.
+
+create table reportes (
+    id                  varchar(255)  not null,
+    tipo                varchar(255)  not null,
+    estado              varchar(255)  not null,
+    fecha_creacion      timestamp(6)  not null,
+    zona                varchar(255),
+    referencia          varchar(255),
+    latitud             float(53),
+    longitud            float(53),
+    descripcion         varchar(2000),
+    contacto_nombre     varchar(255),
+    contacto_medio      varchar(255),
+    mascota_nombre      varchar(255),
+    mascota_especie     varchar(255),
+    mascota_raza        varchar(255),
+    mascota_color       varchar(255),
+    mascota_senas       varchar(255),
+    descripcion_mascota varchar(2000),
+    constraint pk_reportes primary key (id)
+);
+
+create index idx_reportes_estado on reportes (estado);
+
+create table avistamientos (
+    id              varchar(255)  not null,
+    reporte_id      varchar(255)  not null,
+    fecha_hora      timestamp(6)  not null,
+    zona            varchar(255),
+    referencia      varchar(255),
+    latitud         float(53),
+    longitud        float(53),
+    descripcion     varchar(2000),
+    contacto_nombre varchar(255),
+    contacto_medio  varchar(255),
+    tipo            varchar(255),
+    constraint pk_avistamientos primary key (id),
+    constraint fk_avistamientos_reporte foreign key (reporte_id) references reportes (id)
+);
+
+-- PostgreSQL no indexa solo las llaves foráneas; la lista con sus avistamientos (S8) busca por aquí.
+create index idx_avistamientos_reporte on avistamientos (reporte_id);
diff --git a/src/test/java/petfinder/domain/GeneradorIdReportesTest.java b/src/test/java/petfinder/domain/GeneradorIdReportesTest.java
--- a/src/test/java/petfinder/domain/GeneradorIdReportesTest.java
+++ b/src/test/java/petfinder/domain/GeneradorIdReportesTest.java
@@ -1,6 +1,7 @@
 package petfinder.domain;
 
 import static org.junit.jupiter.api.Assertions.assertEquals;
+import static org.junit.jupiter.api.Assertions.assertThrows;
 
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
@@ -33,6 +34,41 @@
         String siguiente = generador.siguiente();
         // Assert
         assertEquals("PF-1000", siguiente);
+    }
+
+    @Test
+    @DisplayName("Con la base en PF-041, el siguiente identificador es PF-042")
+    void sigueDespuesDelUltimoGuardado() {
+        // Arrange
+        GeneradorIdReportes generador = new GeneradorIdReportes(41);
+        // Act
+        String siguiente = generador.siguiente();
+        // Assert
+        assertEquals("PF-042", siguiente);
+    }
+
+    @Test
+    @DisplayName("numeroDe compara como número: PF-1000 es mayor que PF-999")
+    void numeroDeComparaComoNumero() {
+        // Act
+        int mayor = Math.max(GeneradorIdReportes.numeroDe("PF-999"), GeneradorIdReportes.numeroDe("PF-1000"));
+        // Assert
+        assertEquals(1000, mayor);
+    }
+
+    @Test
+    @DisplayName("numeroDe devuelve 0 si el texto no tiene el formato PF-número")
+    void numeroDeSinFormato() {
+        // Act y Assert
+        assertEquals(0, GeneradorIdReportes.numeroDe("PF-T01"));
+        assertEquals(0, GeneradorIdReportes.numeroDe(null));
+    }
+
+    @Test
+    @DisplayName("Un último número negativo se rechaza")
+    void rechazaNegativo() {
+        // Act y Assert
+        assertThrows(IllegalArgumentException.class, () -> new GeneradorIdReportes(-1));
     }
 
     @Test
diff --git a/src/test/java/petfinder/integracion/persistencia/ReinicioPersistenciaIT.java b/src/test/java/petfinder/integracion/persistencia/ReinicioPersistenciaIT.java
new file mode 100644
--- /dev/null
+++ b/src/test/java/petfinder/integracion/persistencia/ReinicioPersistenciaIT.java
@@ -0,0 +1,73 @@
+package petfinder.integracion.persistencia;
+
+import static org.junit.jupiter.api.Assertions.assertEquals;
+import static org.junit.jupiter.api.Assertions.assertTrue;
+
+import java.util.UUID;
+
+import org.junit.jupiter.api.DisplayName;
+import org.junit.jupiter.api.Test;
+import org.springframework.boot.builder.SpringApplicationBuilder;
+import org.springframework.context.ConfigurableApplicationContext;
+
+import petfinder.PetFinderApplication;
+import petfinder.application.port.entrada.GestionReportes;
+import petfinder.domain.model.Contacto;
+import petfinder.domain.model.GeneradorIdReportes;
+import petfinder.domain.model.Mascota;
+import petfinder.domain.model.ReportePerdida;
+import petfinder.domain.model.SolicitudReporte;
+import petfinder.domain.model.TipoReporte;
+import petfinder.domain.model.Ubicacion;
+
+/**
+ * Simula un reinicio contra una base que sobrevive, como PostgreSQL en Compose:
+ * la aplicación arranca dos veces seguidas contra la misma base H2 con nombre
+ * (DB_CLOSE_DELAY=-1 la mantiene viva entre los dos arranques). Antes de S7 el
+ * segundo arranque volvía a PF-001 y el siguiente POST pisaba el caso guardado.
+ */
+class ReinicioPersistenciaIT {
+
+    @Test
+    @DisplayName("Tras reiniciar, el caso sigue ahí, el siguiente ID es nuevo y la lista crece en 1")
+    void elCasoSobreviveAlReinicio() {
+        // Arrange
+        String base = "--spring.datasource.url=jdbc:h2:mem:reinicio-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
+        String idAntes;
+        int activosAntes;
+        try (ConfigurableApplicationContext app = arrancar(base)) {
+            GestionReportes gestion = app.getBean(GestionReportes.class);
+            idAntes = gestion.registrar(TipoReporte.PERDIDA, perdida("Toby")).getId();
+            activosAntes = gestion.listarActivos().size();
+        }
+
+        // Act
+        try (ConfigurableApplicationContext app = arrancar(base)) {
+            GestionReportes gestion = app.getBean(GestionReportes.class);
+            ReportePerdida guardado = (ReportePerdida) gestion.consultar(idAntes);
+            String idNuevo = gestion.registrar(TipoReporte.PERDIDA, perdida("Nala")).getId();
+            int activosDespues = gestion.listarActivos().size();
+
+            // Assert
+            assertEquals("Toby", guardado.getMascota().nombre());
+            assertTrue(GeneradorIdReportes.numeroDe(idNuevo) > GeneradorIdReportes.numeroDe(idAntes),
+                    "El ID después del reinicio debe ser nuevo: antes " + idAntes + ", después " + idNuevo);
+            assertEquals(activosAntes + 1, activosDespues,
+                    "Solo debe sumarse Nala: los casos de ejemplo no se cargan otra vez");
+        }
+    }
+
+    private static ConfigurableApplicationContext arrancar(String base) {
+        // Argumentos de línea de comandos: ganan sobre application.properties y sobre el .env local.
+        return new SpringApplicationBuilder(PetFinderApplication.class)
+                .run("--server.port=0", "--ANTHROPIC_API_KEY=", base);
+    }
+
+    private static SolicitudReporte perdida(String nombre) {
+        return SolicitudReporte.paraPerdida(
+                new Ubicacion("Chapinero", "Parque de los Hippies", 4.6486, -74.0628),
+                "Salió por la puerta del garaje.",
+                new Contacto("Prueba de reinicio", "3001112233"),
+                new Mascota(nombre, "perro", "criollo", "negro", "collar verde"));
+    }
+}
diff --git a/src/test/java/petfinder/integracion/persistencia/RepositorioReportesH2IT.java b/src/test/java/petfinder/integracion/persistencia/RepositorioReportesH2IT.java
--- a/src/test/java/petfinder/integracion/persistencia/RepositorioReportesH2IT.java
+++ b/src/test/java/petfinder/integracion/persistencia/RepositorioReportesH2IT.java
@@ -88,6 +88,18 @@
     }
 
     @Test
+    @DisplayName("El último número de ID se compara como número, no como texto")
+    void ultimoNumeroDeIdEsNumerico() {
+        // Arrange
+        repositorio.guardar(perdida("PF-999", EstadoReporte.ACTIVO, List.of()));
+        repositorio.guardar(perdida("PF-1000", EstadoReporte.CERRADO, List.of()));
+        // Act
+        int ultimo = repositorio.ultimoNumeroDeId();
+        // Assert
+        assertEquals(1000, ultimo);
+    }
+
+    @Test
     @DisplayName("Listar activos excluye resueltos y cerrados")
     void listarActivosFiltraPorEstado() {
         // Arrange
diff --git a/src/test/resources/config/application.properties b/src/test/resources/config/application.properties
new file mode 100644
--- /dev/null
+++ b/src/test/resources/config/application.properties
@@ -0,0 +1,5 @@
+# Solo para pruebas: Spring Boot lee classpath:/config/application.properties además del de
+# src/main/resources y con más prioridad. Cada contexto de prueba recibe su propia base H2.
+# Desde S7 el esquema lo crea Flyway y ya no se borra al arrancar otro contexto, así que sin
+# esto los datos de una prueba aparecerían en la siguiente según el orden en que corran.
+spring.datasource.url=jdbc:h2:mem:pruebas-${random.uuid};DB_CLOSE_DELAY=-1
```

4. `./mvnw -q clean verify`, commit, push y PR.

**Cómo comprobarlo:**

```bash
./mvnw -q verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=ReinicioPersistenciaIT -Djacoco.skip=true
```

"Hecho cuando" con Compose (necesita Docker Desktop y A3 en main; el `.env` lo creas tú desde `.env.example` y nunca se commitea):

```bash
# Publica un caso con el nombre de mascota que recibe y escribe su id (PF-...)
publicar() {
  curl -s -X POST http://localhost:8080/api/reportes -H 'Content-Type: application/json' -d '{
    "tipo": "PERDIDA", "zona": "Chapinero", "referencia": "Parque de los Hippies",
    "descripcion": "Prueba de reinicio de S7", "nombreContacto": "Prueba S7", "medioContacto": "3001112233",
    "mascota": { "nombre": "'"$1"'", "especie": "perro" } }' | grep -o '"id":"PF-[0-9]*"' | head -n 1 | cut -d'"' -f4
}
contar() { curl -s http://localhost:8080/api/reportes | grep -o '"id":"PF-[0-9]*"' | wc -l; }

docker compose up -d --build --wait
id1=$(publicar TobyS7); antes=$(contar); echo "primer caso: $id1, casos activos: $antes"
docker compose restart app && docker compose up -d --wait
curl -s "http://localhost:8080/api/reportes/$id1" | grep -o '"nombreMascota":"TobyS7"'   # esperado: sigue ahí
id2=$(publicar NalaS7); despues=$(contar); echo "segundo caso: $id2, casos activos: $despues"
# esperado: id2 es mayor que id1 (nunca PF-001) y despues = antes + 1
docker compose down      # sin -v: -v borra la base
```

**Trampas:**
- Si tienes un volumen de Compose de antes de S7 (tablas creadas por Hibernate), Flyway falla con "Found non-empty schema(s) without schema history table". Bórralo una vez con `docker compose down -v`.
- Nunca edites `V1__esquema_inicial.sql` después de integrarlo: Flyway guarda su checksum y la app no arranca. Los cambios van en `V2__...` (S9 agrega la columna del código de gestión).
- Límite declarado: el contador vive en el proceso. Con una instancia basta; dos instancias contra la misma base repetirían IDs. Va a docs/seguridad.md y a evolucion.md.
- `flyway-database-postgresql` es obligatorio desde Flyway 10; sin él, Flyway no reconoce PostgreSQL. Las versiones salen del BOM de Spring Boot: no les pongas `<version>`.

**Pedido para tu agente:** "Aplica el parche de la sección S7 de esta guía en `corte3/S7`, resuelve `application.properties` como dice el paso 3 (consola H2 apagada) y corre `./mvnw -q clean verify`. No hagas commit."

---

# Tramo 2 · Endurecer (lun 9 a mié 11 nov)

## S8 · Quitar el N+1 y limitar la lista (pendiente 3)

**Para qué:** pendiente 3 del Corte 2 (va a evolucion.md) y margen para el SLO en Render (M7). Hoy `listarActivos()` trae los reportes y luego carga los avistamientos de cada uno por separado (`@OneToMany` perezoso en `ReporteEntity`): 1 + N consultas. En la carga del Corte 2, `voz_listar` tuvo p95 de 106,1 ms y `voz_registrar` 15,7 ms.
**Depende de:** nada (mejor después de S7, que ya creó `idx_avistamientos_reporte`). **Desbloquea:** ayuda a M7.
**Hecho cuando (del plan):** Con show-sql, listar hace una sola consulta; p95 de voz_listar queda cerca del de voz_registrar en perf/README.md.
**Archivos:** `ReporteJpaRepository.java`, `RepositorioReportesH2.java`, `ServicioComandosVoz.java`, `application.properties`, una IT nueva en `src/test/java/petfinder/integracion/persistencia/`, `perf/README.md`.

**Cómo hacerlo** (todo **sin probar todavía**):

1. Una sola consulta con `JOIN FETCH` y tope en SQL, en `ReporteJpaRepository`:
   ```java
   @Query("""
           select distinct r from ReporteEntity r
           left join fetch r.avistamientos
           where r.id in (
               select r2.id from ReporteEntity r2
               where r2.estado = :estado
               order by r2.fechaCreacion desc
               limit :maximo)
           order by r.fechaCreacion desc
           """)
   List<ReporteEntity> activosConAvistamientos(@Param("estado") EstadoReporte estado, @Param("maximo") int maximo);
   ```
   No uses `Pageable` con `join fetch` de una colección: Hibernate pagina en memoria (aviso HHH90003004) y trae todo igual. Si Hibernate no acepta el `limit` dentro de la subconsulta, usa dos consultas fijas (IDs con tope y luego `join fetch` por esos IDs) y explica en el PR por qué son 2 y no 1.
2. `RepositorioReportesH2.listarActivos()` usa esa consulta con un tope que llega desde config (`petfinder.lista.maximo=200`, constructor del adaptador en `config/`). La firma del puerto no cambia, así la interfaz y `ReinicioPersistenciaIT` siguen igual: `GET /api/reportes` devuelve el mismo arreglo, con los 200 más recientes como máximo.
3. "ver reportes" por voz: en `ServicioComandosVoz.listarActivos()`, nombra los 10 más recientes y di cuántos hay en total.
4. IT que cuente consultas: con `spring.jpa.properties.hibernate.generate_statistics=true`, crea 3 reportes con avistamientos, limpia las estadísticas (`entityManagerFactory.unwrap(SessionFactory.class).getStatistics().clear()`), llama `listarActivos()` y exige `getPrepareStatementCount() == 1`. Fija `ANTHROPIC_API_KEY=` y no uses IDs fijos.
5. Mide otra vez con k6 (4 min 30 s) y agrega una fila a las tablas de `perf/README.md`, con fecha y commit:
   ```bash
   ./mvnw spring-boot:run          # en otra terminal
   k6 run perf/scripts/carga.js
   ```

**Cómo comprobarlo:** la IT del paso 4 en verde; con `./mvnw spring-boot:run -Dspring-boot.run.arguments=--spring.jpa.show-sql=true` y un `curl -s http://localhost:8080/api/reportes > /dev/null`, la consola muestra un solo `select`; en `perf/README.md`, el p95 de `voz_listar` queda cerca del de `voz_registrar`.

**Trampas:**
- La interfaz (mapa, lista, Cerca de ti) espera un arreglo. Un objeto `Page` la rompe.
- No pongas `FetchType.EAGER` en la entidad: arregla esta lista y empeora todas las demás consultas.

**Pedido para tu agente:** "Haz S8 siguiendo esa sección de la guía: consulta única con tope, tope desde config, voz con 10, IT que cuente consultas y la tabla de perf/README.md con lo que yo mida."

## S9 · Código de gestión: núcleo (pendiente 4)

**Para qué:** DevSecOps (20): con usuarios, la autenticación y la autorización deben estar entre las mitigaciones (amenaza T1: hoy cualquiera cierra el caso de otra familia, y cualquier página puede dispararlo). También es el pendiente 4 del Corte 2 y la decisión D1.
**Depende de:** D1 y S7. Se hace junto con A9 (web, Antonio) y M10 (interfaz, Mateo). **Desbloquea:** A9, M10, S16 y A13.
**Hecho cuando (del plan):** Una IT espera 403 sin código, 403 con el de otro caso y 204 con el correcto o el de administrador; el código no aparece en la lista ni en el detalle.
**Archivos:** `domain/model/` (código nuevo), `domain/exception/` (excepción nueva), `GestionReportes.java`, `ServicioReportes.java`, `ServicioComandosVoz.java`, `ProcesadorComandosVoz.java` (ahí vive el record `ResultadoComandoVoz`; no tiene archivo propio), la respuesta de `VozController`, `adaptadores/entrada/consola/`, `ReporteEntity.java`, `RepositorioReportesH2.java`, `db/migration/V2__codigo_gestion.sql`, `config/`, `application.properties`, `.env.example`, `docker-compose.yml`.

**Antes de empezar, acuerda con Antonio el contrato** (lun 9): el nombre del campo en la respuesta de publicar (`codigoGestion`), la cabecera `X-Codigo-Gestion` y estos métodos del puerto.

**Cómo hacerlo** (diseño propuesto, **sin probar todavía**):

1. **Dominio.** Un `record CodigoGestion` en `domain/model` con:
   - `generar()`: 16 bytes de `SecureRandom` (128 bits) en Base64URL sin relleno, 22 caracteres.
   - `huella()`: SHA-256 del código. Solo la huella se guarda.
   - `coincide(String codigoRecibido, String huellaGuardada)`: compara la huella de lo recibido con `MessageDigest.isEqual`, en tiempo constante.

   `java.security` es `java.*`, así que ArchUnit lo permite en el dominio. Agrega `CodigoGestionInvalidoException extends DominioException`, con un mensaje para la persona, por ejemplo "Ese código no corresponde a este caso. Revisa el código que recibiste al publicar."
2. **Puerto en dos pasos, para que ningún PR quede en rojo:**
   - S9 **agrega** `ReportePublicado publicar(TipoReporte, SolicitudReporte)` (un record con el reporte y el código en claro, que se devuelve una sola vez), `resolver(String id, String codigo)` y `cerrar(String id, String codigo)`. Deja los tres métodos de hoy, `registrar(tipo, solicitud)`, `resolver(id)` y `cerrar(id)`, con Javadoc que diga que se van en A9. Así `ReporteController`, `corta.js` y `humo-flujo.sh`, que hoy publican y cierran sin código, siguen en verde mientras A9 no entre. `DatosDeEjemplo` pasa a `publicar` y descarta el código.
   - A9 cambia los controladores a los métodos nuevos y **borra** los tres viejos del puerto (también `registrar`: si queda, `POST /api/reportes` podría seguir publicando casos sin código). Ahí se cierra T1.
3. **Administrador.** `petfinder.codigo-admin=${PETFINDER_CODIGO_ADMIN:}` en `application.properties`; config calcula su huella al arrancar y se la pasa a `ServicioReportes`. Un código de administrador vacío **nunca** coincide (si no, un cuerpo sin código cerraría todo). Si viene con menos de 20 caracteres, la app no arranca. Nunca se escribe en el log. Esto cubre el paso de la guía (A13) sobre el primer administrador.
4. **Persistencia.** `V2__codigo_gestion.sql`: `alter table reportes add column huella_codigo varchar(64);`, que admite nulos, porque los casos viejos no tienen código y solo el administrador los cierra. `ReporteEntity` gana la columna y `RepositorioReportesH2` la copia en los dos sentidos.
5. **Voz y consola.** `ServicioComandosVoz` publica con `publicar` y `ResultadoComandoVoz` lleva el código, que la respuesta de `/api/voz` devuelve. `MenuConsola` y `EscenarioDemostracion` muestran el código al publicar y lo piden al resolver o cerrar.
6. **Variables:** agrega `PETFINDER_CODIGO_ADMIN=` (vacía) a `.env.example` y `PETFINDER_CODIGO_ADMIN: ${PETFINDER_CODIGO_ADMIN:-}` al servicio `app` de `docker-compose.yml`. Mateo la crea en Render con un valor largo (`openssl rand -base64 24` en Git Bash) y no lo pega en ningún lado más.
7. Pruebas de S9: unitarias de `CodigoGestion` y de `ServicioReportes` (sin código, código de otro caso, código correcto, administrador y administrador vacío) y una IT de persistencia que confirme que se guarda la huella y no el código. La IT HTTP del "Hecho cuando" (403, 403 y 204) entra con A9, que pone el 403 en `ManejadorErrores`.

**Cómo comprobarlo** (cuando S9 y A9 estén en main): la IT de A9 espera 403 sin código, 403 con el código de otro caso y 204 con el correcto o con el de administrador. Además:

```bash
# con la app arriba: el código no sale en la lista ni en el detalle
curl -s http://localhost:8080/api/reportes | grep -c codigo        # esperado: 0
curl -s http://localhost:8080/api/reportes/PF-001 | grep -c codigo # esperado: 0
```

**Trampas:**
- **M1 tocó `VozController`, y A9 toca los mismos controladores que M1:** trabaja sobre main con M1 ya integrado.
- `.env.example` está bloqueado para los agentes hasta que entre A3 (regla de `.claude/settings.json`): edítalo tú si tu agente no puede.
- `corta.js` y `humo-flujo.sh` cierran los casos que crean. Cuando A9 borre los métodos viejos, deben mandar `X-Codigo-Gestion` con el código que recibieron, o el despliegue queda en rojo. A9 actualiza `corta.js`; confírmale que también `humo-flujo.sh`.
- Los casos de ejemplo (`DatosDeEjemplo`) reciben un código que nadie conoce: solo el administrador los cierra. Es lo esperado.

**Pedido para tu agente:** "Implementa S9 según esa sección de la guía, con el puerto en dos pasos (agregar ahora, borrar en A9), migración V2 y pruebas unitarias e IT de persistencia; muéstrame el contrato del puerto antes de tocar voz y consola."

## S10 · Límite de peticiones

**Para qué:** DevSecOps (20): amenazas T2 (publicaciones abusivas), T5 (recolección del detalle) y T7 (agotar el presupuesto de Claude). Medido: 3000 POST con concurrencia 30 al asistente dieron 3000 respuestas 200 y ningún 429.
**Depende de:** M2 en main (consola H2 apagada antes de tocar IP). **Desbloquea:** A13 (tabla de variables).
**Hecho cuando (del plan):** Una IT espera 429 en la llamada 11 por minuto al asistente, y perf/scripts/corta.js sigue cumpliendo sus umbrales.
**Archivos:** filtro nuevo en `adaptadores/entrada/web/`, su `@Bean` en `config/`, `application.properties`, `.env.example`, `docker-compose.yml`, IT nueva en `src/test/java/petfinder/integracion/sistema/`, el `@Order` de `FiltroTamanoCuerpo.java` (M1) y el `.env` de prueba de `.github/workflows/carga-larga.yml` (M4) (ver Trampas).

**Cómo hacerlo** (**sin probar todavía**):

1. Un filtro (`OncePerRequestFilter`) con token bucket por IP y por ruta, sin dependencias nuevas: un `ConcurrentHashMap` de cubetas con tope de entradas (por ejemplo 10 000, y se limpian las que llevan más de un minuto quietas), para que miles de IP falsas no llenen la memoria.
2. La IP sale de `CF-Connecting-IP`, porque Render está detrás de Cloudflare, y si no viene, de `request.getRemoteAddr()`. **No** actives `server.forward-headers-strategy` (amenaza T9: con eso, `X-Forwarded-For: 127.0.0.1` cambia lo que ve la app).
3. Límites por minuto, configurables por variable de entorno:
   ```properties
   petfinder.limite.asistente.por-ip=${PETFINDER_LIMITE_ASISTENTE_IP:10}
   petfinder.limite.asistente.global=${PETFINDER_LIMITE_ASISTENTE_GLOBAL:60}
   petfinder.limite.voz.por-ip=${PETFINDER_LIMITE_VOZ_IP:3000}
   petfinder.limite.reportes.por-ip=${PETFINDER_LIMITE_REPORTES_IP:3000}
   ```
   El global del asistente protege el gasto aunque se falsifique la IP. `corta.js` manda 33 peticiones por segundo desde una sola IP (unas 2000 por minuto) a `/api/voz` y `/api/reportes`: por eso esos dos límites quedan por encima. Regístralo como riesgo aceptado en S11.
4. Al pasarse: 429, cabecera `Retry-After: 60` y cuerpo `{"error": "Demasiadas peticiones. Espera un minuto e intenta de nuevo."}`, con el mismo formato de `ManejadorErrores`.
5. Agrega las cuatro variables a `.env.example` y al servicio `app` de `docker-compose.yml` con `${VAR:-valor}`. Pásale los nombres a Mateo para Render.
6. Límite de gasto en la consola de Anthropic (console.anthropic.com > Settings > Limits): lo fija quien tenga la cuenta. Anota el valor en docs/seguridad.md.

**Cómo comprobarlo:**
- IT `LimitePeticionesSistemaIT` (`@SpringBootTest` con puerto aleatorio, `ANTHROPIC_API_KEY=` y una IP aleatoria en `CF-Connecting-IP`): 10 `POST /api/asistente/turno` sin 429 y el 11 con 429.
- `BASE_URL=http://localhost:8080 k6 run perf/scripts/corta.js` sale 0 con la app local.

**Trampas:**
- El límite global vive en el contexto de Spring. Si otra IT llama al asistente muchas veces en el mismo minuto y comparte contexto, tu IT se vuelve inestable. Dale propiedades propias para que tenga su contexto.
- Fuera de Cloudflare (Compose, local), `CF-Connecting-IP` se puede falsificar: por eso existe el global.
- M1 agregó `FiltroTamanoCuerpo` (64 KB, 413) como `@Component` con `@Order(Ordered.HIGHEST_PRECEDENCE)`. Los dos filtros conviven; pon el de límite primero, porque rechazar no necesita leer el cuerpo. Con el mismo orden en los dos, Spring no garantiza cuál va antes: registra el tuyo en `config/` con un `FilterRegistrationBean` y `setOrder(Ordered.HIGHEST_PRECEDENCE)`, y cambia el de M1 a `Ordered.HIGHEST_PRECEDENCE + 1` (un cambio de una línea; avísale a Mateo en el PR).
- `carga.js` (50 usuarios, unas 79 peticiones por segundo, casi 4700 por minuto a `/api/voz` desde una sola IP) sí pasa el límite por defecto de 3000. Donde corra `carga.js`, arranca la app con `PETFINDER_LIMITE_VOZ_IP=100000` y `PETFINDER_LIMITE_REPORTES_IP=100000`: en local (S8, paso 5) y en `.github/workflows/carga-larga.yml` de M4, que arma su `.env` con `printf` en el paso "Crear un .env de prueba". Agrega ahí esas dos variables en el mismo PR de S10, o la carga larga queda en rojo por los 429.

**Pedido para tu agente:** "Haz S10 según esa sección de la guía: filtro con token bucket, límites por variable, 429 con Retry-After, IT de la llamada 11 y las variables en compose; no actives forward-headers-strategy."

## S12 · Pull request bloqueado de evidencia

**Para qué:** DevSecOps (20): el enunciado exige al menos un PR que introduce a propósito un problema, que el pipeline bloquea, que se cierra sin fusionar y que se enlaza en docs/seguridad.md.
**Depende de:** S2, S5, S6, A5 y A6. **Desbloquea:** S11.
**Hecho cuando (del plan):** Secretos, SAST, SCA e Imagen quedan en rojo, GitHub muestra el merge bloqueado y el PR queda Closed, no Merged.
**Archivos (solo en esa rama, nunca en main):** `pom.xml`, `src/main/resources/application.properties`, `src/main/java/petfinder/adaptadores/entrada/web/DiagnosticoController.java`.

**Cómo hacerlo:**

1. `git switch main && git pull && git switch -c evidencia/controles-seguridad`
2. Tres problemas falsos:
   - `pom.xml`: dependencia `org.apache.commons:commons-text` versión `1.9` (CVE-2022-42889, crítica, corregida en 1.10.0). Pone en rojo **SCA** e **Imagen** (`imagen-y-escaneo`), porque queda dentro del jar. Va dentro de `<dependencies>`, **sin** `<scope>` (con `test` no entraría al jar y la imagen saldría en verde):
     ```xml
     <!-- PR de evidencia S12: dependencia vulnerable a propósito. Nunca se fusiona. -->
     <dependency>
         <groupId>org.apache.commons</groupId>
         <artifactId>commons-text</artifactId>
         <version>1.9</version>
     </dependency>
     ```
   - `application.properties`: `petfinder.pago.api-secret=` con 40 caracteres aleatorios. Nada lee esa propiedad. Pone en rojo **Secretos** (regla `generic-api-key`).
     ```bash
     echo "petfinder.pago.api-secret=$(LC_ALL=C tr -dc 'A-Za-z0-9' < /dev/urandom | head -c 40)" >> src/main/resources/application.properties
     ```
   - `DiagnosticoController`: `GET /api/diagnostico` que pasa un `@RequestParam` a `new ProcessBuilder("sh", "-c", comando)`. Pone en rojo **SAST** (`java/command-line-injection`).
3. Commit, push y PR con un título y una descripción que lo digan claro: "PR de evidencia: todo es falso a propósito (dependencia vulnerable, llave inventada y controlador inseguro). No fusionar."
4. Espera a que terminen los checks. Toma captura de la lista de checks y del merge bloqueado, y guarda el enlace del PR y de cada corrida.
5. Ciérralo sin fusionar: `gh pr close <número> --comment "Evidencia de los controles: Secretos, SAST, SCA e Imagen en rojo. Se cierra sin fusionar."`
6. Borra la rama remota: `git push origin --delete evidencia/controles-seguridad`. El PR, sus commits y sus checks siguen visibles.

**Cómo comprobarlo:** Secretos, SAST, SCA e Imagen en rojo; GitHub muestra el merge bloqueado; `gh pr view <número> --json state --jq .state` responde `CLOSED`, no `MERGED`.

**Trampas:**
- No uses llaves con forma `sk-ant-...`, `ghp_...` ni `AKIA...EXAMPLE` (ver S5).
- Para la demo en vivo de M15 abre otra rama; no reabras esta.
- Si Gitleaks quedó sin `--log-opts="HEAD"`, esta rama pone en rojo el check Secretos de todos los PR hasta que la borres.

**Pedido para tu agente:** "Prepara en `evidencia/controles-seguridad` los tres problemas falsos de la sección S12 de esta guía y el texto de la descripción del PR; el push, el PR y el cierre los hago yo."

---

# Tramo 3 · Documentar y ensayar (jue 12 a dom 15 nov)

Desde el viernes 13 solo entran documentos, con revisión y pipeline completo. Si S16 tiene código, debe entrar el jueves 12.

## S11 · docs/seguridad.md

**Para qué:** DevSecOps (20) y Documentación (10): modelo de amenazas, controles con su umbral y registro de cada hallazgo con decisión.
**Depende de:** S5, S6, A5, A6 y S12. **Desbloquea:** A13 (los riesgos aceptados pasan a la parte 9 de la guía de implementación).
**Hecho cuando (del plan):** Al menos 5 amenazas con las 5 columnas del enunciado y el código de gestión como mitigación de autenticación y autorización. Cada hallazgo de Actions y cada alerta de la pestaña Security tiene fila con decisión, y está el enlace al PR bloqueado.
**Archivos:** `docs/seguridad.md` (nuevo) y su fila en `docs/README.md` (lo creó M3).

**Cómo hacerlo:**

1. **Modelo de amenazas** sobre el diagrama de contenedores del Corte 2, con las 5 columnas del enunciado: Amenaza, STRIDE, Componente o flujo, Mitigación y Cómo se verifica. Parte del borrador T1 a T11 del Apéndice A de `docs/plan-corte-3.md` y, para cada una, pon la mitigación y la verificación **finales**, con el nombre de la prueba o el check. Mínimo 5; están las 11. T1 debe nombrar el código de gestión como autenticación (por posesión) y autorización (por caso).
2. **Controles del pipeline:** una fila por control (Secretos, SAST, SCA, Imagen), con herramienta, versión fijada, umbral, job o check que lo aplica y artefacto que publica.
3. **Registro de hallazgos:** ID, fuente (Actions, pestaña Security, auditoría manual), hallazgo, severidad y decisión, que es una de tres:
   - **Corregido**, con enlace al commit.
   - **Falso positivo**, con la justificación técnica.
   - **Riesgo aceptado**, con la justificación y lo que haría falta para corregirlo.

   Filas iniciales:

   | ID | Fuente | Hallazgo | Decisión |
   |---|---|---|---|
   | H-01 | Trivy (SCA) | Tomcat y Jackson vulnerables | Corregido en `2d624c0` |
   | H-02 | Trivy (SCA) | Guava CVE-2026-102554 (alta, solo en pruebas vía Selenium) | Corregido en el commit de A5 (forzar guava 33.7.2-jre) |
   | H-03 | Auditoría | ReDoS cuadrático en `ExtractorRegex` (100 KB = 17,4 s) | Corregido en el commit de M1 |
   | H-04 | Auditoría | Consola H2 respondía 200 en Render | Corregido en el commit de M2 |

   Ojo con H-02: el parche de A5 que pasó el gate en local no fuerza Guava (en `pom.xml` no hay ninguna línea de guava). Antes de escribir "Corregido", confirma con Antonio el commit que lo hace y enlázalo; si no entró, la fila queda abierta.

   Después, una fila por cada hallazgo de Actions y cada alerta de la pestaña Security (`gh api --paginate repos/DrearSanti/pet-finder-dyas/code-scanning/alerts --jq '.[] | [.number, .rule.id, .state] | @tsv'`), y una por cada línea de `.github/codeql/excepciones.txt`, de `.gitleaksignore` o de `.gitleaks.toml`.
4. **Evidencia:** enlace al PR de S12 (Closed) y a las corridas en rojo de S4, S5 y S6.
5. **Riesgos aceptados**, para la parte 9 de la guía: verificar identidad en T2 (exigiría SMS o correo); un solo proceso para el generador de IDs (S7); límites altos en `/api/voz` y `/api/reportes` por la prueba de carga (S10); `CF-Connecting-IP` falsificable fuera de Cloudflare; frases y contactos que llegan a Anthropic (T8); usuario de base dueño del esquema (T10); S16 si no alcanza.

**Cómo comprobarlo:**

```bash
sh scripts/contiene.sh docs/seguridad.md 'STRIDE' 'Cómo se verifica' 'código de gestión' 'Corregido' 'Falso positivo' 'Riesgo aceptado' '2d624c0' 'pull/'
```

Y a mano: al menos 5 amenazas con las 5 columnas; cada alerta abierta en Security tiene su fila.

**Pedido para tu agente:** "Escribe `docs/seguridad.md` con la estructura de la sección S11 de esta guía, a partir del Apéndice A de `docs/plan-corte-3.md`, y lista las alertas de code scanning que todavía no tienen fila."

## S13 · ADR-003: seguridad del pipeline

**Para qué:** Documentación (10): el enunciado pide como mínimo un ADR nuevo sobre la seguridad del pipeline, con herramientas y umbrales. El de despliegue (ADR-004) es de Mateo.
**Depende de:** S5, S6, A5 y A6. **Desbloquea:** nada.
**Hecho cuando (del plan):** El ADR tiene contexto, decisión, alternativas y consecuencias, y sus umbrales coinciden con los workflows.
**Archivos:** `docs/adr/ADR-003-seguridad-del-pipeline.md`.

**Cómo hacerlo:** usa el formato de ADR-002 (tabla con Estado, Fecha de registro, Decide y Relacionada con; secciones Contexto, Opciones consideradas, Decisión, Consecuencias positivas, Consecuencias negativas y Cuándo se revisaría). Contenido mínimo:
- Herramientas y umbrales, **copiados de los workflows**: Gitleaks 8.30.1 con cero secretos en el historial; CodeQL `security-extended` con `umbral-sarif.py` a `7.0` (escríbelo con punto, como en el workflow: la comprobación de abajo busca `7.0`); Trivy fs con HIGH y CRITICAL corregibles; Trivy image con CRITICAL corregible más reporte de HIGH; Dependabot como apoyo, que no reemplaza el SCA.
- Por qué SCA usa HIGH y la imagen CRITICAL: es el mínimo del enunciado para cada control, y la imagen trae paquetes del sistema base que el equipo no controla.
- Alternativas descartadas, con su razón: Semgrep (la edición comunitaria corre sin cuenta, pero no sigue datos entre archivos, y el análisis entre archivos exige cuenta y token de su plataforma), SonarCloud (cuenta y token externos), OWASP Dependency-Check (descarga lenta de la NVD y clave de API), TruffleHog (verifica llaves contra los proveedores; Gitleaks corre sin red y saca SARIF), Grype.
- Qué se sacrificó en tiempo (CodeQL es el job más lento) y por qué corre en paralelo.
- Código de gestión frente a cuentas (D1): resuelve "que solo quien publicó cierre su caso" sin registro ni contraseñas; se sacrifica que quien pierde el código depende del administrador.

**Cómo comprobarlo:** tiene contexto, decisión, alternativas y consecuencias, y cada umbral coincide con el workflow:

```bash
sh scripts/contiene.sh docs/adr/ADR-003-seguridad-del-pipeline.md '## Contexto' '## Decisión' '## Opciones consideradas' '## Consecuencias' '7.0' 'CRITICAL' 'HIGH'
grep -n 'severity\|exit-code\|7.0' .github/workflows/*.yml
```

**Pedido para tu agente:** "Escribe ADR-003 con el formato de ADR-002 y el contenido de la sección S13 de esta guía, y compara cada umbral con `.github/workflows/`."

## S14 · docs/evolucion.md

**Para qué:** Documentación (10): el recorrido de los tres cortes; qué decisiones se mantuvieron, cuáles cambiaron y por qué; cómo se resolvió cada pendiente; y los límites del sistema final.
**Depende de:** casi todo lo demás, y de la respuesta del profesor que guarda Mateo (M0). **Desbloquea:** nada.
**Hecho cuando (del plan):** Están los 6 pendientes y los 3 temas del profesor, cada uno con enlace a su PR o a su razón.
**Archivos:** `docs/evolucion.md` y su fila en `docs/README.md`.

**Cómo hacerlo:**
1. Una sección por corte: Corte 1 (dominio, Factory Method y Observer, consola; ahora en `docs/corte-1/`), Corte 2 (hexagonal, web, voz, asistente, H2 y pruebas) y Corte 3 (contenedores, CI/CD, DevSecOps, PostgreSQL y Render).
2. Decisiones que se mantuvieron (ADR-001, ADR-002) y las que cambiaron: H2 por PostgreSQL con Flyway (D3), Render desde el repo por imagen de GHCR (D2, ADR-004), sin cuentas por código de gestión (D1, ADR-003).
3. Tabla de pendientes, cada uno con enlace a su PR o a su razón:

   | Pendiente | Cómo se resolvió |
   |---|---|
   | 1 Pipeline de CI/CD con DevSecOps | A2, A6, S5, S6, M7 |
   | 2 PostgreSQL con migraciones | S7 |
   | 3 Paginación y JOIN FETCH | S8 |
   | 4 Cuentas y permisos | S9, A9, M10 (código de gestión) |
   | 5 Fotos | Fuera de este corte (D5), con su diseño futuro |
   | 6 Selenium para Cerca de ti y coincidencias | A11 |
   | Profesor: seguridad, incluido si la PWA permite meter un virus | S11, M9, A8, M14 |
   | Profesor: fotos de las mascotas | Trabajo futuro (D5), citando su respuesta |
   | Profesor: reconocimiento de parecidos con IA | Trabajo futuro (D5), citando su respuesta |
4. Límites del sistema final: una sola instancia (generador de IDs), PostgreSQL gratis de Render que vence a los 30 días, quien pierde el código depende del administrador, los riesgos aceptados de S11.

**Cómo comprobarlo:** están los 6 pendientes y los 3 temas del profesor, cada uno con enlace a su PR o a su razón:

```bash
grep -c 'pull/' docs/evolucion.md     # al menos un enlace por pendiente resuelto
```

**Pedido para tu agente:** "Arma `docs/evolucion.md` con la estructura de la sección S14 de esta guía; saca los enlaces con `gh pr list --state merged --search 'S7 OR S8 OR S9'` y deja marcado lo que falte."

## S15 · Tabla de trazabilidad ampliada en el README

**Para qué:** Arquitectura verificada (15) y Documentación (10): el enunciado pide mantener la tabla del Corte 2 en el README con dos columnas más, la etapa del pipeline que vuelve a verificar cada reto y el umbral que detiene el pipeline.
**Depende de:** A2, A6 y M7. **Desbloquea:** nada.
**Hecho cuando (del plan):** Cada fila de la tabla tiene etapa y umbral, o "sin verificación automática" con el motivo.
**Archivos:** `README.md` (después de M3 la tabla está en "Tabla de trazabilidad del Corte 2").

**Cómo hacerlo:**
1. Agrega las columnas **Etapa del pipeline** y **Umbral que detiene el pipeline** a cada fila.
2. Ejemplos, con los nombres de las filas que tiene hoy la tabla:
   - "Menú interactivo · Usabilidad y modificabilidad": la modificabilidad la vuelven a verificar `reglas-arquitectura` (cualquier violación de ArchUnit) y `pruebas-integracion` (cualquier IT en rojo); la usabilidad queda "sin verificación automática", con el motivo (se evalúa en la demo y en Safari).
   - "Menú interactivo: mapa con coordenadas", "Cerca de ti", "La tengo yo / Ya apareció" y "posibles coincidencias": `pruebas-ui` cuando A11 lo vuelva obligatorio; mientras tanto, dilo en la celda.
   - "Captura de voz · Rendimiento": `despliegue-y-verificacion` en el PR y la verificación de `cd.yml` contra Render, con p95 menor a 500 ms, errores menores a 1 % y al menos 1800 peticiones en `corta.js`.
   - "Captura de voz · Modificabilidad" y "asistente con tarjeta viva": `reglas-arquitectura` (`lasEntradasSoloHablanConPuertos` y `elAsistenteNuncaPublica`).
   - "Los dos retos · Modificabilidad y testabilidad": `reglas-arquitectura` y `pruebas-unitarias`, con cobertura de líneas del dominio de al menos 70 % (A1).
3. No borres filas ni cambies los números del Corte 2: es historia.

**Cómo comprobarlo:** cada fila tiene etapa y umbral, o "sin verificación automática" con el motivo, y los nombres coinciden con los checks del ruleset:

```bash
sh scripts/contiene.sh README.md 'Etapa del pipeline' 'Umbral que detiene el pipeline' 'sin verificación automática'
```

**Pedido para tu agente:** "Amplía la tabla de trazabilidad del README como dice la sección S15 de esta guía, con los nombres exactos de los checks de `.github/workflows/`."

## S16 · Auditoría de quién hizo qué (opcional)

**Para qué:** DevSecOps (20), amenaza T4 (repudio): hoy, tras cerrar un caso, no queda ninguna línea en el log, y un avistamiento con salto de línea inyectó una línea falsa en el log. Si no alcanza, queda como riesgo aceptado en docs/seguridad.md.
**Depende de:** S9 (la huella del código). **Desbloquea:** nada. **Fecha límite:** jue 12, porque desde el vie 13 solo entran documentos.
**Hecho cuando (del plan):** Una IT ve CASO_CERRADO con el ID, y una descripción con salto de línea no crea una línea nueva.
**Archivos:** puerto nuevo `application/port/salida/RegistroAuditoria.java`, adaptador de log en `adaptadores/salida/`, su `@Bean` en `config/`, `ServicioReportes`, `ServicioAvistamientos`, `domain/observer/AlertaPropietarioObserver.java` (saneo de CR/LF) y una IT.

**Cómo hacerlo:**
1. Puerto `RegistroAuditoria` con un método por evento: `CASO_PUBLICADO`, `CASO_RESUELTO`, `CASO_CERRADO` y `AVISTAMIENTO`. Cada uno lleva ID, hora, si actuó el dueño o el administrador y los primeros caracteres de la huella. Nunca datos de contacto.
2. El adaptador escribe con SLF4J en una sola línea y reemplaza `\r` y `\n` de cualquier texto. El dominio no puede usar SLF4J; por eso es un puerto.
3. Sanea también `AlertaPropietarioObserver` (hoy `System.out` sin sanear) y pon tope a la lista en memoria de `AuditoriaObserver`.

**Cómo comprobarlo:** una IT con `OutputCaptureExtension` cierra un caso y encuentra `CASO_CERRADO` con su ID, y un avistamiento con `"\n"` en la descripción no crea una línea nueva en la salida.

**Pedido para tu agente:** "Solo si S9 ya está en main y es jueves 12 o antes: haz S16 según esa sección de la guía; si no, agrega T4 como riesgo aceptado en docs/seguridad.md."

---

## Lo que está sin probar todavía

- Todos los workflows (`seguridad.yml` con Secretos y SAST, y los `ci.yml` y `seguridad.yml` de Antonio): pasaron el gate en local, pero **no han corrido en GitHub Actions**.
- Los pasos en la interfaz de GitHub y los comandos `gh api` de S1, S2 y S3, y lo que se configure en Render y en la consola de Anthropic.
- Flyway contra PostgreSQL (S7): el gate local corrió con H2.
- S4 y S7 por separado: el gate se corrió con los dos juntos.
- `umbral-sarif.py` contra un SARIF real de CodeQL: solo se probó con SARIF de ejemplo.
- Los nombres de los checks del ruleset (S2): salen de los workflows, pero ninguno ha corrido en GitHub.
- Gitleaks: `--log-opts="HEAD"` en Actions, la sintaxis `[allowlist]` en la v8.30.1 y `.gitleaksignore`. El SHA-256 del binario no se descargó: lo pegas tú (S5, paso 2).
- CodeQL: si `paths-ignore` excluye `src/test` en Java con `build-mode: none` (S6).
- La prueba de reinicio con `docker compose` (S7): no se ha corrido.
- Los bloques diff de S4 y S7 solo se comprobaron con `git apply --check` sobre el main de hoy y, el de S7, contra la carpeta de M2 (solo choca `application.properties`). Contra A3 no se ha probado.
- El contenido de `.env.example`: no se revisó, porque está bloqueado para los agentes.
- S8, S9, S10, S12 a S16: los pasos son el diseño propuesto, no código probado.

## Qué entregas, a quién y cuándo

| Cuándo | A quién | Qué |
|---|---|---|
| sáb 7 | Mateo | Revisión de M1, M2, M3 y M4 en menos de 24 horas |
| sáb 7 | Antonio y Mateo | S1 hecho (Actions habilitado para A2) y ruleset base de S2 |
| sáb 7 a dom 8 | Antonio (revisa) | PR de S4 y de S7 |
| dom 8 | Antonio | Job `secretos` en `seguridad.yml` (S5), sin pisar su job `sca` |
| dom 8 | Todos | Checks obligatorios en el ruleset, a medida que aparecen (S2) |
| dom 8 | Mateo | Secretos en `produccion` y paquete de GHCR público (S3), para M6 y M7 |
| dom 8 | Mateo | S7 en main: M6 puede crear PostgreSQL en Render |
| lun 9 | Antonio | Contrato del código de gestión acordado (S9 para A9) |
| mar 10 | Antonio y Mateo | S9 en main; nombre `PETFINDER_CODIGO_ADMIN` para Render (Mateo) y para M10 |
| mar 10 a mié 11 | Mateo | Nombres de las variables de límite de S10 para Render |
| mié 11 | Todos | Enlace del PR bloqueado de S12, cerrado sin fusionar |
| vie 13 | Antonio | docs/seguridad.md con los riesgos aceptados, para la parte 9 de A13 |
| vie 13 a sáb 14 | Todos | ADR-003, tabla de trazabilidad y docs/evolucion.md |
