package petfinder.integracion.persistencia;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import petfinder.application.port.entrada.GestionReportes;
import petfinder.domain.model.ReporteMascota;

/** La aplicación web arranca con los casos de ejemplo cargados por el puerto. */
@SpringBootTest(properties = "ANTHROPIC_API_KEY=")
class DatosDeEjemploIT {

    @Autowired
    private GestionReportes gestion;

    @Test
    @DisplayName("Al arrancar la app web se cargan los casos de ejemplo")
    void cargaLosCasosDeEjemplo() {
        // Act
        List<String> resumenes = gestion.listarActivos().stream().map(ReporteMascota::resumen).toList();
        // Assert
        assertTrue(resumenes.stream().anyMatch(r -> r.contains("Luna")));
        assertTrue(resumenes.stream().anyMatch(r -> r.contains("Max")));
        assertTrue(resumenes.stream().anyMatch(r -> r.contains("Gata gris")));
        assertTrue(resumenes.stream().anyMatch(r -> r.contains("Copito")));
    }
}
