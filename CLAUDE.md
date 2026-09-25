# Pet Finder — instrucciones para agentes

Pet Finder conecta a quien perdió una mascota con quien la vio o la encontró. Proyecto de Diseño y Arquitectura de Software, Corte 2: arquitectura hexagonal sobre Spring Boot, con menú interactivo, captura de voz, asistente con tarjeta viva, mapa y PWA. El plan completo está en `blueprints/pet-finder-corte-2/`.

## Comandos

macOS, Linux y **Git Bash** en Windows (en PowerShell cambia `./mvnw` por `.\mvnw.cmd`):

| Para qué | Comando |
|---|---|
| Compilar | `./mvnw -q -DskipTests compile` |
| App web | `./mvnw spring-boot:run` → http://localhost:8080 |
| Demo de consola (Corte 1) | `./mvnw -q compile exec:java` |
| Menú de consola | `./mvnw -q compile exec:java -Dexec.args="--menu"` |
| Unitarias + ArchUnit + slice web (`*Test`) | `./mvnw -q test` |
| Una clase de prueba | `./mvnw -q test -Dtest=Clase` |
| Integración y sistema (`*IT`) + JaCoCo | `./mvnw -q clean verify` |
| Una sola IT | `./mvnw -q verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=Clase` |
| Pruebas de UI (`*UIT`, necesita Chrome) | `./mvnw -q -Pui verify` |
| Empaquetar | `./mvnw -q -DskipTests package` → `target/pet-finder.jar` |
| Humo del jar | `sh scripts/humo.sh [/ruta ...]` |
| Humo del contenedor (Docker) | `sh scripts/humo-docker.sh` |
| Contenido de un archivo | `sh scripts/contiene.sh ARCHIVO 'debe estar' '!no debe estar'` |
| Tokens de diseño | `sh scripts/verificar-tokens.sh` |
| Carga (k6) | `k6 run perf/scripts/carga.js` |
| Consola H2 | http://localhost:8080/h2-console · `jdbc:h2:mem:petfinder` · usuario `sa`, sin clave |

**Gate de cualquier tarea:** `./mvnw -q clean verify` en verde. JDK 17 obligatorio (`./mvnw -v` debe decir `Java version: 17`). No instales Maven: el wrapper descarga el 3.9.16.

## Cómo trabajar con el blueprint

1. `git pull` de `main` y una rama nueva por tarea: `git switch -c tarea/E3-T1` (el trabajo de los demás desbloquea tus tareas cuando entra a `main`).
2. `/architect-next blueprints/pet-finder-corte-2 --list` muestra la cola completa y qué bloquea a cada tarea.
3. Toma **solo tareas de tu épica**, con su id: `/architect-next blueprints/pet-finder-corte-2 --start E2-T1` (sin `--start`, el comando entrega la primera tarea lista de cualquier épica):

| Épica | Responsable | Archivo |
|---|---|---|
| 01 Núcleo hexagonal, dominio y voz | Santi | `blueprints/pet-finder-corte-2/epics/01-nucleo-hexagonal.md` |
| 02 Adaptador web y pruebas | Antonio | `blueprints/pet-finder-corte-2/epics/02-adaptador-web-y-pruebas.md` |
| 03 Persistencia, asistente e interfaz | Mateo | `blueprints/pet-finder-corte-2/epics/03-persistencia-asistente-e-interfaz.md` |

4. Lee la tarea completa en la épica: el **contenido literal** se escribe tal cual (ya se compiló y probó).
5. `/architect-next blueprints/pet-finder-corte-2 --done <id>` corre todo el `Verify` en orden y, solo si todo sale 0, marca la tarea `done` en `tasks.json`. De ese archivo no se toca nada más.
6. **Detente y entrega el bloque `Checkpoint`**. La persona responsable hace el commit (incluye `tasks.json`), el tag y el push, y abre el Pull Request (se integra con *merge commit*, nunca *squash*). Tú nunca.

## Ruta de una petición

```
navegador (index.html + js/)            consola (Main)
      │ HTTP JSON                               │
      ▼                                         ▼
adaptadores/entrada/web  ──►  application/port/entrada  ◄──  adaptadores/entrada/consola
                                     │ implementan
                                     ▼
                         application/service  ──►  domain (Java puro)
                                     │ usan
                                     ▼
                         application/port/salida
                          │                    │
                          ▼                    ▼
     adaptadores/salida/persistencia/h2    adaptadores/salida/ia (Claude)
     (memoria: solo pruebas unitarias)     ExtractorRegex (respaldo, en application/service)
```

`config/` es el único lugar que conoce las implementaciones concretas y las conecta con `@Bean`.

