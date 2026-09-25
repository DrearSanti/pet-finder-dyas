# Épica 01: Núcleo hexagonal, dominio y voz

> La aplicación corre sobre Spring Boot con arquitectura hexagonal verificada por ArchUnit, el dominio sabe reconstruirse y hablar de borradores, la voz se interpreta por reglas y la decisión arquitectónica queda documentada.

| | |
|---|---|
| **Epic id** | `01-nucleo-hexagonal` |
| **Responsable** | Santiago Escobar (Santi) — solo esta persona trabaja las tareas de esta épica |
| **Tasks** | `E1-T1` … `E1-T6` (6 tareas) |
| **Depends on** | `E2-T5` (02-adaptador-web-y-pruebas), `E3-T8` (03-persistencia-asistente-e-interfaz), `E3-T9` (03-persistencia-asistente-e-interfaz) |
| **Unlocks** | `E2-T2`, `E3-T3`, `E2-T3`, `E3-T4`, `E2-T4`, `E3-T5` |
| **Parallel with** | Las otras dos épicas: cada tarea lista sus dependencias exactas; mientras estén en `done`, se trabaja en paralelo |

You do not need any other file to complete this epic. Everything below is repeated here on purpose.

---

## Stack

Java 17 · Spring Boot 4.1.1 (starters modulares) · arquitectura hexagonal · JPA/Hibernate + H2 en memoria · HTML/CSS/JS sin framework + Leaflet · Claude Sonnet 5 opcional con respaldo por reglas (regex) · Docker en Render.
Build con **Maven Wrapper**: `./mvnw` (macOS, Linux y Git Bash de Windows) o `.\mvnw.cmd` (PowerShell). No instales Maven. **JDK 17 obligatorio** (`./mvnw -v` debe decir `Java version: 17`). Las versiones de dependencias están en `pom.xml`: léelas, nunca las adivines ni las cambies.

| Task | Command |
|---|---|
| Compilar | `./mvnw -q -DskipTests compile` |
| App web | `./mvnw spring-boot:run` → http://localhost:8080 |
| Demo de consola | `./mvnw -q compile exec:java` · menú: `./mvnw -q compile exec:java -Dexec.args="--menu"` |
| Unitarias (`*Test`) | `./mvnw test` · una clase: `./mvnw -q test -Dtest=Clase` |
| Integración y sistema (`*IT`) + cobertura | `./mvnw verify` · una IT: `./mvnw -q verify -Dtest=NINGUNA -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=Clase` |
| Prueba de humo | `./mvnw -q -DskipTests package && sh scripts/humo.sh [/ruta ...]` |
| Verificar contenido | `sh scripts/contiene.sh ARCHIVO 'texto' ['!prohibido' ...]` (comillas simples) |
| Consola H2 | http://localhost:8080/h2-console · JDBC `jdbc:h2:mem:petfinder` · usuario `sa`, sin clave |

**Gate:** `./mvnw -q clean verify` passes before any task here is marked done.

Windows: todos los comandos corren igual en **Git Bash** (viene con Git para Windows). En PowerShell cambia `./mvnw` por `.\mvnw.cmd` y corre los scripts con el `sh` de Git Bash.
El proyecto no necesita ningún servicio externo para las pruebas: H2 es embebida y las pruebas nunca llaman a Claude (toda `@SpringBootTest` fija `ANTHROPIC_API_KEY=`).

## Directory subtree

Only the parts this epic touches:

```
pom.xml                                        # E1-T1 — Spring Boot 4.1.1, surefire (*Test), failsafe (*IT), JaCoCo
src/main/java/petfinder/
  PetFinderApplication.java                    # E1-T1 NEW — arranque de la app web
  Main.java                                    # E1-T1 EDIT — consola: mismo contexto de Spring, sin servidor
  config/ConfiguracionPetFinder.java           # E1-T1 NEW, E1-T5 EDIT — composition root (@Bean por pieza)
  domain/model/ReporteMascota.java             # E1-T3 EDIT — constructor de reconstrucción
  domain/model/ReportePerdida.java             # E1-T3 EDIT — reconstruir(...)
  domain/model/ReporteEncontrada.java          # E1-T3 EDIT — reconstruir(...)
  domain/model/GeneradorIdReportes.java        # E1-T3 EDIT — AtomicInteger
  domain/model/Ubicacion.java                  # E1-T4 EDIT — latitud/longitud opcionales, aproximada()
  domain/model/BorradorReporte.java            # E1-T4 NEW — la tarjeta viva
  domain/factory|observer|exception/           # exist, read-only (Factory Method y Observer del Corte 1)
  application/port/entrada/                    # E1-T2 NEW GestionReportes, RegistroAvistamientos · E1-T4 AsistenteReportes · E1-T5 ProcesadorComandosVoz
  application/port/salida/                     # E1-T2 RepositorioReportes (movido) · E1-T4 ExtractorDatosReporte
  application/service/                         # E1-T2 ServicioReportes, ServicioAvistamientos (movidos) · E1-T5 InterpreteComandoVoz, ServicioComandosVoz
  adaptadores/entrada/consola/                 # E1-T2 MenuConsola, EscenarioDemostracion (movidos)
  adaptadores/salida/persistencia/memoria/     # E1-T2 RepositorioReportesEnMemoria (movido)
src/main/resources/application.properties      # E1-T1 NEW
src/test/java/petfinder/
  application/service/                         # E1-T2 pruebas del Corte 1 (movidas)
  application/voz/                             # E1-T5 InterpreteComandoVozTest, ServicioComandosVozTest
  arquitectura/ReglasArquitecturaTest.java     # E1-T2 NEW — ArchUnit
  domain/                                      # E1-T3 EstadoReporteTest, CreadoresReporteTest, GeneradorIdReportesTest, ReconstruccionReporteTest
  domain/borrador/                             # E1-T4 UbicacionTest, BorradorReporteTest
docs/adr/ADR-001-estilo-arquitectonico.md      # E1-T6 NEW
docs/adr/ADR-002-extraccion-de-intencion.md    # E1-T6 NEW
docs/diagramas/c4.md                           # E1-T6 NEW — Corte 1 + C4 contexto, contenedores, componentes
docs/arquitectura.md                           # E1-T6 NEW — las 7 secciones del enunciado
README.md                                      # E1-T6 EDIT — tabla de trazabilidad
```

Everything outside this subtree is out of scope. `mvnw`, `mvnw.cmd`, `.mvn/wrapper/`, `scripts/*.sh`, `.gitattributes`, `.gitignore` y `.env.example` ya están en la raíz: los copió el Bootstrap desde el `workspace/` del bundle. If a task seems to require editing a file not listed here, stop and report — it means the epic boundary is wrong.

## Data model touched here

| Entity | Fields this epic adds or reads | Notes |
|---|---|---|
| `Ubicacion` (record) | `zonaOBarrio`, `referencia`, `latitud`, `longitud` | Coordenadas opcionales y juntas; rango −90..90 / −180..180 |
| `BorradorReporte` (record) | 14 campos (ver contratos) | Todo puede ser null; `camposFaltantes()` replica las reglas de los creadores |
| `ReporteMascota` y subclases | `fechaCreacion`, `estado` en reconstrucción | El estado sigue cambiando solo con `resolver()` / `cerrar()` |
| `GeneradorIdReportes` | secuencia `AtomicInteger` | Formato `PF-%03d`; en memoria, se reinicia con la app |

No hay tablas en esta épica: la persistencia es de la épica 03.

## Contracts

**Consumed** — already exists, do not rebuild:

| From | Interface | Guarantee |
|---|---|---|
| Corte 1 (`main`) | `CreadorReporte`, `CreadorReportePerdida`, `CreadorReporteEncontrada` | Factory Method; validan y lanzan `DatosInvalidosException` |
| Corte 1 (`main`) | `PublicadorAvistamientos`, `ObservadorAvistamiento` y observadores | Observer; se suscriben en `ConfiguracionPetFinder` |
| `02-adaptador-web-y-pruebas`, `03-persistencia-asistente-e-interfaz` | resultados de `E2-T5`, `E3-T8`, `E3-T9` | Solo `E1-T6` los usa, para la trazabilidad con números reales |

**Produced** — later epics depend on exactly these signatures. Changing one breaks them:

```java
// application/port/entrada — lo único que conocen los adaptadores de entrada
public interface GestionReportes {
    ReporteMascota registrar(TipoReporte tipo, SolicitudReporte solicitud);
    List<ReporteMascota> listarActivos();
    ReporteMascota consultar(String id);          // ReporteNoEncontradoException si no existe
    void resolver(String id);                     // OperacionNoPermitidaException si no está ACTIVO
    void cerrar(String id);
}
public interface RegistroAvistamientos {
    void registrar(String idReporte, Avistamiento avistamiento);
}
public interface ProcesadorComandosVoz {
    ResultadoComandoVoz procesar(String texto, Contacto contacto);
    record ResultadoComandoVoz(AccionVoz accion, String mensaje, List<ReporteMascota> reportes) {}
    enum AccionVoz { REGISTRAR_PERDIDA, REGISTRAR_ENCONTRADA, LISTAR_ACTIVOS, CONSULTAR, NO_RECONOCIDO }
}
public interface AsistenteReportes {
    ResultadoTurno procesarTurno(BorradorReporte actual, String texto);
    record ResultadoTurno(BorradorReporte borrador, List<BorradorReporte.Campo> faltantes,
                          String pregunta, String fuente) { boolean listoParaPublicar(); }
}
// application/port/salida — lo que el núcleo necesita de afuera
public interface RepositorioReportes {
    void guardar(ReporteMascota reporte);
    Optional<ReporteMascota> buscarPorId(String id);
    List<ReporteMascota> listarActivos();
}
public interface ExtractorDatosReporte {
    String nombre();                                          // "claude" o "regex"
    Optional<BorradorReporte> extraer(String texto, BorradorReporte contexto);
}
// domain/model — firmas nuevas del Corte 2
public record Ubicacion(String zonaOBarrio, String referencia, Double latitud, Double longitud) {
    public Ubicacion(String zonaOBarrio, String referencia);  // constructor del Corte 1, se conserva
    public boolean tieneCoordenadas();
    public Ubicacion aproximada();                            // coordenadas a 3 decimales (≈100 m)
}
public record BorradorReporte(TipoReporte tipo, String nombre, String especie, String raza, String color,
        String senas, String descripcionMascota, String zona, String referencia, Double latitud,
        Double longitud, String descripcion, String contactoNombre, String contactoMedio) {
    public enum Campo { TIPO, NOMBRE, ESPECIE, DESCRIPCION_MASCOTA, ZONA, DESCRIPCION, CONTACTO; String pregunta(); }
    public static BorradorReporte vacio();
    public List<Campo> camposFaltantes();
    public boolean estaCompleto();
    public BorradorReporte fusionar(BorradorReporte nuevo);
    public SolicitudReporte aSolicitud();
}
public static ReportePerdida reconstruir(String id, Ubicacion ubicacion, String descripcion, Mascota mascota,
        Contacto contactoPropietario, LocalDateTime fechaCreacion, EstadoReporte estado, List<Avistamiento> avistamientos);
public static ReporteEncontrada reconstruir(String id, Ubicacion ubicacion, String descripcion,
        String descripcionMascota, Contacto contactoReportante, LocalDateTime fechaCreacion, EstadoReporte estado);
```

