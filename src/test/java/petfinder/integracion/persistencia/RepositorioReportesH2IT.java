package petfinder.integracion.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;

import petfinder.adaptadores.salida.persistencia.h2.ReporteJpaRepository;
import petfinder.adaptadores.salida.persistencia.h2.RepositorioReportesH2;
import petfinder.domain.model.Avistamiento;
import petfinder.domain.model.Contacto;
import petfinder.domain.model.EstadoReporte;
import petfinder.domain.model.Mascota;
import petfinder.domain.model.ReporteEncontrada;
import petfinder.domain.model.ReporteMascota;
import petfinder.domain.model.ReportePerdida;
import petfinder.domain.model.Ubicacion;

/**
 * Frontera adaptador de salida + base de datos real (H2 embebida).
 * @DataJpaTest revierte cada prueba al terminar: son aisladas y no dependen
 * del orden. Nunca se afirma un ID fijo: cada prueba crea el suyo.
 */
@DataJpaTest
@Import(RepositorioReportesH2IT.Configuracion.class)
class RepositorioReportesH2IT {

    @TestConfiguration
    static class Configuracion {
        @Bean
        RepositorioReportesH2 repositorioReportesH2(ReporteJpaRepository jpa) {
            return new RepositorioReportesH2(jpa);
        }
    }

    private static final LocalDateTime HACE_UNA_HORA = LocalDateTime.of(2026, 9, 24, 15, 0);

    @Autowired
    private RepositorioReportesH2 repositorio;

    private static ReportePerdida perdida(String id, EstadoReporte estado, List<Avistamiento> avistamientos) {
        return ReportePerdida.reconstruir(id, new Ubicacion("Cedritos", "Parque 147", 4.7235, -74.0417),
                "Se escapó en la tarde", new Mascota("Luna", "perro", "criolla", "blanca", "mancha negra"),
                new Contacto("Camila", "3001234567"), HACE_UNA_HORA, estado, avistamientos);
    }

    @Test
    @DisplayName("Guardar una pérdida y buscarla conserva mascota, contacto y coordenadas")
    void idaYVueltaDePerdida() {
        // Arrange
        repositorio.guardar(perdida("PF-T01", EstadoReporte.ACTIVO, List.of()));
        // Act
        ReporteMascota leido = repositorio.buscarPorId("PF-T01").orElseThrow();
        // Assert
        ReportePerdida p = assertInstanceOf(ReportePerdida.class, leido);
        assertEquals("Luna", p.getMascota().nombre());
        assertEquals("3001234567", p.getContactoPropietario().medioContacto());
        assertEquals(4.7235, p.getUbicacion().latitud());
    }

    @Test
    @DisplayName("Guardar un hallazgo conserva la descripción del animal")
    void idaYVueltaDeEncontrada() {
        // Arrange
        repositorio.guardar(ReporteEncontrada.reconstruir("PF-T02", new Ubicacion("Chía", "Parque"),
                "Estaba sola", "Gata gris", new Contacto("Lorenzi", "3007654321"), HACE_UNA_HORA, EstadoReporte.ACTIVO));
        // Act
        ReporteEncontrada e = assertInstanceOf(ReporteEncontrada.class, repositorio.buscarPorId("PF-T02").orElseThrow());
        // Assert
        assertEquals("Gata gris", e.getDescripcionMascota());
    }

    @Test
    @DisplayName("Buscar un ID inexistente devuelve Optional vacío")
    void idInexistente() {
        assertTrue(repositorio.buscarPorId("PF-NO-EXISTE").isEmpty());
    }

    @Test
    @DisplayName("Listar activos excluye resueltos y cerrados")
    void listarActivosFiltraPorEstado() {
        // Arrange
        repositorio.guardar(perdida("PF-T03", EstadoReporte.ACTIVO, List.of()));
        repositorio.guardar(perdida("PF-T04", EstadoReporte.RESUELTO, List.of()));
        repositorio.guardar(perdida("PF-T05", EstadoReporte.CERRADO, List.of()));
        // Act
        List<String> ids = repositorio.listarActivos().stream().map(ReporteMascota::getId).toList();
        // Assert
        assertTrue(ids.contains("PF-T03"));
        assertFalse(ids.contains("PF-T04"));
        assertFalse(ids.contains("PF-T05"));
    }

    @Test
    @DisplayName("Guardar de nuevo un reporte resuelto actualiza su estado")
    void actualizarEstado() {
        // Arrange
        ReportePerdida reporte = perdida("PF-T06", EstadoReporte.ACTIVO, List.of());
        repositorio.guardar(reporte);
        // Act
        reporte.resolver();
        repositorio.guardar(reporte);
        // Assert
        assertEquals(EstadoReporte.RESUELTO, repositorio.buscarPorId("PF-T06").orElseThrow().getEstado());
    }

    @Test
    @DisplayName("Los avistamientos se guardan y se recuperan con el reporte")
    void avistamientosIdaYVuelta() {
        // Arrange
        ReportePerdida reporte = perdida("PF-T07", EstadoReporte.ACTIVO, List.of());
        repositorio.guardar(reporte);
        reporte.agregarAvistamiento(new Avistamiento("AV-T1", HACE_UNA_HORA.plusMinutes(30),
                new Ubicacion("Cedritos", "Canchas"), "La vi corriendo", null));
        // Act
        repositorio.guardar(reporte);
        // Assert
        ReportePerdida leido = (ReportePerdida) repositorio.buscarPorId("PF-T07").orElseThrow();
        assertEquals(1, leido.getAvistamientos().size());
        assertEquals("La vi corriendo", leido.getAvistamientos().get(0).descripcion());
    }

    @Test
    @DisplayName("La fecha de creación se conserva al reconstruir")
    void fechaSeConserva() {
        // Arrange
        repositorio.guardar(perdida("PF-T08", EstadoReporte.ACTIVO, List.of()));
        // Act + Assert
        assertEquals(HACE_UNA_HORA, repositorio.buscarPorId("PF-T08").orElseThrow().getFechaCreacion());
    }
}