## Límites (ArchUnit los hace cumplir en cada `./mvnw test`)

| Paquete | Puede importar | Nunca importa |
|---|---|---|
| `domain` | `java.*` | Spring, JPA, Anthropic, otros paquetes del proyecto |
| `application` | `domain`, `java.*` | Spring, adaptadores |
| `adaptadores.entrada.web` | puertos de entrada, `domain`, Spring Web | `application.service`, persistencia |
| `adaptadores.salida.persistencia` | puertos de salida, `domain`, JPA | web, IA |
| `adaptadores.salida.ia` | puerto `ExtractorDatosReporte`, `domain`, `com.anthropic` | web, persistencia |
| `config` | todo | — |

## Dónde vive cada cosa

- Dominio: `src/main/java/petfinder/domain/{model,factory,observer,exception}`.
- Puertos: `application/port/entrada` (GestionReportes, RegistroAvistamientos, ProcesadorComandosVoz, AsistenteReportes) y `application/port/salida` (RepositorioReportes, ExtractorDatosReporte).
- Servicios: `application/service`.
- Web: `adaptadores/entrada/web` (controladores, `ManejadorErrores`, `dto/`).
- Interfaz: `src/main/resources/static/` (`index.html`, `css/tokens.css`, `css/app.css`, `js/*.js`, `img/`, `manifest.webmanifest`, `sw.js`).
- Pruebas: `src/test/java/petfinder/` — `*Test` unitarias, `arquitectura/` ArchUnit, `integracion/**/*IT`, `ui/*UIT`.
- Carga: `perf/` · Documentación: `docs/` · Diseño: `docs/diseno/`.

## Reglas de código

- **Todo en español**: clases, métodos, variables, Javadoc y textos. Solo los sufijos que imponen las herramientas en inglés (`Controller`, `DTO`, `Entity`, `Test`, `IT`, `UIT`). No renombres código existente.
- Javadoc que explica el **porqué**, como el del Corte 1.
- Objetos de valor como `record`. Sin setters en el dominio: las transiciones de estado solo en `EstadoReporte`.
- Errores de negocio: subclases de `DominioException` con mensaje en español para la persona.
- Los servicios no llevan anotaciones de Spring; se crean con `@Bean` en `config/`.
- El asistente **nunca publica**: publicar siempre es un `POST /api/reportes` que la persona dispara.
- Pruebas: `// Arrange`, `// Act`, `// Assert`, `@DisplayName` en español, sin estado compartido, nunca un ID fijo en integración o sistema. Toda `@SpringBootTest` fija `ANTHROPIC_API_KEY=`.

## Diseño

Cualquier cambio en `static/` pasa por la skill `diseno-pet-finder`: lee `docs/diseno/DESIGN.md` y `docs/diseno/vistas.html` antes de tocar la interfaz. Colores solo con variables de `css/tokens.css` (cero hex en `app.css`); el logo nunca se redibuja.

## Entorno

- Copia `.env.example` a `.env` (nunca lo commitees). Spring lo lee solo.
- `ANTHROPIC_API_KEY` es opcional: sin clave el asistente funciona con regex.
- `PETFINDER_IA_MODELO` (por defecto `claude-sonnet-5`) y `PETFINDER_IA_TOPE_DIARIO` (por defecto `200`).
- Puerto `8080` (`PORT` lo cambia; Render lo define solo).

## Reglas por zona

| Archivo | Aplica a |
|---|---|
| `.claude/rules/nucleo.md` | `domain/**`, `application/**` |
| `.claude/rules/web.md` | `adaptadores/entrada/web/**` |
| `.claude/rules/persistencia.md` | `adaptadores/salida/persistencia/**` |
| `.claude/rules/ia.md` | `adaptadores/salida/ia/**`, `config/ConfiguracionIa.java` |
| `.claude/rules/interfaz.md` | `src/main/resources/static/**` |
| `.claude/rules/pruebas.md` | `src/test/**` |

## Innegociables

1. **El agente nunca hace `git commit`, `git tag`, `git push` ni `git reset --hard`.** La autoría individual se evalúa en el historial: cada quien commitea su trabajo.
2. Nunca leas ni imprimas `.env` ni la clave de Anthropic.
3. No cambies versiones del `pom.xml` ni agregues dependencias que la tarea no pida.
4. No toques archivos de otra épica salvo los que tu tarea lista en **Files**.
5. Si un `Verify` falla, arregla la causa; nunca edites el `Verify`, el script ni la prueba para que pase.
6. Nada se marca `done` sin que `./mvnw -q clean verify` pase.