Beans que deja `ConfiguracionPetFinder` (E1-T1 y E1-T5): `GeneradorIdReportes`, `Map<TipoReporte, CreadorReporte>`, `RepositorioReportes`, `AlertaPropietarioObserver`, `AuditoriaObserver`, `PublicadorAvistamientos`, `ServicioReportes` (puerto `GestionReportes`), `ServicioAvistamientos` (puerto `RegistroAvistamientos`), `InterpreteComandoVoz` y `ProcesadorComandosVoz`. `InterpreteComandoVoz` expone `Comando interpretar(String texto)` con `record Comando(ProcesadorComandosVoz.AccionVoz accion, String nombre, String especie, String zona, String descripcion, String idReporte)`; E3-T4 lo reutiliza.

## Conventions that bite in this area

- **Todo en español**: clases, métodos, variables, Javadoc y textos visibles. Solo los sufijos que imponen las herramientas van en inglés (`Controller`, `DTO`, `Entity`, `Test`, `IT`). No renombres código existente.
- **Javadoc que explica el porqué**, como el del Corte 1: una decisión y su razón, no una paráfrasis del método.
- **Objetos de valor como `record`**; entidades con estado protegido (sin setters; las transiciones solo en `EstadoReporte`).
- **Errores de negocio con subclases de `DominioException`** (`DatosInvalidosException`, `ReporteNoEncontradoException`, `OperacionNoPermitidaException`) y mensajes en español para la persona.
- **Pruebas**: `// Arrange`, `// Act`, `// Assert`, `@DisplayName` en español, nada compartido entre pruebas, nunca un ID fijo en pruebas de integración o sistema.
- **Dirección de dependencias** (ArchUnit la verifica): `domain` no importa nada de Spring, JPA, Anthropic ni de otros paquetes del proyecto; `application` no importa Spring ni adaptadores; la web solo conoce puertos; solo `adaptadores.salida.ia` importa `com.anthropic`; solo `adaptadores.salida.persistencia` importa `jakarta.persistence`.
- **Commits y push los hace la persona responsable**, nunca el agente: el agente prepara el cambio, corre el `Verify` y entrega el bloque `Checkpoint`. Un commit por tarea con el prefijo del id (`E1-T3: …`) y su tag.
- **Mueve con `git mv`, nunca con copiar y borrar**: la historia de cada archivo es la evidencia de autoría del Corte 1.
- **Los servicios y el dominio no llevan `@Service`, `@Component` ni ninguna anotación de Spring**: se construyen en `ConfiguracionPetFinder`. Por eso siguen siendo Java puro y se prueban sin framework.
- **No cambies firmas del contrato sin avisar**: Antonio y Mateo programan contra ellas en paralelo.

Full project rules: `CLAUDE.md`. Area rules: `.claude/rules/nucleo.md`, `.claude/rules/pruebas.md`. Both sit in the project root — the builder copied them there from the bundle's `workspace/` before task one.

---

## Tasks

Listed in the same order as `tasks.json`. That order is the build order — work top to bottom and do not re-rank by priority or by what looks quick. Una tarea está lista cuando todas sus dependencias (de cualquier épica) están en `done`: antes de empezar, haz `git pull` de `main` para tener el trabajo de los demás.

### `E1-T1` — Migrar el build a Spring Boot 4.1.1 con wrapper

**Depends on:** nothing · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-01-base-spring`

Pasa el proyecto de Maven plano a Spring Boot **sin mover todavía ningún paquete**: el objetivo es que el código del Corte 1 compile y pase sus 11 pruebas sobre el nuevo build, y que exista un servidor que arranca. `ConfiguracionPetFinder` reemplaza el ensamblaje manual de `Main` con un `@Bean` por pieza; los servicios y el dominio **no** llevan anotaciones de Spring. `Main` se conserva como adaptador de consola: levanta el mismo contexto sin servidor web y le pide los beans, así consola y web comparten un solo composition root. El `pom.xml` usa los starters modulares de Boot 4 (`spring-boot-starter-webmvc`, `-webmvc-test`, `-data-jpa-test`); no uses `spring-boot-starter-web` ni `TestRestTemplate` sin `@AutoConfigureTestRestTemplate`. `mvnw`, `.mvn/wrapper/` y `scripts/humo.sh` ya llegaron con el workspace; no los escribas.

**Files**
- `pom.xml`
- `src/main/java/petfinder/PetFinderApplication.java`
- `src/main/java/petfinder/config/ConfiguracionPetFinder.java`
- `src/main/java/petfinder/Main.java`
- `src/main/resources/application.properties`

**Contenido literal** — escríbelo tal cual; este código ya se compiló y probó.

**`pom.xml`** — reemplaza el archivo completo

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.1.1</version>
        <relativePath/>
    </parent>

    <groupId>petfinder</groupId>
    <artifactId>pet-finder-dyas</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>17</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <exec.mainClass>petfinder.Main</exec.mainClass>
    </properties>

    <dependencies>
        <!-- Adaptadores de entrada: API REST y página estática -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>
        <!-- Salud del servicio para el script de humo y para Render -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!-- Adaptador de salida: persistencia con JPA sobre H2 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-h2console</artifactId>
        </dependency>

        <!-- Pruebas -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.tngtech.archunit</groupId>
            <artifactId>archunit-junit5</artifactId>
            <version>1.5.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>pet-finder</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <mainClass>petfinder.PetFinderApplication</mainClass>
                </configuration>
            </plugin>
            <!-- Unitarias: *Test (mvnw test) -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
            </plugin>
            <!-- Integración y sistema: *IT (mvnw verify) -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <!-- Cobertura: reporte en target/site/jacoco/index.html al correr mvnw verify -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.15</version>
                <executions>
                    <execution>
                        <id>preparar-unitarias</id>
                        <goals><goal>prepare-agent</goal></goals>
                    </execution>
                    <execution>
                        <id>reporte-unitarias</id>
                        <phase>verify</phase>
                        <goals><goal>report</goal></goals>
                    </execution>
                </executions>
            </plugin>
            <!-- Demo de consola del Corte 1: mvnw compile exec:java -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.2.0</version>
                <configuration>
                    <mainClass>${exec.mainClass}</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**`src/main/java/petfinder/PetFinderApplication.java`** — nuevo

```java
package petfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Arranque de la aplicación web: API REST, página estática y PWA.
 *
 * Es solo el punto de entrada. El ensamblaje de las piezas vive en
 * ConfiguracionPetFinder, que cumple el papel que tenía Main en el Corte 1:
 * el único lugar donde se nombran las implementaciones concretas.
 */
@SpringBootApplication
public class PetFinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetFinderApplication.class, args);
    }
}
```

**`src/main/java/petfinder/config/ConfiguracionPetFinder.java`** — nuevo

```java
package petfinder.config;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import petfinder.application.ServicioAvistamientos;
import petfinder.application.ServicioReportes;
import petfinder.domain.factory.CreadorReporte;
import petfinder.domain.factory.CreadorReporteEncontrada;
import petfinder.domain.factory.CreadorReportePerdida;
import petfinder.domain.model.GeneradorIdReportes;
import petfinder.domain.model.TipoReporte;
import petfinder.domain.observer.AlertaPropietarioObserver;
import petfinder.domain.observer.AuditoriaObserver;
import petfinder.domain.observer.PublicadorAvistamientos;
import petfinder.domain.repository.RepositorioReportes;
import petfinder.infrastructure.persistence.RepositorioReportesEnMemoria;

/**
 * Composition root de Pet Finder: el único lugar donde se nombran las clases
 * concretas. Reemplaza el ensamblaje manual que hacía Main en el Corte 1.
 *
 * Los servicios y el dominio no llevan anotaciones de Spring: se construyen
 * aquí con new, y por eso siguen siendo Java puro y probables sin framework.
 */
@Configuration
public class ConfiguracionPetFinder {

    @Bean
    public GeneradorIdReportes generadorIdReportes() {
        return new GeneradorIdReportes();
    }

    @Bean
    public Map<TipoReporte, CreadorReporte> creadoresReporte(GeneradorIdReportes generador) {
        Map<TipoReporte, CreadorReporte> creadores = new EnumMap<>(TipoReporte.class);
        creadores.put(TipoReporte.PERDIDA, new CreadorReportePerdida(generador));
        creadores.put(TipoReporte.ENCONTRADA, new CreadorReporteEncontrada(generador));
        return creadores;
    }

    @Bean
    public RepositorioReportes repositorioReportes() {
        return new RepositorioReportesEnMemoria();
    }

    @Bean
    public AlertaPropietarioObserver alertaPropietarioObserver() {
        return new AlertaPropietarioObserver();
    }

    @Bean
    public AuditoriaObserver auditoriaObserver() {
        return new AuditoriaObserver();
    }

    @Bean
    public PublicadorAvistamientos publicadorAvistamientos(AlertaPropietarioObserver alerta,
                                                           AuditoriaObserver auditoria) {
        PublicadorAvistamientos publicador = new PublicadorAvistamientos();
        publicador.suscribir(alerta);
        publicador.suscribir(auditoria);
        return publicador;
    }

    @Bean
    public ServicioReportes servicioReportes(RepositorioReportes repositorio,
                                             Map<TipoReporte, CreadorReporte> creadores) {
        return new ServicioReportes(repositorio, creadores);
    }

