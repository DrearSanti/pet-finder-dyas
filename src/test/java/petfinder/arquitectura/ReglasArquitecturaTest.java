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
