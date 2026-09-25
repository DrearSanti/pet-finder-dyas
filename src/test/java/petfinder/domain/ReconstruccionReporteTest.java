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