    @Bean
    public ServicioAvistamientos servicioAvistamientos(RepositorioReportes repositorio,
                                                       PublicadorAvistamientos publicador) {
        return new ServicioAvistamientos(repositorio, publicador);
    }
}
```

**`src/main/java/petfinder/Main.java`** — reemplaza el archivo completo

```java
package petfinder;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import petfinder.application.ServicioAvistamientos;
import petfinder.application.ServicioReportes;
import petfinder.domain.observer.AlertaPropietarioObserver;
import petfinder.domain.observer.AuditoriaObserver;
import petfinder.domain.observer.PublicadorAvistamientos;
import petfinder.ui.EscenarioDemostracion;
import petfinder.ui.MenuConsola;

/**
 * Entrada de consola del Corte 1, conservada como un adaptador más.
 *
 * Ya no ensambla nada a mano: levanta la misma ConfiguracionPetFinder que usa
 * la web, pero sin servidor, y le pide las piezas. Así la consola y la API
 * comparten un solo composition root.
 *
 * Uso: ./mvnw -q compile exec:java  (demostración)
 *      ./mvnw -q compile exec:java -Dexec.args="--menu"  (menú interactivo)
 */
public class Main {

    public static void main(String[] args) {
        try (ConfigurableApplicationContext contexto = new SpringApplicationBuilder(PetFinderApplication.class)
                .web(WebApplicationType.NONE)
                .properties("spring.main.banner-mode=off", "logging.level.root=WARN")
                .run(args)) {

            ServicioReportes servicioReportes = contexto.getBean(ServicioReportes.class);
            ServicioAvistamientos servicioAvistamientos = contexto.getBean(ServicioAvistamientos.class);

            if (tieneBandera(args, "--menu")) {
                new MenuConsola(servicioReportes, servicioAvistamientos).ejecutar();
            } else {
                new EscenarioDemostracion(
                        servicioReportes, servicioAvistamientos,
                        contexto.getBean(PublicadorAvistamientos.class),
                        contexto.getBean(AlertaPropietarioObserver.class),
                        contexto.getBean(AuditoriaObserver.class)).ejecutar();
            }
        }
    }

    private static boolean tieneBandera(String[] args, String bandera) {
        for (String arg : args) {
            if (bandera.equals(arg)) {
                return true;
            }
        }
        return false;
    }
}
```

**`src/main/resources/application.properties`** — nuevo

```properties
spring.application.name=pet-finder
# Render asigna el puerto en PORT; en local es 8080
server.port=${PORT:8080}
# Lee .env como archivo de propiedades si existe (clave=valor). Nunca se commitea.
spring.config.import=optional:file:.env[.properties]

# Salud para el script de humo, el ping de Render y el healthcheck de Docker
management.endpoints.web.exposure.include=health

# H2 en memoria: los datos se pierden al reiniciar (límite declarado)
spring.datasource.url=jdbc:h2:mem:petfinder;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.open-in-view=false
spring.h2.console.enabled=true
```

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** `./mvnw -q clean test` runs **THE SYSTEM SHALL** exit 0 with the 11 Corte 1 tests (5 in ServicioReportesTest, 6 in ServicioAvistamientosTest) passing on Spring Boot 4.1.1.
2. **WHEN** `./mvnw -q -DskipTests package` runs **THE SYSTEM SHALL** write the executable jar `target/pet-finder.jar`.
3. **WHEN** `sh scripts/humo.sh` starts that jar **THE SYSTEM SHALL** answer `GET /actuator/health` with 200 and the script SHALL exit 0.
4. **WHEN** `./mvnw -q compile exec:java` runs **THE SYSTEM SHALL** print the console demo through `PASO 11`, with every piece taken from `ConfiguracionPetFinder` instead of manual wiring in `Main`.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
./mvnw -q clean test
./mvnw -q -DskipTests package
sh scripts/humo.sh
./mvnw -q compile exec:java | grep -q "PASO 11"
```

**Checkpoint**

```bash
git add -A && git commit -m "E1-T1: Migrar el build a Spring Boot 4.1.1 con wrapper"
git tag step-01-base-spring
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-01-base-spring`. Never invent the tag: copy the `checkpoint` field.

---

### `E1-T2` — Reorganizar en hexagonal con puertos y ArchUnit

**Depends on:** `E1-T1` · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-04-hexagonal`

Mueve los paquetes a la estructura del contrato **con `git mv`** (Git conserva la historia de cada archivo, que es la evidencia de autoría) y convierte los servicios en implementaciones de puertos de entrada. `RepositorioReportes` pasa a `application/port/salida` **sin cambiar sus métodos**. `Main` y `PetFinderApplication` se quedan en el paquete raíz. La prueba de ArchUnit convierte las reglas del estilo en algo que falla: si alguien le pone `@Entity` a un reporte, `mvnw test` se pone en rojo. Las reglas sobre paquetes que aún no existen llevan `allowEmptyShould(true)` porque ArchUnit 1.x falla por defecto cuando una regla no encuentra clases.

**Files**
- `src/main/java/petfinder/**`
- `src/test/java/petfinder/application/**`
- `src/test/java/petfinder/arquitectura/ReglasArquitecturaTest.java`

**Contenido literal** — escríbelo tal cual; este código ya se compiló y probó.

**Movimiento de paquetes — córrelo tal cual:**

```bash
# Desde la raíz del proyecto (macOS, Linux o Git Bash de Windows).
set -e
B=src/main/java/petfinder
T=src/test/java/petfinder
mkdir -p $B/application/port/entrada $B/application/port/salida $B/application/service \
         $B/adaptadores/entrada/consola $B/adaptadores/salida/persistencia/memoria \
         $T/application/service $T/arquitectura
git mv $B/domain/repository/RepositorioReportes.java $B/application/port/salida/RepositorioReportes.java
git mv $B/application/ServicioReportes.java $B/application/service/ServicioReportes.java
git mv $B/application/ServicioAvistamientos.java $B/application/service/ServicioAvistamientos.java
git mv $B/ui/MenuConsola.java $B/adaptadores/entrada/consola/MenuConsola.java
git mv $B/ui/EscenarioDemostracion.java $B/adaptadores/entrada/consola/EscenarioDemostracion.java
git mv $B/infrastructure/persistence/RepositorioReportesEnMemoria.java $B/adaptadores/salida/persistencia/memoria/RepositorioReportesEnMemoria.java
git mv $T/application/ServicioReportesTest.java $T/application/service/ServicioReportesTest.java
git mv $T/application/ServicioAvistamientosTest.java $T/application/service/ServicioAvistamientosTest.java
# Paquetes e imports (perl viene con macOS y con Git para Windows; sed -i no es portable)
perl -pi -e 's/^package petfinder\.domain\.repository;/package petfinder.application.port.salida;/; s/^package petfinder\.application;/package petfinder.application.service;/; s/^package petfinder\.ui;/package petfinder.adaptadores.entrada.consola;/; s/^package petfinder\.infrastructure\.persistence;/package petfinder.adaptadores.salida.persistencia.memoria;/; s/petfinder\.domain\.repository\.RepositorioReportes/petfinder.application.port.salida.RepositorioReportes/g; s/petfinder\.application\.Servicio/petfinder.application.service.Servicio/g; s/petfinder\.ui\./petfinder.adaptadores.entrada.consola./g; s/petfinder\.infrastructure\.persistence\./petfinder.adaptadores.salida.persistencia.memoria./g' $(git ls-files 'src/*.java')
# Los servicios implementan los puertos de entrada
perl -pi -e 's/^public class ServicioReportes \{/public class ServicioReportes implements GestionReportes {/; s/^(package petfinder\.application\.service;)$/$1\n\nimport petfinder.application.port.entrada.GestionReportes;/' $B/application/service/ServicioReportes.java
perl -pi -e 's/^public class ServicioAvistamientos \{/public class ServicioAvistamientos implements RegistroAvistamientos {/; s/^(package petfinder\.application\.service;)$/$1\n\nimport petfinder.application.port.entrada.RegistroAvistamientos;/' $B/application/service/ServicioAvistamientos.java
# Carpetas vacías que quedan del Corte 1
rmdir $B/ui $B/infrastructure/persistence $B/infrastructure $B/domain/repository
```
Si el bloque se interrumpe a la mitad, no lo repitas encima: vuelve al checkpoint anterior con `git reset --hard step-01-base-spring` y córrelo completo otra vez.

**`src/main/java/petfinder/application/port/entrada/GestionReportes.java`** — nuevo

```java
package petfinder.application.port.entrada;

import java.util.List;

import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.TipoReporte;

/**
 * Puerto de entrada del ciclo de vida de un reporte.
 *
 * Es lo único que conocen los adaptadores de entrada (consola, web, voz,
 * asistente): ninguno nombra a ServicioReportes. Así agregar una forma nueva
 * de usar el sistema no toca el caso de uso.
 */
public interface GestionReportes {

    ReporteMascota registrar(TipoReporte tipo, SolicitudReporte solicitud);

    List<ReporteMascota> listarActivos();

    ReporteMascota consultar(String id);

    void resolver(String id);

    void cerrar(String id);
}
```

**`src/main/java/petfinder/application/port/entrada/RegistroAvistamientos.java`** — nuevo

```java
package petfinder.application.port.entrada;

import petfinder.domain.model.Avistamiento;

/**
 * Puerto de entrada para registrar una pista sobre una mascota perdida.
 * Lo implementa ServicioAvistamientos, que conserva las reglas y el Observer.
 */
public interface RegistroAvistamientos {

    void registrar(String idReporte, Avistamiento avistamiento);
}
```

**`src/test/java/petfinder/arquitectura/ReglasArquitecturaTest.java`** — nuevo

```java
package petfinder.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Reglas de la arquitectura hexagonal convertidas en pruebas: si alguien
 * rompe la dirección de las dependencias, mvnw test se pone en rojo.
 *
 * Las reglas sobre paquetes que todavía no existen usan allowEmptyShould,
 * porque ArchUnit falla por defecto cuando una regla no encuentra clases.
 */
@AnalyzeClasses(packages = "petfinder", importOptions = ImportOption.DoNotIncludeTests.class)
class ReglasArquitecturaTest {

    @ArchTest
    static final ArchRule elDominioEsJavaPuro = noClasses()
            .that().resideInAPackage("petfinder.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "com.anthropic..",
                    "petfinder.application..", "petfinder.adaptadores..", "petfinder.config..");

    @ArchTest
    static final ArchRule laAplicacionNoConoceAdaptadoresNiFrameworks = noClasses()
            .that().resideInAPackage("petfinder.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "com.anthropic..",
                    "petfinder.adaptadores..", "petfinder.config..");

