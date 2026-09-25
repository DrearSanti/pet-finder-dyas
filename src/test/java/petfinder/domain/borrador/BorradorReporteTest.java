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
