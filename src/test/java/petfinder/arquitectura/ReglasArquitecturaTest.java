package petfinder.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import petfinder.application.port.entrada.AsistenteReportes;
import petfinder.application.port.entrada.GestionReportes;
import petfinder.application.port.salida.RepositorioReportes;

/**
 * Reglas de la arquitectura hexagonal convertidas en pruebas: si alguien
 * rompe la dirección de las dependencias, mvnw test se pone en rojo, y en el
 * pipeline se pone en rojo el check reglas-arquitectura.
 *
 * Cada regla protege una frontera que nombra ADR-001. Ninguna usa
 * allowEmptyShould: si un paquete cambia de nombre y la regla deja de
 * encontrar clases, ArchUnit falla en vez de pasar sin revisar nada.
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

    /**
     * Antes solo miraba la web, y la consola usaba ServicioReportes y
     * ServicioAvistamientos directamente. Ahora cubre todo adaptadores.entrada:
     * una forma nueva de entrar al sistema (consola, web, voz, un chat) solo
     * puede hablar con los puertos de entrada.
     */
    @ArchTest
    static final ArchRule lasEntradasSoloHablanConPuertos = noClasses()
            .that().resideInAPackage("petfinder.adaptadores.entrada..")
            .should().dependOnClassesThat().resideInAPackage("petfinder.application.service..");

    /** Cambiar la base de datos no puede obligar a tocar el extractor con Claude, ni al revés. */
    @ArchTest
    static final ArchRule laPersistenciaNoConoceOtrosAdaptadores = noClasses()
            .that().resideInAPackage("petfinder.adaptadores.salida.persistencia..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "petfinder.adaptadores.entrada..", "petfinder.adaptadores.salida.ia..");

    @ArchTest
    static final ArchRule laIaNoConoceOtrosAdaptadores = noClasses()
            .that().resideInAPackage("petfinder.adaptadores.salida.ia..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "petfinder.adaptadores.entrada..", "petfinder.adaptadores.salida.persistencia..");

    /**
     * config es el composition root: el único lugar que nombra RepositorioReportesH2,
     * ExtractorClaude y compañía. Si otra clase los usara, cambiar de adaptador
     * dejaría de ser "cambiar una línea de config".
     */
    @ArchTest
    static final ArchRule soloConfigConoceLosAdaptadoresDeSalida = noClasses()
            .that().resideOutsideOfPackages("petfinder.config..", "petfinder.adaptadores.salida..")
            .should().dependOnClassesThat().resideInAPackage("petfinder.adaptadores.salida..");

    /**
     * El asistente nunca publica (amenaza T8 de docs/seguridad.md): aunque una
     * frase manipule al modelo, quien implementa AsistenteReportes no tiene cómo
     * llegar a GestionReportes ni al repositorio. Publicar siempre es un
     * POST /api/reportes que dispara la persona.
     */
    @ArchTest
    static final ArchRule elAsistenteNuncaPublica = noClasses()
            .that().implement(AsistenteReportes.class)
            .should().dependOnClassesThat().areAssignableTo(GestionReportes.class)
            .orShould().dependOnClassesThat().areAssignableTo(RepositorioReportes.class);

    @ArchTest
    static final ArchRule soloElAdaptadorDeIaUsaElSdkDeAnthropic = noClasses()
            .that().resideOutsideOfPackage("petfinder.adaptadores.salida.ia..")
            .should().dependOnClassesThat().resideInAPackage("com.anthropic..");

    @ArchTest
    static final ArchRule soloLaPersistenciaUsaJpa = noClasses()
            .that().resideOutsideOfPackage("petfinder.adaptadores.salida.persistencia..")
            .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..");
}