    @ArchTest
    static final ArchRule laWebSoloHablaConPuertos = noClasses()
            .that().resideInAPackage("petfinder.adaptadores.entrada.web..")
            .should().dependOnClassesThat().resideInAPackage("petfinder.application.service..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule soloElAdaptadorDeIaUsaElSdkDeAnthropic = noClasses()
            .that().resideOutsideOfPackage("petfinder.adaptadores.salida.ia..")
            .should().dependOnClassesThat().resideInAPackage("com.anthropic..");

    @ArchTest
    static final ArchRule soloLaPersistenciaUsaJpa = noClasses()
            .that().resideOutsideOfPackage("petfinder.adaptadores.salida.persistencia..")
            .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..");
}
```

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** `./mvnw -q clean test` runs **THE SYSTEM SHALL** exit 0 with the 11 Corte 1 tests and the 5 rules of `ReglasArquitecturaTest` passing.
2. **WHEN** ArchUnit analyses `petfinder.domain..` **THE SYSTEM SHALL** find no dependency on `org.springframework`, `jakarta.persistence`, `com.anthropic`, `petfinder.application`, `petfinder.adaptadores` or `petfinder.config`.
3. **WHEN** `ServicioReportes` and `ServicioAvistamientos` are read **THE SYSTEM SHALL** declare `implements GestionReportes` and `implements RegistroAvistamientos` from `petfinder.application.port.entrada`.
4. **WHEN** the source tree is listed **THE SYSTEM SHALL** have no `src/main/java/petfinder/ui`, `src/main/java/petfinder/infrastructure` or `src/main/java/petfinder/domain/repository` directory.
5. **WHEN** `sh scripts/humo.sh` runs after `./mvnw -q -DskipTests package` **THE SYSTEM SHALL** still answer health 200.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
./mvnw -q clean test
sh scripts/contiene.sh src/main/java/petfinder/application/service/ServicioReportes.java 'implements GestionReportes'
sh scripts/contiene.sh src/main/java/petfinder/application/service/ServicioAvistamientos.java 'implements RegistroAvistamientos'
test ! -e src/main/java/petfinder/ui && test ! -e src/main/java/petfinder/infrastructure && test ! -e src/main/java/petfinder/domain/repository
./mvnw -q -DskipTests package
sh scripts/humo.sh
```

**Checkpoint**

```bash
git add -A && git commit -m "E1-T2: Reorganizar en hexagonal con puertos y ArchUnit"
git tag step-04-hexagonal
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-04-hexagonal`. Never invent the tag: copy the `checkpoint` field.

---

### `E1-T3` — Agregar reconstrucción, IDs atómicos y pruebas de dominio

**Depends on:** `E1-T2` · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-07-dominio-reconstruccion`

Hoy `ReporteMascota` siempre nace con fecha de ahora y estado ACTIVO, así que un adaptador de persistencia no podría devolver un caso RESUELTO con su fecha original. Agrega un constructor protegido de reconstrucción y los métodos estáticos `reconstruir(...)` en cada subclase (firmas exactas abajo; E3-T3 las usa). `GeneradorIdReportes` pasa a `AtomicInteger`: con la API, dos peticiones simultáneas podían recibir el mismo `PF-00X` y una sobrescribía a la otra. Las pruebas nuevas cubren clases de equivalencia (válido, inválido) y valores límite (PF-999→PF-1000, nulo, vacío, solo espacios). Los cuatro archivos de producción se reemplazan completos.

**Files**
- `src/main/java/petfinder/domain/model/ReporteMascota.java`
- `src/main/java/petfinder/domain/model/ReportePerdida.java`
- `src/main/java/petfinder/domain/model/ReporteEncontrada.java`
- `src/main/java/petfinder/domain/model/GeneradorIdReportes.java`
- `src/test/java/petfinder/domain/*.java`

**Contenido literal** — escríbelo tal cual; este código ya se compiló y probó.

**`src/main/java/petfinder/domain/model/ReporteMascota.java`** — reemplaza el archivo completo

```java
package petfinder.domain.model;

import java.time.LocalDateTime;

import petfinder.domain.exception.OperacionNoPermitidaException;

/**
 * Base común de todo reporte: identificacion, dónde y cuándo ocurrió, y en
 * qué estado está el caso.
 *
 * Es abstracta porque un reporte "en general" no existe en el dominio; existen
 * pérdidas y hallazgos, y cada uno se resume de forma distinta. Ese resumen es
 * el único método que las subclases deben implementar.
 *
 * El estado se cambia únicamente a través de resolver() y cerrar(), nunca con
 * un setter: así ninguna clase externa puede saltarse las reglas de
 * transición.
 */
public abstract class ReporteMascota {

    private final String id;
    private final LocalDateTime fechaCreacion;
    private final Ubicacion ubicacion;
    private final String descripcion;
    private EstadoReporte estado;

    protected ReporteMascota(String id, Ubicacion ubicacion, String descripcion) {
        this(id, ubicacion, descripcion, LocalDateTime.now(), EstadoReporte.ACTIVO);
    }

    /**
     * Constructor de reconstrucción: lo usan los métodos reconstruir() de las
     * subclases cuando un adaptador de persistencia devuelve un reporte que ya
     * existía. Conserva la fecha y el estado originales en lugar de pisarlos
     * con "ahora" y ACTIVO.
     */
    protected ReporteMascota(String id, Ubicacion ubicacion, String descripcion,
                             LocalDateTime fechaCreacion, EstadoReporte estado) {
        this.id = id;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
    }

    public void resolver() {
        cambiarEstado(EstadoReporte.RESUELTO);
    }

    public void cerrar() {
        cambiarEstado(EstadoReporte.CERRADO);
    }

    /**
     * Punto único de cambio de estado. Delega la regla en el propio enum, de
     * modo que agregar un estado nuevo no obliga a tocar esta clase.
     */
    private void cambiarEstado(EstadoReporte destino) {
        if (!estado.permiteTransicionA(destino)) {
            throw new OperacionNoPermitidaException(
                    "El reporte " + id + " está en estado " + estado
                            + " y no puede pasar a " + destino);
        }
        this.estado = destino;
    }

    public boolean estaActivo() {
        return estado == EstadoReporte.ACTIVO;
    }

    /** Cada tipo de reporte decide qué información mostrar. */
    public abstract String resumen();

    public String getId() {
        return id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public EstadoReporte getEstado() {
        return estado;
    }
}
```

**`src/main/java/petfinder/domain/model/ReportePerdida.java`** — reemplaza el archivo completo

```java
package petfinder.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import petfinder.domain.exception.OperacionNoPermitidaException;

/**
 * Caso abierto por alguien que perdió a su mascota y la está buscando.
 *
 * Es el único tipo de reporte que acumula avistamientos, porque solo tiene
 * sentido reportar dónde se vio a un animal que todavía nadie ha recuperado.
 */
public class ReportePerdida extends ReporteMascota {

    private final Mascota mascota;
    private final Contacto contactoPropietario;
    private final List<Avistamiento> avistamientos = new ArrayList<>();

    public ReportePerdida(
            String id,
            Ubicacion ubicacion,
            String descripcion,
            Mascota mascota,
            Contacto contactoPropietario) {
        super(id, ubicacion, descripcion);
        this.mascota = mascota;
        this.contactoPropietario = contactoPropietario;
    }

    private ReportePerdida(String id, Ubicacion ubicacion, String descripcion, Mascota mascota,
                           Contacto contactoPropietario, LocalDateTime fechaCreacion,
                           EstadoReporte estado) {
        super(id, ubicacion, descripcion, fechaCreacion, estado);
        this.mascota = mascota;
        this.contactoPropietario = contactoPropietario;
    }

    /**
     * Devuelve a memoria un reporte que ya existía, con su fecha, su estado y
     * sus avistamientos originales. Los avistamientos se cargan directamente:
     * un caso RESUELTO conserva las pistas que recibió mientras estaba activo,
     * aunque ya no admita nuevas.
     */
    public static ReportePerdida reconstruir(String id, Ubicacion ubicacion, String descripcion,
                                             Mascota mascota, Contacto contactoPropietario,
                                             LocalDateTime fechaCreacion, EstadoReporte estado,
                                             List<Avistamiento> avistamientos) {
        ReportePerdida reporte = new ReportePerdida(id, ubicacion, descripcion, mascota,
                contactoPropietario, fechaCreacion, estado);
        reporte.avistamientos.addAll(avistamientos);
        return reporte;
    }

    /**
     * La verificación de estado se repite aquí aunque el servicio ya la haga.
     * La entidad no puede confiar en que quien la llame haya validado: si
     * mañana otro caso de uso agrega avistamientos, la regla sigue protegida.
     */
    public void agregarAvistamiento(Avistamiento avistamiento) {
        if (!estaActivo()) {
            throw new OperacionNoPermitidaException(
                    "El reporte " + getId() + " ya no admite avistamientos");
        }
        avistamientos.add(avistamiento);
    }

    /**
     * Se devuelve una vista de solo lectura para que nadie agregue
     * avistamientos por fuera del método anterior, saltándose la validación.
     */
    public List<Avistamiento> getAvistamientos() {
        return Collections.unmodifiableList(avistamientos);
    }

    public Mascota getMascota() {
        return mascota;
    }

    public Contacto getContactoPropietario() {
        return contactoPropietario;
    }

    @Override
    public String resumen() {
        return String.format(
                "[%s] PERDIDA - %s (%s, %s) | Zona: %s | Estado: %s | Avistamientos: %d",
                getId(),
                mascota.nombre(),
                mascota.especie(),
                mascota.color(),
                getUbicacion().zonaOBarrio(),
                getEstado(),
                avistamientos.size());
    }
}
```

**`src/main/java/petfinder/domain/model/ReporteEncontrada.java`** — reemplaza el archivo completo

```java
package petfinder.domain.model;

import java.time.LocalDateTime;

/**
 * Caso abierto por alguien que encontró una mascota y la tiene bajo su
 * cuidado.
 *
 * A diferencia de una pérdida, aquí no hay un objeto Mascota: quien la
 * encontró no sabe su nombre ni su raza, solo puede describir lo que ve. Por
 * eso el campo es una descripción libre y no una entidad identificada.
 */
public class ReporteEncontrada extends ReporteMascota {

    private final String descripcionMascota;
    private final Contacto contactoReportante;

    public ReporteEncontrada(
            String id,
            Ubicacion ubicacion,
            String descripcion,
            String descripcionMascota,
            Contacto contactoReportante) {
        super(id, ubicacion, descripcion);
        this.descripcionMascota = descripcionMascota;
        this.contactoReportante = contactoReportante;
    }

    private ReporteEncontrada(String id, Ubicacion ubicacion, String descripcion,
                              String descripcionMascota, Contacto contactoReportante,
                              LocalDateTime fechaCreacion, EstadoReporte estado) {
        super(id, ubicacion, descripcion, fechaCreacion, estado);
        this.descripcionMascota = descripcionMascota;
        this.contactoReportante = contactoReportante;
    }

    /** Devuelve a memoria un hallazgo que ya existía, con su fecha y su estado originales. */
    public static ReporteEncontrada reconstruir(String id, Ubicacion ubicacion, String descripcion,
                                                String descripcionMascota, Contacto contactoReportante,
                                                LocalDateTime fechaCreacion, EstadoReporte estado) {
        return new ReporteEncontrada(id, ubicacion, descripcion, descripcionMascota,
                contactoReportante, fechaCreacion, estado);
    }

    public String getDescripcionMascota() {
        return descripcionMascota;
    }

    public Contacto getContactoReportante() {
        return contactoReportante;
    }

    @Override
    public String resumen() {
        return String.format(
                "[%s] ENCONTRADA - %s | Zona: %s | Estado: %s | Contacto: %s",
                getId(),
                descripcionMascota,
                getUbicacion().zonaOBarrio(),
                getEstado(),
                contactoReportante.nombre());
    }
}
```

**`src/main/java/petfinder/domain/model/GeneradorIdReportes.java`** — reemplaza el archivo completo

```java
package petfinder.domain.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Entrega identificadores consecutivos con el formato PF-001.
 *
 * Se mantiene fuera de las entidades y del repositorio: un reporte no debería
 * inventar su propio identificador, y el repositorio solo guarda lo que ya
 * viene formado. Los creadores lo reciben en el constructor y lo consultan al
 * armar cada reporte.
 *
 * Es una clase concreta y no una interfaz porque no hay una segunda forma de
 * generar identificadores en este alcance. Crear la abstraccion por si acaso
 * seria generalidad especulativa; la inversion de dependencias se aplica
 * donde si hay una frontera real de sustitucion, que es el almacenamiento.
 */
public class GeneradorIdReportes {

    /**
     * AtomicInteger y no int: con la API, dos peticiones simultáneas podían
     * leer el mismo valor con secuencia++ y recibir el mismo PF-00X, y la
     * segunda sobrescribía a la primera en el repositorio.
     */
    private final AtomicInteger secuencia = new AtomicInteger();

    public String siguiente() {
        return String.format("PF-%03d", secuencia.incrementAndGet());
    }
}
```

**`src/test/java/petfinder/domain/EstadoReporteTest.java`** — nuevo

```java
package petfinder.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import petfinder.domain.model.EstadoReporte;

/** Matriz completa de transiciones: 3 estados de origen × 3 de destino. */
class EstadoReporteTest {

    @ParameterizedTest(name = "{0} → {1} permitido = {2}")
    @DisplayName("Solo un reporte ACTIVO puede pasar a RESUELTO o CERRADO")
    @CsvSource({
            "ACTIVO,   ACTIVO,   false",
            "ACTIVO,   RESUELTO, true",
            "ACTIVO,   CERRADO,  true",
            "RESUELTO, ACTIVO,   false",
            "RESUELTO, RESUELTO, false",
            "RESUELTO, CERRADO,  false",
            "CERRADO,  ACTIVO,   false",
            "CERRADO,  RESUELTO, false",
            "CERRADO,  CERRADO,  false"
    })
    void matrizDeTransiciones(EstadoReporte origen, EstadoReporte destino, boolean permitido) {
        // Arrange: los valores llegan de la tabla
        // Act
        boolean resultado = origen.permiteTransicionA(destino);
        // Assert
        assertEquals(permitido, resultado);
    }
}
```

**`src/test/java/petfinder/domain/CreadoresReporteTest.java`** — nuevo

```java
package petfinder.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import petfinder.domain.exception.DatosInvalidosException;
import petfinder.domain.factory.CreadorReporteEncontrada;
import petfinder.domain.factory.CreadorReportePerdida;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.GeneradorIdReportes;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.SolicitudReporte;
import petfinder.domain.model.Ubicacion;

/**
 * Clases de equivalencia de los datos obligatorios: nulo, vacío y solo
 * espacios son inválidos para cada campo que exigen los creadores.
 */
class CreadoresReporteTest {

    private final CreadorReportePerdida creadorPerdida = new CreadorReportePerdida(new GeneradorIdReportes());
    private final CreadorReporteEncontrada creadorEncontrada = new CreadorReporteEncontrada(new GeneradorIdReportes());

    static Stream<Arguments> perdidasInvalidas() {
        Mascota luna = new Mascota("Luna", "perro", "criolla", "blanca", "mancha negra");
        Contacto camila = new Contacto("Camila", "3001234567");
        Ubicacion cedritos = new Ubicacion("Cedritos", "Parque 147");
        return Stream.of(
                Arguments.of("sin zona", SolicitudReporte.paraPerdida(new Ubicacion(" ", "x"), "Se perdió", camila, luna)),
                Arguments.of("sin descripción", SolicitudReporte.paraPerdida(cedritos, "", camila, luna)),
                Arguments.of("sin medio de contacto", SolicitudReporte.paraPerdida(cedritos, "Se perdió", new Contacto("Camila", null), luna)),
                Arguments.of("sin mascota", SolicitudReporte.paraPerdida(cedritos, "Se perdió", camila, null)),
                Arguments.of("mascota sin nombre", SolicitudReporte.paraPerdida(cedritos, "Se perdió", camila, new Mascota("  ", "perro", "", "", ""))),
                Arguments.of("mascota sin especie", SolicitudReporte.paraPerdida(cedritos, "Se perdió", camila, new Mascota("Luna", null, "", "", ""))));
    }

    @ParameterizedTest(name = "pérdida {0}")
    @MethodSource("perdidasInvalidas")
    @DisplayName("Una pérdida con un dato obligatorio faltante se rechaza")
    void perdidaInvalidaSeRechaza(String caso, SolicitudReporte solicitud) {
        // Act + Assert
        assertThrows(DatosInvalidosException.class, () -> creadorPerdida.preparar(solicitud));
    }

    @ParameterizedTest(name = "descripción del animal = [{0}]")
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Un hallazgo sin descripción del animal se rechaza")
    void encontradaSinDescripcionSeRechaza(String descripcionAnimal) {
        // Arrange
        SolicitudReporte solicitud = SolicitudReporte.paraEncontrada(
                new Ubicacion("Chía", "Parque principal"), "Lo encontré solo",
                new Contacto("Lorenzi", "3007654321"), descripcionAnimal);
        // Act + Assert
        assertThrows(DatosInvalidosException.class, () -> creadorEncontrada.preparar(solicitud));
    }
}
```

**`src/test/java/petfinder/domain/GeneradorIdReportesTest.java`** — nuevo

```java
package petfinder.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import petfinder.domain.model.GeneradorIdReportes;

class GeneradorIdReportesTest {

    @Test
    @DisplayName("El primer identificador es PF-001")
    void primerIdentificador() {
        assertEquals("PF-001", new GeneradorIdReportes().siguiente());
    }

    @Test
    @DisplayName("Después de PF-999 sigue PF-1000 (valor límite del formato)")
    void pasaDeTresACuatroDigitos() {
        // Arrange
        GeneradorIdReportes generador = new GeneradorIdReportes();
        for (int i = 0; i < 999; i++) {
            generador.siguiente();
        }
        // Act
        String siguiente = generador.siguiente();
        // Assert
        assertEquals("PF-1000", siguiente);
    }

    @Test
    @DisplayName("1000 llamadas desde 8 hilos no repiten ningún identificador")
    void esSeguroEntreHilos() throws InterruptedException {
        // Arrange
        GeneradorIdReportes generador = new GeneradorIdReportes();
        Set<String> vistos = ConcurrentHashMap.newKeySet();
        ExecutorService hilos = Executors.newFixedThreadPool(8);
        // Act
        for (int i = 0; i < 1000; i++) {
            hilos.submit(() -> vistos.add(generador.siguiente()));
        }
        hilos.shutdown();
        hilos.awaitTermination(10, TimeUnit.SECONDS);
        // Assert
        assertEquals(1000, vistos.size());
    }
}
```

**`src/test/java/petfinder/domain/ReconstruccionReporteTest.java`** — nuevo

```java
package petfinder.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import petfinder.domain.exception.OperacionNoPermitidaException;
import petfinder.domain.model.Avistamiento;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteEncontrada;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.Ubicacion;

class ReconstruccionReporteTest {

    private static final LocalDateTime AYER = LocalDateTime.of(2026, 9, 23, 16, 30);

    @Test
    @DisplayName("Reconstruir una pérdida conserva fecha, estado y avistamientos")
    void reconstruirPerdidaConservaTodo() {
        // Arrange
        Avistamiento pista = new Avistamiento("AV-1", AYER, new Ubicacion("Cedritos", "Parque 147"), "La vi", null);
        // Act
        ReportePerdida reporte = ReportePerdida.reconstruir("PF-007", new Ubicacion("Cedritos", "Calle 140"),
                "Se escapó", new Mascota("Luna", "perro", "criolla", "blanca", "mancha negra"),
                new Contacto("Camila", "3001234567"), AYER, EstadoReporte.RESUELTO, List.of(pista));
        // Assert
        assertEquals(AYER, reporte.getFechaCreacion());
        assertEquals(EstadoReporte.RESUELTO, reporte.getEstado());
        assertEquals(1, reporte.getAvistamientos().size());
    }

    @Test
    @DisplayName("Una pérdida reconstruida como RESUELTO no admite avistamientos nuevos")
    void perdidaResueltaReconstruidaNoAdmiteAvistamientos() {
        // Arrange
        ReportePerdida reporte = ReportePerdida.reconstruir("PF-008", new Ubicacion("Suba", "Portal"), "Se escapó",
                new Mascota("Max", "perro", "", "", ""), new Contacto("Ana", "ana@correo.co"),
                AYER, EstadoReporte.RESUELTO, List.of());
        Avistamiento nueva = new Avistamiento("AV-2", AYER, new Ubicacion("Suba", "Parque"), "Lo vi", null);
        // Act + Assert
        assertThrows(OperacionNoPermitidaException.class, () -> reporte.agregarAvistamiento(nueva));
    }

    @Test
    @DisplayName("Reconstruir un hallazgo conserva fecha y estado")
    void reconstruirEncontradaConservaFechaYEstado() {
        // Act
        ReporteEncontrada reporte = ReporteEncontrada.reconstruir("PF-009", new Ubicacion("Chía", "Parque"),
                "Estaba sola", "Gata gris", new Contacto("Lorenzi", "3007654321"), AYER, EstadoReporte.CERRADO);
        // Assert
        assertEquals(AYER, reporte.getFechaCreacion());
        assertEquals(EstadoReporte.CERRADO, reporte.getEstado());
    }
}
```

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** `ReportePerdida.reconstruir(...)` receives a date, a RESUELTO state and a list of sightings **THE SYSTEM SHALL** return a report with that same date, state and sightings.
2. **WHEN** a reconstructed RESUELTO report receives a new sighting **THE SYSTEM SHALL** throw `OperacionNoPermitidaException`.
3. **WHEN** 8 threads request 1000 identifiers from one `GeneradorIdReportes` **THE SYSTEM SHALL** return 1000 distinct values, and after the 999th THE SYSTEM SHALL return `PF-1000`.
4. **WHEN** each of the 9 origin→destination state pairs is evaluated **THE SYSTEM SHALL** allow only ACTIVO→RESUELTO and ACTIVO→CERRADO.
5. **WHEN** a lost-pet request lacks zone, description, contact medium, pet, pet name or species, or a found-pet request has a null, empty or blank animal description **THE SYSTEM SHALL** throw `DatosInvalidosException`.
6. **WHEN** `./mvnw -q clean test` runs **THE SYSTEM SHALL** exit 0 with every earlier test still passing.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
./mvnw -q test -Dtest='EstadoReporteTest,CreadoresReporteTest,GeneradorIdReportesTest,ReconstruccionReporteTest'
./mvnw -q clean test
```

**Checkpoint**

```bash
git add -A && git commit -m "E1-T3: Agregar reconstrucción, IDs atómicos y pruebas de dominio"
git tag step-07-dominio-reconstruccion
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-07-dominio-reconstruccion`. Never invent the tag: copy the `checkpoint` field.

---

### `E1-T4` — Definir coordenadas, borrador y puertos del asistente

**Depends on:** `E1-T2` · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-08-contrato-asistente`

Este es el contrato que desbloquea a Antonio (DTO con coordenadas) y a Mateo (H2 y asistente), así que se hace temprano. `Ubicacion` gana `latitud` y `longitud` **opcionales y juntas**, con un segundo constructor de dos argumentos para que nada del Corte 1 cambie. `BorradorReporte` es la tarjeta viva: todo puede venir vacío y `camposFaltantes()` aplica las **mismas** reglas que los creadores del Factory Method, así voz, asistente y formulario terminan en la misma validación. Los puertos son sin estado: el cliente manda el borrador y la frase, y recibe el borrador actualizado.

**Files**
- `src/main/java/petfinder/domain/model/Ubicacion.java`
- `src/main/java/petfinder/domain/model/BorradorReporte.java`
- `src/main/java/petfinder/application/port/entrada/AsistenteReportes.java`
- `src/main/java/petfinder/application/port/salida/ExtractorDatosReporte.java`
- `src/test/java/petfinder/domain/borrador/*.java`

**Contenido literal** — escríbelo tal cual; este código ya se compiló y probó.

**`src/main/java/petfinder/domain/model/Ubicacion.java`** — reemplaza el archivo completo

```java
package petfinder.domain.model;

import petfinder.domain.exception.DatosInvalidosException;

/**
 * Lugar del hecho: zona o barrio, una referencia cercana y, si la persona lo
 * marcó en el mapa, sus coordenadas.
 *
 * Las coordenadas son opcionales y van juntas: un reporte por voz puede no
 * tenerlas, y un punto con latitud pero sin longitud no existe.
 */
public record Ubicacion(String zonaOBarrio, String referencia, Double latitud, Double longitud) {

    public Ubicacion {
        if ((latitud == null) != (longitud == null)) {
            throw new DatosInvalidosException("La ubicación necesita latitud y longitud juntas, o ninguna");
        }
        if (latitud != null && (latitud < -90 || latitud > 90)) {
            throw new DatosInvalidosException("La latitud debe estar entre -90 y 90");
        }
        if (longitud != null && (longitud < -180 || longitud > 180)) {
            throw new DatosInvalidosException("La longitud debe estar entre -180 y 180");
        }
    }

    /** Ubicación solo con texto, como en el Corte 1. */
    public Ubicacion(String zonaOBarrio, String referencia) {
        this(zonaOBarrio, referencia, null, null);
    }

    public boolean tieneCoordenadas() {
        return latitud != null;
    }

    /**
     * Versión que se puede mostrar en público: coordenadas redondeadas a tres
     * decimales (unos 100 m), para no exponer la dirección exacta de nadie.
     */
    public Ubicacion aproximada() {
        if (!tieneCoordenadas()) {
            return this;
        }
        return new Ubicacion(zonaOBarrio, referencia, redondear(latitud), redondear(longitud));
    }

    private static double redondear(double valor) {
        return Math.round(valor * 1000d) / 1000d;
    }
}
```

**`src/main/java/petfinder/domain/model/BorradorReporte.java`** — nuevo

```java
package petfinder.domain.model;

import java.util.ArrayList;
import java.util.List;

import petfinder.domain.exception.DatosInvalidosException;

/**
 * Lo que la persona lleva dicho o escrito antes de publicar: la "tarjeta viva".
 *
 * Todos los campos pueden estar vacíos. camposFaltantes() aplica las mismas
 * reglas que exigen los creadores del Factory Method, así la voz, el
 * asistente y el formulario terminan en la misma validación y ninguno se la
 * salta.
 */
public record BorradorReporte(
        TipoReporte tipo,
        String nombre,
        String especie,
        String raza,
        String color,
        String senas,
        String descripcionMascota,
        String zona,
        String referencia,
        Double latitud,
        Double longitud,
        String descripcion,
        String contactoNombre,
        String contactoMedio) {

    /** Datos que el creador exige, en el orden en que el asistente los pregunta. */
    public enum Campo {
        TIPO("¿Perdiste una mascota o encontraste una?"),
        NOMBRE("¿Cómo se llama tu mascota?"),
        ESPECIE("¿Es perro, gato u otro animal?"),
        DESCRIPCION_MASCOTA("¿Cómo es el animal que encontraste?"),
        ZONA("¿En qué barrio o zona pasó?"),
        DESCRIPCION("Cuéntame en una frase qué pasó."),
        CONTACTO("¿A qué número o correo te pueden escribir?");

        private final String pregunta;

        Campo(String pregunta) {
            this.pregunta = pregunta;
        }

        public String pregunta() {
            return pregunta;
        }
    }

    public static BorradorReporte vacio() {
        return new BorradorReporte(null, null, null, null, null, null, null,
                null, null, null, null, null, null, null);
    }

    public List<Campo> camposFaltantes() {
        List<Campo> faltantes = new ArrayList<>();
        if (tipo == null) {
            faltantes.add(Campo.TIPO);
        } else if (tipo == TipoReporte.PERDIDA) {
            if (vacio(nombre)) {
                faltantes.add(Campo.NOMBRE);
            }
            if (vacio(especie)) {
                faltantes.add(Campo.ESPECIE);
            }
        } else if (vacio(descripcionMascota)) {
            faltantes.add(Campo.DESCRIPCION_MASCOTA);
        }
        if (vacio(zona)) {
            faltantes.add(Campo.ZONA);
        }
        if (vacio(descripcion)) {
            faltantes.add(Campo.DESCRIPCION);
        }
        if (vacio(contactoMedio)) {
            faltantes.add(Campo.CONTACTO);
        }
        return List.copyOf(faltantes);
    }

    public boolean estaCompleto() {
        return camposFaltantes().isEmpty();
    }

    /** Lo nuevo gana solo donde trae un valor; lo que ya se había dicho se conserva. */
    public BorradorReporte fusionar(BorradorReporte nuevo) {
        if (nuevo == null) {
            return this;
        }
        return new BorradorReporte(
                nuevo.tipo != null ? nuevo.tipo : tipo,
                elegir(nuevo.nombre, nombre),
                elegir(nuevo.especie, especie),
                elegir(nuevo.raza, raza),
                elegir(nuevo.color, color),
                elegir(nuevo.senas, senas),
                elegir(nuevo.descripcionMascota, descripcionMascota),
                elegir(nuevo.zona, zona),
                elegir(nuevo.referencia, referencia),
                nuevo.latitud != null ? nuevo.latitud : latitud,
                nuevo.longitud != null ? nuevo.longitud : longitud,
                elegir(nuevo.descripcion, descripcion),
                elegir(nuevo.contactoNombre, contactoNombre),
                elegir(nuevo.contactoMedio, contactoMedio));
    }

    /** Convierte el borrador en la misma SolicitudReporte que arma el formulario. */
    public SolicitudReporte aSolicitud() {
        if (tipo == null) {
            throw new DatosInvalidosException("Falta indicar si la mascota se perdió o se encontró");
        }
        Ubicacion ubicacion = new Ubicacion(zona, referencia, latitud, longitud);
        Contacto contacto = new Contacto(contactoNombre, contactoMedio);
        if (tipo == TipoReporte.PERDIDA) {
            return SolicitudReporte.paraPerdida(ubicacion, descripcion, contacto,
                    new Mascota(nombre, especie, raza, color, senas));
        }
        return SolicitudReporte.paraEncontrada(ubicacion, descripcion, contacto, descripcionMascota);
    }

    private static String elegir(String nuevo, String actual) {
        return vacio(nuevo) ? actual : nuevo;
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
```

**`src/main/java/petfinder/application/port/entrada/AsistenteReportes.java`** — nuevo

```java
package petfinder.application.port.entrada;

import java.util.List;

import petfinder.domain.model.BorradorReporte;

/**
 * Puerto de entrada del asistente: un turno de conversación sobre la tarjeta
 * viva. Es sin estado: el cliente envía el borrador que tiene y la frase
 * nueva, y recibe el borrador actualizado, lo que falta y la siguiente
 * pregunta. Publicar sigue siendo GestionReportes.registrar().
 */
public interface AsistenteReportes {

    ResultadoTurno procesarTurno(BorradorReporte actual, String texto);

    /**
     * @param fuente de dónde salieron los datos de este turno: "claude" o "regex"
     */
    record ResultadoTurno(BorradorReporte borrador,
                          List<BorradorReporte.Campo> faltantes,
                          String pregunta,
                          String fuente) {

        public boolean listoParaPublicar() {
            return faltantes.isEmpty();
        }
    }
}
```

**`src/main/java/petfinder/application/port/salida/ExtractorDatosReporte.java`** — nuevo

```java
package petfinder.application.port.salida;

import java.util.Optional;

import petfinder.domain.model.BorradorReporte;

/**
 * Puerto de salida que convierte una frase en datos de un reporte. Tiene dos
 * adaptadores intercambiables: uno con expresiones regulares, sin red, y uno
 * con Claude. El servicio los prueba en orden y usa el primero que responda.
 */
public interface ExtractorDatosReporte {

    /** Nombre corto que se muestra como fuente del turno: "claude" o "regex". */
    String nombre();

    /**
     * Devuelve solo lo que la frase menciona; lo demás queda en null. El
     * contexto es el borrador actual, para interpretar respuestas cortas como
     * un número de teléfono. Vacío si este extractor no pudo procesarla.
     */
    Optional<BorradorReporte> extraer(String texto, BorradorReporte contexto);
}
```

**`src/test/java/petfinder/domain/borrador/UbicacionTest.java`** — nuevo

```java
package petfinder.domain.borrador;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import petfinder.domain.exception.DatosInvalidosException;
import petfinder.domain.model.Ubicacion;

class UbicacionTest {

    @ParameterizedTest(name = "lat {0}, lon {1} es válida")
    @DisplayName("Los extremos del rango de coordenadas son válidos")
    @CsvSource({"-90, -180", "90, 180", "0, 0", "4.7235, -74.0417"})
    void limitesValidos(double latitud, double longitud) {
        assertDoesNotThrow(() -> new Ubicacion("Cedritos", null, latitud, longitud));
    }

    @ParameterizedTest(name = "lat {0}, lon {1} se rechaza")
    @DisplayName("Un paso fuera del rango se rechaza")
    @CsvSource({"-90.0001, 0", "90.0001, 0", "0, -180.0001", "0, 180.0001"})
    void fueraDeRango(double latitud, double longitud) {
        assertThrows(DatosInvalidosException.class, () -> new Ubicacion("Cedritos", null, latitud, longitud));
    }

    @Test
    @DisplayName("Latitud sin longitud se rechaza")
    void coordenadaIncompleta() {
        assertThrows(DatosInvalidosException.class, () -> new Ubicacion("Cedritos", null, 4.7, null));
    }

    @Test
    @DisplayName("La versión pública redondea a tres decimales")
    void aproximadaRedondea() {
        // Arrange
        Ubicacion exacta = new Ubicacion("Cedritos", "Calle 140", 4.723581, -74.041749);
        // Act
        Ubicacion publica = exacta.aproximada();
        // Assert
        assertEquals(4.724, publica.latitud());
        assertEquals(-74.042, publica.longitud());
    }
}
```

**`src/test/java/petfinder/domain/borrador/BorradorReporteTest.java`** — nuevo

```java
package petfinder.domain.borrador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import petfinder.domain.factory.CreadorReportePerdida;
import petfinder.domain.model.BorradorReporte;
import petfinder.domain.model.BorradorReporte.Campo;
import petfinder.domain.model.GeneradorIdReportes;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.TipoReporte;

class BorradorReporteTest {

    private static BorradorReporte perdidaCompleta() {
        return new BorradorReporte(TipoReporte.PERDIDA, "Luna", "perro", null, "blanca", "mancha negra",
                null, "Cedritos", null, 4.7235, -74.0417, "Se escapó en la tarde", "Camila", "3001234567");
    }

    @Test
    @DisplayName("Un borrador vacío pide primero el tipo de reporte")
    void vacioPideTipo() {
        assertEquals(List.of(Campo.TIPO, Campo.ZONA, Campo.DESCRIPCION, Campo.CONTACTO),
                BorradorReporte.vacio().camposFaltantes());
    }

    @Test
    @DisplayName("Una pérdida sin nombre ni especie los pide en ese orden")
    void perdidaPideNombreYEspecie() {
        // Arrange
        BorradorReporte borrador = BorradorReporte.vacio().fusionar(
                new BorradorReporte(TipoReporte.PERDIDA, null, null, null, null, null, null,
                        "Cedritos", null, null, null, "Se escapó", null, "3001234567"));
        // Act + Assert
        assertEquals(List.of(Campo.NOMBRE, Campo.ESPECIE), borrador.camposFaltantes());
    }

    @Test
    @DisplayName("Fusionar conserva lo dicho antes y agrega lo nuevo")
    void fusionarConservaYAgrega() {
        // Arrange
        BorradorReporte antes = BorradorReporte.vacio().fusionar(
                new BorradorReporte(TipoReporte.PERDIDA, "Luna", null, null, null, null, null,
                        null, null, null, null, null, null, null));
        BorradorReporte nuevo = new BorradorReporte(null, null, "perro", null, null, null, null,
                "Cedritos", null, null, null, null, null, null);
        // Act
        BorradorReporte despues = antes.fusionar(nuevo);
        // Assert
        assertEquals("Luna", despues.nombre());
        assertEquals("perro", despues.especie());
        assertEquals(TipoReporte.PERDIDA, despues.tipo());
    }

    @Test
    @DisplayName("Un borrador completo produce una solicitud que el Factory Method acepta")
    void completoPasaLaValidacionDelCreador() {
        // Arrange
        BorradorReporte borrador = perdidaCompleta();
        CreadorReportePerdida creador = new CreadorReportePerdida(new GeneradorIdReportes());
        // Act
        var reporte = creador.preparar(borrador.aSolicitud());
        // Assert
        assertTrue(borrador.estaCompleto());
        assertInstanceOf(ReportePerdida.class, reporte);
    }
}
```

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** coordinates are exactly ±90 latitude and ±180 longitude **THE SYSTEM SHALL** accept them, and WHEN either exceeds its range by 0.0001 THE SYSTEM SHALL throw `DatosInvalidosException`.
2. **WHEN** only one of latitude and longitude is given **THE SYSTEM SHALL** throw `DatosInvalidosException`.
3. **WHEN** `aproximada()` is applied to 4.723581, -74.041749 **THE SYSTEM SHALL** return 4.724, -74.042.
4. **WHEN** an empty `BorradorReporte` is asked what is missing **THE SYSTEM SHALL** answer TIPO, ZONA, DESCRIPCION, CONTACTO in that order.
5. **WHEN** a complete lost-pet draft is converted with `aSolicitud()` **THE SYSTEM SHALL** produce a request that `CreadorReportePerdida` accepts.
6. **WHEN** `./mvnw -q clean test` runs **THE SYSTEM SHALL** exit 0, including every test that still uses the Corte 1 constructor `new Ubicacion(zona, referencia)`.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
./mvnw -q test -Dtest='UbicacionTest,BorradorReporteTest'
./mvnw -q clean test
```

**Checkpoint**

```bash
git add -A && git commit -m "E1-T4: Definir coordenadas, borrador y puertos del asistente"
git tag step-08-contrato-asistente
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-08-contrato-asistente`. Never invent the tag: copy the `checkpoint` field.

---

### `E1-T5` — Construir el intérprete y el servicio de comandos de voz

**Depends on:** `E1-T2` · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-09-voz-regex`

Dos clases por SRP: una entiende la frase, la otra ejecuta lo entendido. **`InterpreteComandoVoz`** es Java puro sin dependencias: normaliza (minúsculas, sin tildes con `Normalizer.NFD` + quitar marcas, sin `.,¡!¿?`, espacios colapsados), rechaza nulo/vacío/>200 caracteres y reconoce con expresiones regulares `^(?:se me )?perdi(?:o)? (?:a )?(?:mi |un |una )?(?<especie>[a-zñ]+) (?:llamad[oa]|que se llama) (?<nombre>[a-zñ]+) en (?<zona>.+)$`, `^encontre (?:un |una )(?<descripcion>.+) en (?<zona>.+)$`, `^(?:ver|listar|mostrar)(?: los)? reportes(?: activos)?$` y `^consultar (?:el )?reporte pf[ -]?(?<numero>\d{1,4})$` (el número se rellena a tres dígitos: `PF-001`). Normaliza la especie (`perr*`→`perro`, `gat*`→`gato`) y capitaliza el nombre y la zona. Devuelve un `InterpreteComandoVoz.Comando` (record anidado). **`ProcesadorComandosVoz`** es el puerto de entrada del plan de Santi con sus tipos anidados: `ResultadoComandoVoz procesar(String texto, Contacto contacto)`, `record ResultadoComandoVoz(AccionVoz accion, String mensaje, List<ReporteMascota> reportes)` y `enum AccionVoz { REGISTRAR_PERDIDA, REGISTRAR_ENCONTRADA, LISTAR_ACTIVOS, CONSULTAR, NO_RECONOCIDO }`. **`ServicioComandosVoz`** lo implementa sobre `GestionReportes`: una pérdida arma `SolicitudReporte.paraPerdida` con la frase completa como descripción, `new Ubicacion(zona, "Reportado por voz")` y `"No indicado"` en raza, color y señas; una no reconocida devuelve el mensaje `No entendí. Prueba con: perdí un perro llamado Max en Chía` sin llamar a nadie. Registra en `ConfiguracionPetFinder` los beans `InterpreteComandoVoz` y `ProcesadorComandosVoz` (sin anotar las clases). Pruebas nuevas en `src/test/java/petfinder/application/voz/`: `InterpreteComandoVozTest.java` (criterios 1–3, con `@ParameterizedTest` + `@ValueSource`/`@CsvSource`) y `ServicioComandosVozTest.java` (criterios 4–5, Mockito con `@Mock GestionReportes` y `ArgumentCaptor`).

**Files**
- `src/main/java/petfinder/application/port/entrada/ProcesadorComandosVoz.java`
- `src/main/java/petfinder/application/service/InterpreteComandoVoz.java`
- `src/main/java/petfinder/application/service/ServicioComandosVoz.java`
- `src/main/java/petfinder/config/ConfiguracionPetFinder.java`
- `src/test/java/petfinder/application/voz/*.java`

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** the interpreter reads `perdí un perro llamado Max en Chía`, `Perdi una gata llamada Nala en Cedritos` or `PERDÍ UN PERRO LLAMADO MAX EN CHÍA` **THE SYSTEM SHALL** return REGISTRAR_PERDIDA with especie, nombre and zona.
2. **WHEN** it reads `encontré una gata blanca en Cajicá` **THE SYSTEM SHALL** return REGISTRAR_ENCONTRADA; `ver reportes`, `listar reportes` and `mostrar reportes` SHALL return LISTAR_ACTIVOS; `consultar reporte PF-001`, `consultar reporte pf 1` and `consultar reporte pf001` SHALL return CONSULTAR with id `PF-001`.
3. **WHEN** the text is null, empty, only spaces, longer than 200 characters, lacks species or zone, or is `cómprame un perro` **THE SYSTEM SHALL** return NO_RECONOCIDO, and WHEN a valid phrase is exactly 200 characters THE SYSTEM SHALL still recognize it.
4. **WHEN** `ServicioComandosVoz` receives a lost-pet phrase **THE SYSTEM SHALL** call `GestionReportes.registrar(PERDIDA, …)` with the dictated name, species and zone, verified with `ArgumentCaptor`.
5. **WHEN** it receives an unrecognized phrase **THE SYSTEM SHALL** never call `registrar`, and for `ver reportes` THE SYSTEM SHALL return exactly what `listarActivos()` returned.
6. **WHEN** the app boots **THE SYSTEM SHALL** expose `ProcesadorComandosVoz` and `InterpreteComandoVoz` beans, and `sh scripts/humo.sh` SHALL exit 0.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
./mvnw -q test -Dtest='InterpreteComandoVozTest,ServicioComandosVozTest'
./mvnw -q clean test
./mvnw -q -DskipTests package
sh scripts/humo.sh
```

**Checkpoint**

```bash
git add -A && git commit -m "E1-T5: Construir el intérprete y el servicio de comandos de voz"
git tag step-09-voz-regex
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-09-voz-regex`. Never invent the tag: copy the `checkpoint` field.

---

### `E1-T6` — Escribir ADR, diagramas C4, arquitectura y trazabilidad

**Depends on:** `E2-T5`, `E3-T8`, `E3-T9` · **Priority:** p0 — metadata for scope cuts, not a running order · **Checkpoint:** `step-21-documentacion`

Cierra la entrega con evidencia real, no con declaraciones. ADR-001: contexto (los dos retos piden sumar entradas sin tocar el negocio), matriz de estilos (capas, MVC, hexagonal, microservicios como descartado) contra criterios que salen de los retos, decisión y consecuencias positivas (modificabilidad, testabilidad) y negativas (más interfaces, `ConfiguracionPetFinder` crece, las entidades JPA duplican la forma del dominio). ADR-002 registra Laya como opción descartada con los números de la prueba (intención 60 %, confirmación 84 %, p95 812 ms en CPU; mínimos 85 % y 90 %); el guion no se guarda en el repo. Los diagramas usan nombres que existen en `src/`. `docs/arquitectura.md` con las 7 secciones del enunciado (§3 de este blueprint las lista). La tabla de trazabilidad del README junta las filas de Mateo y Antonio con números: pruebas que pasan, códigos HTTP, p95 de k6, cobertura de JaCoCo, y declara lo que no se logró (voz solo en Chrome/Edge y con internet, H2 en memoria, sin paginación, sin cuentas). El enunciado solo permite funcionalidad nueva si un reto la exige: el mapa con coordenadas se justifica por el menú interactivo y el asistente con tarjeta viva por la captura de voz, y cada uno lleva la nota `Funcionalidad agregada porque el reto la exige`. Como evidencia de modificabilidad, cita el tag `step-15-extractor-claude`: el extractor de Claude entró sin tocar `domain/` (`git diff step-14-h2-integrado step-15-extractor-claude --stat -- src/main/java/petfinder/domain` sale vacío).

**Files**
- `docs/adr/ADR-001-estilo-arquitectonico.md`
- `docs/adr/ADR-002-extraccion-de-intencion.md`
- `docs/diagramas/c4.md`
- `docs/arquitectura.md`
- `README.md`

**Acceptance**

Copied verbatim from this task's `acceptance` array in `tasks.json`. Each one is decidable by a command below, on this machine, during the build.

1. **WHEN** ADR-001 is read **THE SYSTEM SHALL** compare layers, MVC, hexagonal and microservices against criteria taken from the two challenges and list positive and negative consequences.
2. **WHEN** ADR-002 is read **THE SYSTEM SHALL** record the Laya test (intent 60 %, confirmation 84 %, p95 812 ms on CPU), why it was rejected and the Claude + regex decision.
3. **WHEN** `docs/diagramas/c4.md` is read **THE SYSTEM SHALL** contain the Corte 1 diagram and Mermaid `C4Context`, `C4Container` and `C4Component` diagrams.
4. **WHEN** `docs/arquitectura.md` is read **THE SYSTEM SHALL** contain the 7 numbered sections required by the assignment.
5. **WHEN** the README traceability table is read **THE SYSTEM SHALL** use the rubric columns (Reto, Atributo de calidad, Decisión arquitectónica, Dónde está, Prueba que lo evidencia, Resultado) with one row per challenge and a numeric result, mark the map with coordinates and the assistant with `Funcionalidad agregada porque el reto la exige` naming the challenge that requires each, and state the known limits, with no `__` left.
6. **WHEN** `./mvnw -q clean verify` runs on the final tree **THE SYSTEM SHALL** exit 0.

**Verify** — every command, in order, run from the project root. Each one exits 0 when this task is correct; the last one exiting 0 is what makes the task done.

```bash
sh scripts/contiene.sh docs/adr/ADR-001-estilo-arquitectonico.md 'Contexto' 'Opciones consideradas' 'Decisión' 'Consecuencias positivas' 'Consecuencias negativas' 'Hexagonal' 'Capas' 'MVC' 'Microservicios'
sh scripts/contiene.sh docs/adr/ADR-002-extraccion-de-intencion.md 'Laya' '60 %' '84 %' '812 ms' 'Claude' 'regex'
sh scripts/contiene.sh docs/diagramas/c4.md 'C4Context' 'C4Container' 'C4Component' 'Corte 1'
sh scripts/contiene.sh docs/arquitectura.md '## 1.' '## 2.' '## 3.' '## 4.' '## 5.' '## 6.' '## 7.'
sh scripts/contiene.sh README.md 'Tabla de trazabilidad' 'Atributo de calidad' 'Decisión arquitectónica' 'Prueba que lo evidencia' 'Menú interactivo' 'Captura de voz' 'Funcionalidad agregada porque el reto la exige' 'docs/adr/ADR-001-estilo-arquitectonico.md' 'docs/arquitectura.md' '!__'
./mvnw -q clean verify
```

**Checkpoint**

```bash
git add -A && git commit -m "E1-T6: Escribir ADR, diagramas C4, arquitectura y trazabilidad"
git tag step-21-documentacion
```

Run both after the last `Verify` command exits 0, before starting the next task. **La persona responsable ejecuta el commit y el tag** (el agente prepara el cambio y se detiene aquí). Luego `git push` de la rama y `git push origin step-21-documentacion`. Never invent the tag: copy the `checkpoint` field.

---

## Epic acceptance

The epic is done when every task is `done` **and**:

1. **WHEN** `./mvnw -q clean verify` runs on the tree the three epics leave **THE SYSTEM SHALL** exit 0 with `ReglasArquitecturaTest` green, proving the dominio still has no Spring, JPA or Anthropic dependency after every other adapter was added.
2. **WHEN** `./mvnw -q compile exec:java` runs **THE SYSTEM SHALL** still print the Corte 1 demo through `PASO 11` with `Creado: PF-001` first — the console adapter survived the migration.

```bash
./mvnw -q clean verify
./mvnw -q compile exec:java | grep -q "Creado: PF-001"
./mvnw -q compile exec:java | grep -q "PASO 11"
```

Run from the project root. Both criteria must be decidable by these commands.

## Pitfalls

- **`git mv` a medias** — si el bloque de E1-T2 se corta, no lo repitas encima: `git reset --hard step-01-base-spring` y córrelo completo. Correrlo dos veces falla en el primer `git mv`, y eso es lo correcto.
- **Anotar un servicio con `@Service`** — funciona, pero rompe la regla de que el núcleo es Java puro y crea un segundo bean del mismo puerto junto al de `ConfiguracionPetFinder`. Los beans se declaran solo en `config/`.
- **Cambiar el constructor de `Ubicacion` de dos argumentos** — todo el Corte 1 y las pruebas lo usan. El de cuatro se agrega; el de dos se conserva.
- **ArchUnit en rojo por una regla vacía** — ArchUnit 1.x falla cuando una regla no encuentra clases. Las reglas sobre paquetes que aún no existen llevan `allowEmptyShould(true)`, como en el contenido literal.
- **Afirmar resultados en la documentación que no se midieron** — cada número de la trazabilidad sale de un reporte real (Surefire, Failsafe, JaCoCo, k6). Si algo no se logró, se dice.

## Before moving on

- [ ] Every task in this epic is `done` in `tasks.json` — no task left `in_progress`.
- [ ] Every `verify` command of every task in this epic passed, not just the first one.
- [ ] No `verify` command was edited, and none was skipped because a file it names did not exist.
- [ ] **Every task in this epic has its `checkpoint` tag in version control** — one tag per task, matching the `checkpoint` value in `tasks.json`. `git tag -l 'step-*'` lists them, and they were pushed with `git push origin <tag>`.
- [ ] Gate command passes clean, run from the project root: `./mvnw -q clean verify`.
- [ ] Every "Produced" contract above exists with the stated signature.
- [ ] No file outside the subtree was modified.
- [ ] `.env.example` updated if this epic added a variable — this project's variables are `ANTHROPIC_API_KEY`, `PETFINDER_IA_MODELO`, `PETFINDER_IA_TOPE_DIARIO` and `PORT`.
- [ ] One commit per task, each prefixed with its task id, each followed by its checkpoint tag — **hechos por la persona responsable**.
- [ ] Las ramas entraron a `main` por Pull Request con *merge commit* (no *squash*): con *squash* los tags quedan apuntando a commits que no están en `main`.
